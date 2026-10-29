package com.notasapp.service.impl;

import com.notasapp.dto.category.CategoryRequest;
import com.notasapp.dto.category.CategoryResponse;
import com.notasapp.exception.ConflictException;
import com.notasapp.exception.NotFoundException;
import com.notasapp.mapper.CategoryMapper;
import com.notasapp.model.Categoria;
import com.notasapp.model.Usuario;
import com.notasapp.repository.CategoriaRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.service.AuditService;
import com.notasapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoryMapper categoryMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long usuarioId) {
        return categoriaRepository.findAllByUsuarioIdAndDeletedAtIsNullOrderByNombreAsc(usuarioId)
                .stream().map(categoryMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse get(Long id, Long usuarioId) {
        return categoryMapper.toResponse(findOwned(id, usuarioId));
    }

    @Override
    @Transactional
    public CategoryResponse create(Long usuarioId, CategoryRequest request) {
        Usuario usuario = findUser(usuarioId);
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByUsuarioIdAndNombreIgnoreCaseAndDeletedAtIsNull(usuarioId, nombre)) {
            throw new ConflictException("CATEGORY_EXISTS", "Ya existe una categoría con ese nombre");
        }
        Categoria categoria = new Categoria();
        categoria.setUsuario(usuario);
        categoria.setNombre(nombre);
        categoria.setColor(request.color().toUpperCase());
        categoria.setDescripcion(request.descripcion());
        Categoria saved = categoriaRepository.save(categoria);
        auditService.log("CATEGORY_CREATE", usuarioId, "CATEGORIA", saved.getId(), null, null);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, Long usuarioId, CategoryRequest request) {
        Categoria categoria = findOwned(id, usuarioId);
        String nombre = request.nombre().trim();
        if (!categoria.getNombre().equalsIgnoreCase(nombre)
                && categoriaRepository.existsByUsuarioIdAndNombreIgnoreCaseAndDeletedAtIsNull(usuarioId, nombre)) {
            throw new ConflictException("CATEGORY_EXISTS", "Ya existe una categoría con ese nombre");
        }
        categoria.setNombre(nombre);
        categoria.setColor(request.color().toUpperCase());
        categoria.setDescripcion(request.descripcion());
        Categoria saved = categoriaRepository.save(categoria);
        auditService.log("CATEGORY_UPDATE", usuarioId, "CATEGORIA", saved.getId(), null, null);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, Long usuarioId) {
        Categoria categoria = findOwned(id, usuarioId);
        if (categoriaRepository.countNotes(id) > 0) {
            throw new ConflictException("CATEGORY_HAS_NOTES", "No se puede borrar una categoría con notas");
        }
        categoria.setDeletedAt(Instant.now());
        categoriaRepository.save(categoria);
        auditService.log("CATEGORY_DELETE", usuarioId, "CATEGORIA", id, null, null);
    }

    private Categoria findOwned(Long id, Long usuarioId) {
        return categoriaRepository.findByIdAndUsuarioIdAndDeletedAtIsNull(id, usuarioId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
    }

    private Usuario findUser(Long usuarioId) {
        return usuarioRepository.findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
}
