package com.notasapp.security;

import com.notasapp.config.AntivirusProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.antivirus", name = "enabled", havingValue = "true")
public class ClamAvClient {

    private final AntivirusProperties properties;

    public boolean isClean(byte[] content) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(properties.getHost(), properties.getPort()),
                    (int) properties.getTimeout().toMillis());
            socket.setSoTimeout((int) properties.getTimeout().toMillis());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
            int offset = 0;
            while (offset < content.length) {
                int size = Math.min(8192, content.length - offset);
                out.writeInt(size);
                out.write(content, offset, size);
                offset += size;
            }
            out.writeInt(0);
            out.flush();
            InputStream in = socket.getInputStream();
            String response = new String(in.readAllBytes(), StandardCharsets.US_ASCII);
            return response.contains("OK") && !response.contains("FOUND");
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo escanear con ClamAV", ex);
        }
    }
}
