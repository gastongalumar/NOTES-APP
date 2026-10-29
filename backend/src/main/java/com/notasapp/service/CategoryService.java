package com.notasapp.service;

import com.notasapp.dto.category.CategoryRequest;
import com.notasapp.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> list(Long usuarioId);

    CategoryResponse get(Long id, Long usuarioId);

    CategoryResponse create(Long usuarioId, CategoryRequest request);

    CategoryResponse update(Long id, Long usuarioId, CategoryRequest request);

    void delete(Long id, Long usuarioId);
}
