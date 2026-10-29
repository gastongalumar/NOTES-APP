package com.notasapp.service.impl;

import com.notasapp.model.Notificacion;
import com.notasapp.model.Recordatorio;
import com.notasapp.repository.NotificacionRepository;
import com.notasapp.repository.RecordatorioRepository;
import com.notasapp.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final RecordatorioRepository recordatorioRepository;
    private final NotificacionRepository notificacionRepository;

    @Override
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional
    public void processDueReminders() {
        List<Recordatorio> due = recordatorioRepository
                .findByEstadoAndRecordarAtLessThanEqualOrderByRecordarAtAsc(
                        "PENDIENTE", Instant.now(), PageRequest.of(0, 50));
        for (Recordatorio reminder : due) {
            if (reminder.getNota().getDeletedAt() != null) {
                reminder.setEstado("CANCELADO");
                recordatorioRepository.save(reminder);
                continue;
            }
            Notificacion notification = new Notificacion();
            notification.setUsuario(reminder.getUsuario());
            notification.setTipo("RECORDATORIO");
            notification.setPayload("{\"notaId\":" + reminder.getNota().getId() + "}");
            notification.setCreatedAt(Instant.now());
            notificacionRepository.save(notification);
            reminder.setEnviadoAt(Instant.now());
            reminder.setEstado("ENVIADO");
            recordatorioRepository.save(reminder);
        }
        if (!due.isEmpty()) {
            log.info("Recordatorios procesados: {}", due.size());
        }
    }
}
