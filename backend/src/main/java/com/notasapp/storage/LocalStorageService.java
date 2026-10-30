package com.notasapp.storage;

import com.notasapp.config.AttachmentProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.attachments", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final AttachmentProperties properties;
    private Path root;

    @PostConstruct
    void init() throws IOException {
        root = Path.of(properties.getLocalPath()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override
    public void put(String key, InputStream inputStream, long size, String contentType) {
        try {
            Path target = resolve(key);
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar el archivo", ex);
        }
    }

    @Override
    public InputStream get(String key) {
        try {
            return Files.newInputStream(resolve(key));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el archivo", ex);
        }
    }

    @Override
    public String presignedGetUrl(String key, Duration ttl) {
        return null;
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo borrar el archivo", ex);
        }
    }

    private Path resolve(String key) {
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Ruta de storage inválida");
        }
        return target;
    }
}
