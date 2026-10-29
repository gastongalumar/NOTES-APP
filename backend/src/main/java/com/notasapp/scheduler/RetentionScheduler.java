package com.notasapp.scheduler;

import com.notasapp.model.Adjunto;
import com.notasapp.model.Nota;
import com.notasapp.repository.AdjuntoRepository;
import com.notasapp.repository.NotaRepository;
import com.notasapp.repository.RefreshTokenRepository;
import com.notasapp.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetentionScheduler {

    private final NotaRepository notaRepository;
    private final AdjuntoRepository adjuntoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StorageService storageService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeDeletedContent() {
        Instant now = Instant.now();
        for (Adjunto adjunto : adjuntoRepository.findByDeletedAtBefore(now.minus(7, ChronoUnit.DAYS))) {
            storageService.delete(adjunto.getStorageKey());
            adjuntoRepository.delete(adjunto);
        }
        for (Nota nota : notaRepository.findByDeletedAtBefore(now.minus(30, ChronoUnit.DAYS))) {
            for (Adjunto adjunto : adjuntoRepository.findAllByNotaId(nota.getId())) {
                storageService.delete(adjunto.getStorageKey());
                adjuntoRepository.delete(adjunto);
            }
            notaRepository.delete(nota);
        }
        refreshTokenRepository.purgeExpired(now.minus(30, ChronoUnit.DAYS));
        log.info("Retención ejecutada");
    }
}
