package com.notasapp.controller;

import com.notasapp.dto.category.CategoryRequest;
import com.notasapp.dto.category.CategoryResponse;
import com.notasapp.security.JwtPrincipal;
import com.notasapp.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(categoryService.list(principal.userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> get(@AuthenticationPrincipal JwtPrincipal principal,
                                                @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.get(id, principal.userId()));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal JwtPrincipal principal,
                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(principal.userId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@AuthenticationPrincipal JwtPrincipal principal,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, principal.userId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal JwtPrincipal principal,
                                       @PathVariable Long id) {
        categoryService.delete(id, principal.userId());
        return ResponseEntity.noContent().build();
    }
}
