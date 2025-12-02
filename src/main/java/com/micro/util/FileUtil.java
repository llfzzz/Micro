package com.micro.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Handles file storage conventions for media uploads.
 */
public final class FileUtil {

    private FileUtil() {
    }

    public static String saveToStorage(InputStream inputStream, String storageRoot, long userId, String originalName) throws IOException {
        LocalDate today = LocalDate.now();
        String subDir = String.format("%d/%02d/user_%d", today.getYear(), today.getMonthValue(), userId);
        return saveToStorage(inputStream, storageRoot, subDir, originalName);
    }

    public static String saveToStorage(InputStream inputStream, String storageRoot, String subDir, String originalName) throws IOException {
        String normalizedRoot = Path.of(storageRoot).toAbsolutePath().toString();
        String extension = extractExtension(originalName);
        String relative = String.format("%s/%s%s", subDir, UUID.randomUUID(), extension);
        Path target = Path.of(normalizedRoot, relative);
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return relative.replace('\\', '/');
    }

    public static void deleteFile(String storageRoot, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path target = Path.of(storageRoot, relativePath);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // Ignore deletion errors
        }
    }

    private static String extractExtension(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
            return name.substring(dot);
        }
        return "";
    }
}
