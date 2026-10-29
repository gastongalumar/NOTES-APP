package com.notasapp.repository;

import com.notasapp.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    Page<Notificacion> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId, Pageable pageable);

    Page<Notificacion> findByUsuarioIdAndLeidaAtIsNullOrderByCreatedAtDesc(Long usuarioId, Pageable pageable);

    long countByUsuarioIdAndLeidaAtIsNull(Long usuarioId);

    Optional<Notificacion> findByIdAndUsuarioId(Long id, Long usuarioId);
}
