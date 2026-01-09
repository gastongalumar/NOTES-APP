package com.notasapp.service.impl;

import com.notasapp.dto.CategoriaDTO;
import com.notasapp.exception.ResourceNotFoundException;
import com.notasapp.model.Categoria;
import com.notasapp.model.Usuario;
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
    public CategoriaDTO crearCategoria(CategoriaDTO categoriaDTO, Usuario usuario) {
        // Verificar si ya existe una categoría con ese nombre para este usuario
        if (categoriaRepository.existsByUsuarioAndNombre(usuario, categoriaDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setColor(categoriaDTO.getColor());
        categoria.setUsuario(usuario);

        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return convertirACategoriaDTO(categoriaGuardada);
    }

    @Override
    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO categoriaDTO, Usuario usuario) {
        Categoria categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Verificar si el nuevo nombre ya existe (excluyendo la categoría actual)
        if (!categoria.getNombre().equals(categoriaDTO.getNombre()) &&
                categoriaRepository.existsByUsuarioAndNombre(usuario, categoriaDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setColor(categoriaDTO.getColor());

        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        return convertirACategoriaDTO(categoriaActualizada);
    }

    @Override
    public void eliminarCategoria(Long id, Usuario usuario) {
        Categoria categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Verificar si la categoría tiene notas asociadas
        if (!categoria.getNotas().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la categoría porque tiene notas asociadas");
        }

        categoriaRepository.delete(categoria);
    }

    @Override
    public List<CategoriaDTO> obtenerCategorias(Usuario usuario) {
        return categoriaRepository.findByUsuario(usuario)
                .stream()
                .map(this::convertirACategoriaDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriaDTO obtenerCategoria(Long id, Usuario usuario) {
        Categoria categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        return convertirACategoriaDTO(categoria);
    }

    @Override
    public boolean existeCategoria(String nombre, Usuario usuario) {
        return categoriaRepository.existsByUsuarioAndNombre(usuario, nombre);
    }

    // Método privado para convertir Categoria a CategoriaDTO
    private CategoriaDTO convertirACategoriaDTO(Categoria categoria) {
        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setId(categoria.getId());
        categoriaDTO.setNombre(categoria.getNombre());
        categoriaDTO.setColor(categoria.getColor());
        return categoriaDTO;
    }
}
