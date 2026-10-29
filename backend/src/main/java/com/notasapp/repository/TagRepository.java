package com.notasapp.repository;

import com.notasapp.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByUsuarioIdAndDeletedAtIsNullOrderByNombreAsc(Long usuarioId);

    Optional<Tag> findByIdAndUsuarioIdAndDeletedAtIsNull(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndNombreIgnoreCaseAndDeletedAtIsNull(Long usuarioId, String nombre);

    Set<Tag> findAllByIdInAndUsuarioIdAndDeletedAtIsNull(Set<Long> ids, Long usuarioId);
}
