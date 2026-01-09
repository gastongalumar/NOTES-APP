package com.notasapp.service;

import com.notasapp.dto.CategoriaDTO;
import java.util.List;

public interface I_CategoriaService {

    // ❌ ELIMINADO: parámetro Usuario
    CategoriaDTO crearCategoria(CategoriaDTO categoriaDTO);

    CategoriaDTO actualizarCategoria(Long id, CategoriaDTO categoriaDTO);

    void eliminarCategoria(Long id);

    List<CategoriaDTO> obtenerTodasLasCategorias();

    CategoriaDTO obtenerCategoriaPorId(Long id);

    CategoriaDTO obtenerCategoriaPorNombre(String nombre);

    boolean existeCategoria(String nombre);

    // ✅ NUEVOS MÉTODOS (opcionales):
    List<CategoriaDTO> buscarCategorias(String keyword);

    List<CategoriaDTO> obtenerCategoriasPorColor(String color);
}