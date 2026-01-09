package com.notasapp.controller;

import com.notasapp.dto.NotaDTO;
import com.notasapp.model.Usuario;
import com.notasapp.service.I_NotaService;
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
@RequestMapping("/api/notas")
//@CrossOrigin(origins = "*")
public class NotaController {

    private final I_NotaService notaService;
    private final I_UsuarioService usuarioService;

    public NotaController(I_NotaService notaService, I_UsuarioService usuarioService) {
        this.notaService = notaService;
        this.usuarioService = usuarioService;
    }

    private Usuario obtenerUsuarioActual(UserDetails userDetails) {
        return usuarioService.obtenerUsuarioPorUsername(userDetails.getUsername());
    }

    // GET: Listar todas las notas activas
    @GetMapping
    public ResponseEntity<List<NotaDTO>> listarNotasActivas(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        List<NotaDTO> notas = notaService.listarNotasActivas(usuario);
        return ResponseEntity.ok(notas);
    }

    // GET: Listar notas archivadas
    @GetMapping("/archivadas")
    public ResponseEntity<List<NotaDTO>> listarNotasArchivadas(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        List<NotaDTO> notas = notaService.listarNotasArchivadas(usuario);
        return ResponseEntity.ok(notas);
    }

    // GET: Obtener nota por ID
    @GetMapping("/{id}")
    public ResponseEntity<NotaDTO> obtenerNota(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        NotaDTO nota = notaService.obtenerNota(id, usuario);
        return ResponseEntity.ok(nota);
    }

    // POST: Crear nueva nota
    @PostMapping
    public ResponseEntity<NotaDTO> crearNota(@RequestBody NotaDTO notaDTO,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        NotaDTO notaCreada = notaService.crearNota(notaDTO, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(notaCreada);
    }

    // PUT: Actualizar nota existente
    @PutMapping("/{id}")
    public ResponseEntity<NotaDTO> actualizarNota(@PathVariable Long id,
                                                  @RequestBody NotaDTO notaDTO,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        NotaDTO notaActualizada = notaService.actualizarNota(id, notaDTO, usuario);
        return ResponseEntity.ok(notaActualizada);
    }

    // DELETE: Eliminar nota
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarNota(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        notaService.eliminarNota(id, usuario);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Nota eliminada exitosamente");
        return ResponseEntity.ok(response);
    }

    // PATCH: Archivar nota
    @PatchMapping("/{id}/archivar")
    public ResponseEntity<NotaDTO> archivarNota(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        NotaDTO notaArchivada = notaService.archivarNota(id, usuario);
        return ResponseEntity.ok(notaArchivada);
    }

    // PATCH: Desarchivar nota
    @PatchMapping("/{id}/desarchivar")
    public ResponseEntity<NotaDTO> desarchivarNota(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        NotaDTO notaDesarchivada = notaService.desarchivarNota(id, usuario);
        return ResponseEntity.ok(notaDesarchivada);
    }

    // GET: Filtrar notas por categoría
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<NotaDTO>> filtrarPorCategoria(@PathVariable Long categoriaId,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = obtenerUsuarioActual(userDetails);
        List<NotaDTO> notas = notaService.filtrarNotasPorCategoria(categoriaId, usuario);
        return ResponseEntity.ok(notas);
    }
}