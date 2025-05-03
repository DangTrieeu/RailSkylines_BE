package com.fourt.railskylines.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fourt.railskylines.util.error.StorageException;

@Service
public class FileService {
    @Value("${railskylines.upload-file.base-uri}")
    private String baseURI;

    // Utility method to sanitize filenames
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed";
        }
        // Replace invalid characters with underscores
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        // Ensure the filename is not empty after sanitization
        return sanitized.isEmpty() ? "unnamed" : sanitized;
    }

    public void createDirectory(String folder) throws URISyntaxException {
        URI uri = new URI(folder);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectory(tmpDir.toPath());
                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
        }
    }

    public String store(MultipartFile file, String folder) throws URISyntaxException, IOException {
        String originalName = file.getOriginalFilename();
        String sanitizedName = sanitizeFileName(originalName);
        String finalName = System.currentTimeMillis() + "-" + sanitizedName;
        String uriString = baseURI + folder + "/" + finalName;
        // Encode the URI to handle any remaining special characters
        URI uri = new URI(uriString.replace(" ", "%20"));
        Path path = Paths.get(uri);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

    public long getFileLength(String fileName, String folder) throws URISyntaxException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());
        if (!tmpDir.exists() || tmpDir.isDirectory()) {
            return 0;
        }
        return tmpDir.length();
    }

    public InputStreamResource getResource(String fileName, String folder)
            throws URISyntaxException, FileNotFoundException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);
        File file = new File(path.toString());
        return new InputStreamResource(new FileInputStream(file));
    }
}