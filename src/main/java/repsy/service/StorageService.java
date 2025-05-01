package repsy.service;

public interface StorageService {

    void storeFile(String packageName, String version, String fileName, byte[] content);

    byte[] getFile(String packageName, String version, String fileName);

    boolean fileExists(String packageName, String version, String fileName);
}