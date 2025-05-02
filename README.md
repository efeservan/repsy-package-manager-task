# Repsy Package Manager API

This project is a backend API implementation for a fictional package manager system for the Repsy programming language. It allows uploading and downloading packages (`package.rep`) and their metadata (`meta.json`).

## Features

*   REST endpoint for uploading packages and metadata (`PUT /{packageName}/{version}`).
*   REST endpoint for downloading packages and metadata (`GET /{packageName}/{version}/{fileName}`).
*   Support for two different storage strategies:
    *   File System (`file-system`)
    *   Object Storage (`object-storage`) - Integrated with Minio.
*   Stores package metadata in a PostgreSQL database.
*   Easy setup and execution using Docker and Docker Compose.

## Technologies Used

*   Java 17 (or the LTS version you used)
*   Spring Boot 3.1.0 (or the version you used)
*   Spring Data JPA
*   PostgreSQL
*   Minio (For Object Storage)
*   Maven
*   Docker
*   Docker Compose

## Prerequisites

*   Git
*   Docker ([https://www.docker.com/get-started](https://www.docker.com/get-started))
*   Docker Compose (Usually included with Docker Desktop)

## Setup and Running

1.  **Clone the Project:**
    ```bash
    git clone https://github.com/efeservan/repsy-package-manager-task.git # Use your actual repo URL
    cd repsy-package-manager-task
    ```

2.  **Start with Docker Compose:**
    Navigate to the project's root directory in your terminal and run the following command:
    ```bash
    docker-compose up --build -d
    ```
    This command will:
    *   Build the Docker image for the Spring Boot application if necessary.
    *   Start containers for `repsy-app` (the Spring Boot application), `postgres` (the database), and `minio` (object storage).
    *   Create the necessary network connections and volumes.

3.  **Access Points:**
    *   **Application API:** `http://localhost:8098` (or the host port you specified in the `ports` section of `docker-compose.yml`)
    *   **Minio Console:** `http://localhost:9001`
        *   **Access Key (Username):** `minioadmin` (or as specified in `docker-compose.yml`)
        *   **Secret Key (Password):** `minioadmin` (or as specified in `docker-compose.yml`)
    *   **PostgreSQL Database:** (Optional, if you want to connect with a DB client)
        *   **Host:** `localhost`
        *   **Port:** `5432`
        *   **User:** `postgres`
        *   **Password:** `password`
        *   **Database:** `repsy`

## Configuration

Main configurations are handled via environment variables in the `docker-compose.yml` file, which override the values in `src/main/resources/application.properties`.

*   **`STORAGE_STRATEGY`**: The storage strategy to use (`file-system` or `object-storage`). Default: `file-system` (if not changed in `application.properties`). It is set to `object-storage` in `docker-compose.yml`.
*   **`SPRING_DATASOURCE_URL`**: PostgreSQL connection URL (`jdbc:postgresql://postgres:5432/repsy`). Uses the `postgres` service name.
*   **`SPRING_DATASOURCE_USERNAME`**: Database username.
*   **`SPRING_DATASOURCE_PASSWORD`**: Database password.
*   **`OBJECT_STORAGE_ENDPOINT`**: Minio server URL (`http://minio:9000`). Uses the `minio` service name.
*   **`OBJECT_STORAGE_ACCESS_KEY`**: Minio access key.
*   **`OBJECT_STORAGE_SECRET_KEY`**: Minio secret key.
*   **`OBJECT_STORAGE_BUCKET_NAME`**: The Minio bucket name to use.

## API Endpoints

### 1. Deploy Package

*   **Endpoint:** `PUT /{packageName}/{version}`
*   **Description:** Uploads a `package.rep` or `meta.json` file for the specified package name and version.
*   **Request Body:** `multipart/form-data` containing a field named `file` with the file content.
*   **Accepted Files:** Only `package.rep` and `meta.json`.
*   **Validations:**
    *   File name check.
    *   If uploading `meta.json`:
        *   `name` and `version` fields are mandatory.
        *   `name` and `version` in `meta.json` must match `packageName` and `version` from the URL.
*   **Example Request (`curl`):**
    ```bash
    # Upload meta.json
    curl -X PUT -F "file=@meta.json" http://localhost:8098/test-package/1.2.3

    # Upload package.rep
    curl -X PUT -F "file=@package.rep" http://localhost:8098/test-package/1.2.3
    ```
*   **Success Response:** `200 OK` - Body: `File uploaded successfully:`
*   **Error Responses:** `400 Bad Request`, `500 Internal Server Error`

### 2. Download Package

*   **Endpoint:** `GET /{packageName}/{version}/{fileName}`
*   **Description:** Downloads the content of the specified file (`package.rep` or `meta.json`) for the given package name and version.
*   **Validations:**
    *   Does the package/version exist in the database?
    *   Does the file exist in the storage layer?
*   **Example Request (`curl`):**
    ```bash
    # Download meta.json (print to terminal)
    curl http://localhost:8098/test-package/1.2.3/meta.json

    # Download package.rep (save to file)
    curl -o downloaded_package.rep http://localhost:8098/test-package/1.2.3/package.rep
    ```
*   **Success Response:** `200 OK`
    *   `Content-Type`: `application/json` (`meta.json`) or `application/octet-stream` (`package.rep`).
    *   `Content-Disposition`: `attachment; filename="..."`
*   **Error Responses:** `404 Not Found`, `500 Internal Server Error`

## Storage Layer Libraries

The project supports two different storage mechanisms using the Strategy Pattern: `FileSystemStorageService` and `ObjectStorageService`. These services are included within the main application code. The strategy to be used is selected via the `STORAGE_STRATEGY` configuration parameter mentioned above.