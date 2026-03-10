package dev.mitulgautam.pixelflow.worker;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

public class SizeProcessingWorker {
    public static void main(String[] s) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider("pixelflow", "pixelflow"));

        Connection connection;
        try {
            connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare("size-processing", true, false, false, null);
            System.out.println(" [*] Waiting for images to start processing size. To exit press CTRL+C");
            channel.basicQos(1);

            DeliverCallback deliverCallback = getDeliverCallback(channel);
            channel.basicConsume("size-processing", false, deliverCallback, consumerTag -> {
            });
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private static DeliverCallback getDeliverCallback(Channel channel) {
        return (deliverCallback, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("Processed: | [x] Received '" + message + "'");

            try {
                doWork(message);
                // channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } finally {
                System.out.println(" [x] Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
    }

    public static void doWork(String message) throws IOException {
        String imagePath = new String(message.getBytes(), StandardCharsets.UTF_8);

        Path path = Paths.get(imagePath);

        String fileName = path.getFileName().toString();
        System.out.println("[X] Sizing " + fileName);
        String directory = path.getParent().toString();
        // Create folder if it doesn't exist
        File outputDir = new File(directory, "sized");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, fileName);
        Thumbnails.of(imagePath)
                .size(200, 200)
                .toFile(outputFile);
    }
}
