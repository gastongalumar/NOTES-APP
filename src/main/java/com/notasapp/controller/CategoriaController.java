package com.notasapp.controller;

import com.notasapp.dto.CategoriaDTO;
import com.notasapp.model.Usuario;
import com.notasapp.service.I_CategoriaService;
import com.notasapp.service.I_UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
//@CrossOrigin(origins = "*")
public class CategoriaController {

    private final I_CategoriaService categoriaService;
    private final I_UsuarioService usuarioService;

    public CategoriaController(I_CategoriaService categoriaService,
                               I_UsuarioService usuarioService) {
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
    }

    private Usuario obtenerUsuarioActual(UserDetails userDetails) {
        return usuarioService.obtenerUsuarioPorUsername(userDetails.getUsername());
    }

    // GET: Listar todas las categorías del usuario
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarCategorias(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        List<CategoriaDTO> categorias = categoriaService.obtenerCategorias(usuario);
        return ResponseEntity.ok(categorias);
    }

    // GET: Obtener categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> obtenerCategoria(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        CategoriaDTO categoria = categoriaService.obtenerCategoria(id, usuario);
        return ResponseEntity.ok(categoria);
    }

    // POST: Crear nueva categoría
    @PostMapping
    public ResponseEntity<CategoriaDTO> crearCategoria(@RequestBody CategoriaDTO categoriaDTO,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        CategoriaDTO categoriaCreada = categoriaService.crearCategoria(categoriaDTO, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaCreada);
    }

    // PUT: Actualizar categoría existente
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(@PathVariable Long id,
                                                            @RequestBody CategoriaDTO categoriaDTO,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        CategoriaDTO categoriaActualizada = categoriaService.actualizarCategoria(id, categoriaDTO, usuario);
        return ResponseEntity.ok(categoriaActualizada);
    }

    // DELETE: Eliminar categoría
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        categoriaService.eliminarCategoria(id, usuario);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Categoría eliminada exitosamente");
        return ResponseEntity.ok(response);
    }

    // GET: Verificar si existe categoría por nombre
    @GetMapping("/existe/{nombre}")
    public ResponseEntity<Map<String, Boolean>> existeCategoria(@PathVariable String nombre,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        boolean existe = categoriaService.existeCategoria(nombre, usuario);

        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", existe);
        return ResponseEntity.ok(response);
    }
}