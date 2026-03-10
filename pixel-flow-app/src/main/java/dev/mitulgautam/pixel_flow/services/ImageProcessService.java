package dev.mitulgautam.pixel_flow.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageProcessService {
    private final Path fileStorageLocation = Paths.get(System.getenv().getOrDefault("FILE_STORAGE_PATH", "/tmp/pixelflow")).toAbsolutePath().normalize();
    ConnectionFactory factory = new ConnectionFactory();

    public ImageProcessService() {
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        factory.setCredentialsProvider(new DefaultCredentialsProvider("pixelflow", "pixelflow"));
        
        // Ensure storage directory exists
        try {
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
        } catch (IOException e) {
            System.err.println("Could not create storage directory: " + e.getMessage());
        }
    }

    public void sendImageForProcessing(MultipartFile[] files) {
        try (Connection conn = factory.newConnection(); Channel channel = conn.createChannel()) {
            channel.queueDeclare("watermark-processing", true, false, false, null);

            for (MultipartFile file : files) {
                try {
                    UUID uuid = UUID.randomUUID();
                    Path targetLocation = this.fileStorageLocation.resolve(uuid.toString() + "." + Arrays.stream(Objects.requireNonNull(file.getOriginalFilename()).split("\\.")).toList().get(1));
                    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                    channel.basicPublish("", "watermark-processing", MessageProperties.PERSISTENT_TEXT_PLAIN, targetLocation.toString().getBytes(StandardCharsets.UTF_8));
                    System.out.println("Image has been sent to queue for watermark processing.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
