package com.notasapp.storage;

import java.io.InputStream;
import java.time.Duration;

public interface StorageService {

    void put(String key, InputStream inputStream, long size, String contentType);

    InputStream get(String key);

    String presignedGetUrl(String key, Duration ttl);

    void delete(String key);
}
