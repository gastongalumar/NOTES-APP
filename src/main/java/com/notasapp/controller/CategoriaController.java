package com.notasapp.controller;

import com.notasapp.dto.CategoriaDTO;
import com.notasapp.service.I_CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final I_CategoriaService categoriaService;

    public CategoriaController(I_CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // GET: Listar todas las categorías (mismo endpoint)
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        List<CategoriaDTO> categorias = categoriaService.obtenerTodasLasCategorias();
        return ResponseEntity.ok(categorias);
    }

    // GET: Obtener categoría por ID (mismo endpoint)
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> obtenerCategoria(@PathVariable Long id) {
        CategoriaDTO categoria = categoriaService.obtenerCategoriaPorId(id);
        return ResponseEntity.ok(categoria);
    }

    // POST: Crear nueva categoría (mismo endpoint)
    @PostMapping
    public ResponseEntity<CategoriaDTO> crearCategoria(@RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO categoriaCreada = categoriaService.crearCategoria(categoriaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaCreada);
    }

    // PUT: Actualizar categoría existente (mismo endpoint)
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(@PathVariable Long id,
                                                            @RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO categoriaActualizada = categoriaService.actualizarCategoria(id, categoriaDTO);
        return ResponseEntity.ok(categoriaActualizada);
    }

    // DELETE: Eliminar categoría (mismo endpoint)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarCategoria(@PathVariable Long id) {
        categoriaService.eliminarCategoria(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Categoría eliminada exitosamente");
        return ResponseEntity.ok(response);
    }

    // GET: Verificar si existe categoría por nombre (mismo endpoint)
    @GetMapping("/existe/{nombre}")
    public ResponseEntity<Map<String, Boolean>> existeCategoria(@PathVariable String nombre) {
        boolean existe = categoriaService.existeCategoria(nombre);

        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", existe);
        return ResponseEntity.ok(response);
    }
}