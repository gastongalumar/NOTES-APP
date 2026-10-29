package com.notasapp.repository;

import com.notasapp.model.Nota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    @EntityGraph(attributePaths = {"categorias", "tags"})
    Optional<Nota> findByIdAndUsuarioIdAndDeletedAtIsNull(Long id, Long usuarioId);

    @EntityGraph(attributePaths = {"categorias", "tags"})
    @Query("select distinct n from Nota n left join n.categorias c left join n.tags t " +
            "where n.usuario.id = :usuarioId and n.deletedAt is null " +
            "and (:archived is null or n.archivada = :archived) " +
            "and (:q is null or lower(n.titulo) like lower(concat('%', :q, '%')) " +
            "     or lower(n.contenido) like lower(concat('%', :q, '%'))) " +
            "and (:categoryId is null or c.id = :categoryId) " +
            "and (:tagId is null or t.id = :tagId)")
    Page<Nota> searchLike(@Param("usuarioId") Long usuarioId,
                          @Param("archived") Boolean archived,
                          @Param("q") String q,
                          @Param("categoryId") Long categoryId,
                          @Param("tagId") Long tagId,
                          Pageable pageable);

    @Query(value = """
            SELECT n.* FROM notas n
            WHERE n.usuario_id = :usuarioId
              AND n.deleted_at IS NULL
              AND (:archived IS NULL OR n.archivada = :archived)
              AND MATCH(n.titulo, n.contenido) AGAINST (:q IN BOOLEAN MODE)
              AND (:categoryId IS NULL OR EXISTS (
                    SELECT 1 FROM nota_categoria nc
                    WHERE nc.nota_id = n.id AND nc.categoria_id = :categoryId))
              AND (:tagId IS NULL OR EXISTS (
                    SELECT 1 FROM nota_tag nt
                    WHERE nt.nota_id = n.id AND nt.tag_id = :tagId))
            ORDER BY n.updated_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM notas n
            WHERE n.usuario_id = :usuarioId
              AND n.deleted_at IS NULL
              AND (:archived IS NULL OR n.archivada = :archived)
              AND MATCH(n.titulo, n.contenido) AGAINST (:q IN BOOLEAN MODE)
              AND (:categoryId IS NULL OR EXISTS (
                    SELECT 1 FROM nota_categoria nc
                    WHERE nc.nota_id = n.id AND nc.categoria_id = :categoryId))
              AND (:tagId IS NULL OR EXISTS (
                    SELECT 1 FROM nota_tag nt
                    WHERE nt.nota_id = n.id AND nt.tag_id = :tagId))
            """,
            nativeQuery = true)
    Page<Nota> searchFullText(@Param("usuarioId") Long usuarioId,
                              @Param("archived") Boolean archived,
                              @Param("q") String q,
                              @Param("categoryId") Long categoryId,
                              @Param("tagId") Long tagId,
                              Pageable pageable);

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndArchivadaTrue();

    @Query("select count(n) from Nota n where n.usuario.id = :usuarioId and n.deletedAt is null")
    long countActive(@Param("usuarioId") Long usuarioId);

    @Query("select count(n) from Nota n where n.usuario.id = :usuarioId and n.deletedAt is null and n.archivada = true")
    long countArchived(@Param("usuarioId") Long usuarioId);

    @EntityGraph(attributePaths = {"categorias", "tags"})
    @Query("select n from Nota n where n.usuario.id = :usuarioId and n.deletedAt is null " +
            "and n.fechaVencimiento is not null and n.fechaVencimiento between :from and :to order by n.fechaVencimiento asc")
    List<Nota> findAgenda(@Param("usuarioId") Long usuarioId,
                          @Param("from") Instant from,
                          @Param("to") Instant to);

    List<Nota> findByDeletedAtBefore(Instant cutoff);
}
