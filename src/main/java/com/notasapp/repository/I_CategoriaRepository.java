package com.notasapp.repository;

import com.notasapp.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface I_CategoriaRepository extends JpaRepository<Categoria, Long> {

    // ✅ MÉTODOS BÁSICOS PARA CATEGORÍAS GLOBALES:

    // Buscar categoría por nombre (para validaciones)
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    // Verificar si existe categoría con ese nombre
    boolean existsByNombreIgnoreCase(String nombre);

    // Buscar todas ordenadas alfabéticamente
    List<Categoria> findAllByOrderByNombreAsc();

    // ✅ AÑADE ESTOS MÉTODOS PARA EL SERVICIO:

    // Buscar categorías por palabra clave en nombre o descripción
    @Query("SELECT c FROM Categoria c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Categoria> buscarPorPalabraClave(@Param("keyword") String keyword);

    // Buscar categorías por color
    List<Categoria> findByColor(String color);
}