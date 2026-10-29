package com.notasapp.scheduler;

import com.notasapp.model.Adjunto;
import com.notasapp.repository.AdjuntoRepository;
import com.notasapp.security.ClamAvClient;
import com.notasapp.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.antivirus", name = "enabled", havingValue = "true")
public class AttachmentScanScheduler {

    private final AdjuntoRepository adjuntoRepository;
    private final StorageService storageService;
    private final ClamAvClient clamAvClient;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional
    public void scanPending() {
        List<Adjunto> pending = adjuntoRepository.findTop20ByScanStatusAndDeletedAtIsNullOrderByCreatedAtAsc("PENDING");
        for (Adjunto adjunto : pending) {
            try (InputStream input = storageService.get(adjunto.getStorageKey())) {
                boolean clean = clamAvClient.isClean(input.readAllBytes());
                adjunto.setScanStatus(clean ? "READY" : "INFECTED");
                adjunto.setScanEngine("clamav");
                adjunto.setScannedAt(Instant.now());
                if (!clean) {
                    adjunto.setEstado("INFECTED");
                    storageService.delete(adjunto.getStorageKey());
                }
                adjuntoRepository.save(adjunto);
            } catch (Exception ex) {
                adjunto.setScanStatus("ERROR");
                adjunto.setScanEngine("clamav");
                adjunto.setScanError(ex.getMessage() == null ? "scan error" : ex.getMessage().substring(0, Math.min(500, ex.getMessage().length())));
                adjunto.setScannedAt(Instant.now());
                adjuntoRepository.save(adjunto);
                log.warn("Error escaneando adjunto {}", adjunto.getId());
            }
        }
    }
}
