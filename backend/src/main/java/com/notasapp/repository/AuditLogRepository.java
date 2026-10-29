package com.notasapp.repository;

import com.notasapp.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("select a from AuditLog a where " +
            "(:usuarioId is null or a.usuario.id = :usuarioId) and " +
            "(:accion is null or a.accion = :accion) and " +
            "(:desde is null or a.createdAt >= :desde) and " +
            "(:hasta is null or a.createdAt <= :hasta)")
    Page<AuditLog> search(@Param("usuarioId") Long usuarioId,
                          @Param("accion") String accion,
                          @Param("desde") Instant desde,
                          @Param("hasta") Instant hasta,
                          Pageable pageable);

    long countByAccion(String accion);
}
