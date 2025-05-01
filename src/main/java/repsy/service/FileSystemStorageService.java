package repsy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    public FileSystemStorageService(@Value("${storage.filesystem.location}") String location) {
        this.rootLocation = Paths.get(location);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Storage directory could not be created", e);
        }
    }

    @Override
    public void storeFile(String packageName, String version, String fileName, byte[] content) {
        try {
            // Create package and version directories
            Path packageVersionDir = rootLocation.resolve(packageName).resolve(version);
            Files.createDirectories(packageVersionDir);

            // Write the file
            Path filePath = packageVersionDir.resolve(fileName);
            Files.write(filePath, content);
        } catch (IOException e) {
            throw new RuntimeException("File could not be saved: " + fileName, e);
        }
    }

    @Override
    public byte[] getFile(String packageName, String version, String fileName) {
        try {
            Path filePath = rootLocation.resolve(packageName).resolve(version).resolve(fileName);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("File could not be read: " + fileName, e);
        }
    }

    @Override
    public boolean fileExists(String packageName, String version, String fileName) {
        Path filePath = rootLocation.resolve(packageName).resolve(version).resolve(fileName);
        return Files.exists(filePath);
    }
}