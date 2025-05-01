package repsy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repsy.service.FileSystemStorageService;
import repsy.service.ObjectStorageService;
import repsy.service.StorageService;

@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Value("${storageStrategy}")
    private String storageStrategy;

    @Bean
    public StorageService storageService(FileSystemStorageService fileSystemStorageService,
            ObjectStorageService objectStorageService) {

        log.info("Selected storage strategy: {}", storageStrategy);

        switch (storageStrategy) {
            case "file-system":
                log.info("Using FileSystemStorageService");
                return fileSystemStorageService;
            case "object-storage":
                log.info("Using ObjectStorageService");
                return objectStorageService;
            default:
                throw new IllegalArgumentException("Invalid strategy " + storageStrategy);
        }
    }
}