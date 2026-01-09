package com.notasapp.service.impl;

import com.notasapp.dto.CategoriaDTO;
import com.notasapp.exception.ResourceNotFoundException;
import com.notasapp.model.Categoria;
import com.notasapp.repository.I_CategoriaRepository;
import com.notasapp.service.I_CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaServiceImpl implements I_CategoriaService {

    private final I_CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(I_CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public CategoriaDTO crearCategoria(CategoriaDTO categoriaDTO) {
        // Verificar si ya existe una categoría con ese nombre (global)
        if (categoriaRepository.existsByNombreIgnoreCase(categoriaDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + categoriaDTO.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setColor(categoriaDTO.getColor());

        // Si tu DTO tiene descripción, la asignamos
        if (categoriaDTO.getDescripcion() != null) {
            categoria.setDescripcion(categoriaDTO.getDescripcion());
        }

        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return convertirACategoriaDTO(categoriaGuardada);
    }

    @Override
    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO categoriaDTO) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Verificar si el nuevo nombre ya existe (excluyendo la categoría actual)
        if (!categoria.getNombre().equalsIgnoreCase(categoriaDTO.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(categoriaDTO.getNombre())) {
            throw new RuntimeException("Ya existe otra categoría con el nombre: " + categoriaDTO.getNombre());
        }

        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setColor(categoriaDTO.getColor());

        // Actualizar descripción si se proporciona
        if (categoriaDTO.getDescripcion() != null) {
            categoria.setDescripcion(categoriaDTO.getDescripcion());
        }

        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        return convertirACategoriaDTO(categoriaActualizada);
    }

    @Override
    public void eliminarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Verificar si la categoría tiene notas asociadas
        if (!categoria.getNotas().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la categoría porque tiene notas asociadas");
        }

        categoriaRepository.delete(categoria);
    }

    @Override
    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::convertirACategoriaDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriaDTO obtenerCategoriaPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        return convertirACategoriaDTO(categoria);
    }

    @Override
    public CategoriaDTO obtenerCategoriaPorNombre(String nombre) {
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con nombre: " + nombre));

        return convertirACategoriaDTO(categoria);
    }

    @Override
    public boolean existeCategoria(String nombre) {
        return categoriaRepository.existsByNombreIgnoreCase(nombre);
    }

    // ✅ NUEVOS MÉTODOS (opcionales):

    @Override
    public List<CategoriaDTO> buscarCategorias(String keyword) {
        return categoriaRepository.buscarPorPalabraClave(keyword)
                .stream()
                .map(this::convertirACategoriaDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoriaDTO> obtenerCategoriasPorColor(String color) {
        return categoriaRepository.findByColor(color)
                .stream()
                .map(this::convertirACategoriaDTO)
                .collect(Collectors.toList());
    }

    // Método privado para convertir Categoria a CategoriaDTO
    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {
        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setId(categoria.getId());
        categoriaDTO.setNombre(categoria.getNombre());
        categoriaDTO.setColor(categoria.getColor());
        categoriaDTO.setDescripcion(categoria.getDescripcion());

        // Opcional: si quieres incluir conteo de notas
        if (categoria.getNotas() != null) {
            categoriaDTO.setCantidadNotas(categoria.getNotas().size());
        }

        return categoriaDTO;
    }
}