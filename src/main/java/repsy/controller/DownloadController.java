package repsy.controller;

import repsy.repository.PackageRepository;
import repsy.service.StorageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/{packageName}/{version}/{fileName}")
public class DownloadController {

    private final StorageService storageService;
    private final PackageRepository packageRepository;

    public DownloadController(
            StorageService storageService,
            PackageRepository packageRepository) {
        this.storageService = storageService;
        this.packageRepository = packageRepository;
    }

    @GetMapping
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String packageName,
            @PathVariable String version,
            @PathVariable String fileName) {

        try {
            // Check if package exists in the db
            if (!packageRepository.existsByNameAndVersion(packageName, version)) {
                return ResponseEntity.notFound().build();
            }

            // Check if file exists in storage
            if (!storageService.fileExists(packageName, version, fileName)) {
                return ResponseEntity.notFound().build();
            }

            // Get file from storage
            byte[] fileContent = storageService.getFile(packageName, version, fileName);

            // Convert byte array to Resource
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            // Determine content type
            MediaType contentType = determineContentType(fileName);

            // Return file as download
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType determineContentType(String fileName) {
        if (fileName.endsWith(".rep")) {
            return MediaType.APPLICATION_OCTET_STREAM;
        } else if (fileName.endsWith(".json")) {
            return MediaType.APPLICATION_JSON;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}