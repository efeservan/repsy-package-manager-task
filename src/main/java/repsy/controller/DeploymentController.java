package repsy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import repsy.model.Dependency;
import repsy.model.Package;
import repsy.repository.PackageRepository;
import repsy.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;

@RestController
@RequestMapping("/{packageName}/{version}")
public class DeploymentController {

    private final StorageService storageService;
    private final PackageRepository packageRepository;
    private final ObjectMapper objectMapper;

    public DeploymentController(
            StorageService storageService,
            PackageRepository packageRepository,
            ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.packageRepository = packageRepository;
        this.objectMapper = objectMapper;
    }

    @PutMapping
    public ResponseEntity<String> deployPackage(
            @PathVariable String packageName,
            @PathVariable String version,
            @RequestParam("file") MultipartFile file) {

        try {
            String fileName = file.getOriginalFilename();

            if (fileName == null) {
                return ResponseEntity.badRequest().body("File name is required and cannot be blank");
            }

            if (!fileName.equals("package.rep") && !fileName.equals("meta.json")) {
                return ResponseEntity.badRequest().body("Only package.rep or meta.json files are allowed");
            }

            if (fileName.equals("meta.json")) {
                JsonNode metaJson = objectMapper.readTree(file.getBytes());

                if (!metaJson.has("name") || !metaJson.has("version")) {
                    return ResponseEntity.badRequest().body("meta.json file should have name and version fields");
                }

                String metaName = metaJson.get("name").asText();
                String metaVersion = metaJson.get("version").asText();

                if (!metaName.equals(packageName) || !metaVersion.equals(version)) {
                    return ResponseEntity.badRequest().body("meta.json name and version should match with URL");
                }

                savePackageMetadata(metaJson);
            }

            storageService.storeFile(packageName, version, fileName, file.getBytes());

            return ResponseEntity.ok("File uploaded successfully: ");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload error " + e.getMessage());
        }
    }

    private void savePackageMetadata(JsonNode metaJson) {
        String name = metaJson.get("name").asText();
        String version = metaJson.get("version").asText();

        // Check if package exists, if not create new
        Optional<Package> packageOpt = packageRepository.findByNameAndVersion(name, version);
        Package packageEntity = packageOpt.orElse(new Package());

        // Update/create package info
        packageEntity.setName(name);
        packageEntity.setVersion(version);

        // Check and set optional fields
        if (metaJson.has("author")) {
            packageEntity.setAuthor(metaJson.get("author").asText());
        }

        packageEntity.setCreatedAt(LocalDateTime.now());

        // Clear existing dependencies
        packageEntity.getDependencies().clear();

        // Add new dependencies
        if (metaJson.has("dependencies") && metaJson.get("dependencies").isArray()) {
            JsonNode dependencies = metaJson.get("dependencies");

            for (Iterator<JsonNode> it = dependencies.elements(); it.hasNext();) {
                JsonNode dependency = it.next();

                if (dependency.has("package") && dependency.has("version")) {
                    String depName = dependency.get("package").asText();
                    String depVersion = dependency.get("version").asText();

                    Dependency dep = new Dependency();
                    dep.setPackageName(depName);
                    dep.setPackageVersion(depVersion);

                    packageEntity.addDependency(dep);
                }
            }
        }

        packageRepository.save(packageEntity);
    }
}