package com.notasapp.repository;

import com.notasapp.model.Recordatorio;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {

    List<Recordatorio> findByEstadoAndRecordarAtLessThanEqualOrderByRecordarAtAsc(
            String estado, Instant now, Pageable pageable);

    void deleteByNotaIdAndEstado(Long notaId, String estado);
}
