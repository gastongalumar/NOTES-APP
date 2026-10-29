package com.notasapp.repository;

import com.notasapp.model.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    List<Adjunto> findAllByNotaIdAndUsuarioIdAndDeletedAtIsNull(Long notaId, Long usuarioId);

    Optional<Adjunto> findByIdAndUsuarioIdAndDeletedAtIsNull(Long id, Long usuarioId);

    @Query("select coalesce(sum(a.tamanoBytes), 0) from Adjunto a where a.usuario.id = :usuarioId and a.deletedAt is null")
    long totalBytesByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("select coalesce(sum(a.tamanoBytes), 0) from Adjunto a where a.nota.id = :notaId and a.deletedAt is null")
    long totalBytesByNota(@Param("notaId") Long notaId);

    List<Adjunto> findAllByNotaIdAndDeletedAtIsNull(Long notaId);

    List<Adjunto> findAllByNotaId(Long notaId);

    List<Adjunto> findByDeletedAtBefore(java.time.Instant cutoff);

    List<Adjunto> findTop20ByScanStatusAndDeletedAtIsNullOrderByCreatedAtAsc(String scanStatus);

    long countByDeletedAtIsNull();
}
