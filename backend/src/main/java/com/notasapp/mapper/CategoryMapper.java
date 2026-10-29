package com.notasapp.mapper;

import com.notasapp.dto.category.CategoryResponse;
import com.notasapp.model.Categoria;
import com.notasapp.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final CategoriaRepository categoriaRepository;

    public CategoryResponse toResponse(Categoria categoria) {
        return new CategoryResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getColor(),
                categoria.getDescripcion(),
                categoriaRepository.countNotes(categoria.getId())
        );
    }
}
