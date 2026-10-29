package com.notasapp.repository;

import com.notasapp.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByUsuarioIdAndDeletedAtIsNullOrderByNombreAsc(Long usuarioId);

    Optional<Categoria> findByIdAndUsuarioIdAndDeletedAtIsNull(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndNombreIgnoreCaseAndDeletedAtIsNull(Long usuarioId, String nombre);

    Set<Categoria> findAllByIdInAndUsuarioIdAndDeletedAtIsNull(Set<Long> ids, Long usuarioId);

    @Query("select count(n) from Nota n join n.categorias c where c.id = :categoriaId and n.deletedAt is null")
    long countNotes(@Param("categoriaId") Long categoriaId);
}
