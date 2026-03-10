import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

public class WatermarkProcessingWorker {
    public static void main(String[] s) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider("pixelflow", "pixelflow"));

        Connection connection = null;
        try {
            connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare("watermark-processing", true, false, false, null);
            channel.queueDeclare("size-processing", true, false, false, null);
            System.out.println(" [*] Waiting for images to start processing watermark. To exit press CTRL+C");
            channel.basicQos(1);

            DeliverCallback deliverCallback = getDeliverCallback(channel);
            channel.basicConsume("watermark-processing", false, deliverCallback, consumerTag -> {
            });
        } catch (IOException | TimeoutException e) {
            System.out.println("Failed to process.");
        }
    }

    private static DeliverCallback getDeliverCallback(Channel channel) {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("Processed: | [x] Received '" + message + "'");

            try {
                String imagePath = doWork(message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                channel.basicPublish("", "size-processing", MessageProperties.PERSISTENT_TEXT_PLAIN,
                        imagePath.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println(" [x] Failed due to reason. " + e);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
    }

    public static String doWork(String message) throws IOException, NullPointerException {
        String imagePath = new String(message.getBytes(), StandardCharsets.UTF_8);

        Path path = Paths.get(imagePath);

        String fileName = path.getFileName().toString();
        System.out.println("[X] Watermarking " + fileName);
        String directory = path.getParent().toString();
        // Create folder if it doesn't exist
        File outputDir = new File(directory, "watermarked");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, fileName);

        BufferedImage fullSizeLogo = ImageIO
                .read(new File(System.getenv().getOrDefault("LOGO_PATH", "src/main/resources/wmc-logo.png")));
        BufferedImage logo = Thumbnails.of(fullSizeLogo)
                .width(200)
                .keepAspectRatio(true)
                .asBufferedImage();
        Thumbnails.of(imagePath)
                .scale(1.0)
                .watermark(Positions.BOTTOM_RIGHT, logo, 0.5f)
                .toFile(outputFile);
        return outputFile.getAbsolutePath();
    }
}
