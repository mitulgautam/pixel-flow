package dev.mitulgautam.pixel_flow.controllers;

import dev.mitulgautam.pixel_flow.services.ImageProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/image-processing")
public class ImageProcessingController {

    private final ImageProcessService imageProcessService;

    public ImageProcessingController(ImageProcessService imageProcessService) {
        this.imageProcessService = imageProcessService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> postImageForProcessing(@RequestParam("images") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("Please select at least one image.");
        }
        imageProcessService.sendImageForProcessing(files);
        return ResponseEntity.ok("Successfully uploaded.");
    }
}
