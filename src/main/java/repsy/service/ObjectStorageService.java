package repsy.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class ObjectStorageService implements StorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public ObjectStorageService(
            @Value("${storage.objectstorage.endpoint}") String endpoint,
            @Value("${storage.objectstorage.accessKey}") String accessKey,
            @Value("${storage.objectstorage.secretKey}") String secretKey,
            @Value("${storage.objectstorage.bucket}") String bucketName) {

        this.bucketName = bucketName;

        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Minio client", e);
        }
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Minio bucket check failed", e);
        }
    }

    @Override
    public void storeFile(String packageName, String version, String fileName, byte[] content) {
        try {
            // Create the object name: packageName/version/fileName
            String objectName = String.format("%s/%s/%s", packageName, version, fileName);

            // Upload the file
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(content), content.length, -1)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file to object storage: " + fileName, e);
        }
    }

    @Override
    public byte[] getFile(String packageName, String version, String fileName) {
        try {
            // Create the object name
            String objectName = String.format("%s/%s/%s", packageName, version, fileName);

            // Get object
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = response.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file from object storage: " + fileName, e);
        }
    }

    @Override
    public boolean fileExists(String packageName, String version, String fileName) {
        try {
            // Create the object name
            String objectName = String.format("%s/%s/%s", packageName, version, fileName);

            // Check object info
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().contains("NoSuchKey")) {
                return false;
            }
            throw new RuntimeException("File check failed", e);
        } catch (Exception e) {
            throw new RuntimeException("File check failed", e);
        }
    }
}