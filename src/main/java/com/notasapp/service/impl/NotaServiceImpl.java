package com.notasapp.service.impl;

import com.notasapp.dto.NotaDTO;
import com.notasapp.exception.ResourceNotFoundException;
import com.notasapp.model.Categoria;
import com.notasapp.model.Nota;
import com.notasapp.model.Usuario;
import com.notasapp.repository.I_CategoriaRepository;
import com.notasapp.repository.I_NotaRepository;
import com.notasapp.service.I_NotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotaServiceImpl implements I_NotaService {

    private final I_NotaRepository notaRepository;
    private final I_CategoriaRepository categoriaRepository;

    public NotaServiceImpl(I_NotaRepository notaRepository,
                           I_CategoriaRepository categoriaRepository) {
        this.notaRepository = notaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public NotaDTO crearNota(NotaDTO notaDTO, Usuario usuario) {
        Nota nota = new Nota();
        nota.setTitulo(notaDTO.getTitulo());
        nota.setContenido(notaDTO.getContenido());
        nota.setUsuario(usuario);
        nota.setArchivada(notaDTO.isArchivada());

        // Asociar categorías si existen
        if (notaDTO.getCategoriaIds() != null && !notaDTO.getCategoriaIds().isEmpty()) {
            Set<Categoria> categorias = new HashSet<>();
            for (Long categoriaId : notaDTO.getCategoriaIds()) {
                Categoria categoria = categoriaRepository.findById(categoriaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
                categorias.add(categoria);
            }
            nota.setCategorias(categorias);
        }

        Nota notaGuardada = notaRepository.save(nota);
        return convertirANotaDTO(notaGuardada);
    }

    @Override
    public NotaDTO actualizarNota(Long id, NotaDTO notaDTO, Usuario usuario) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada"));

        // Verificar que la nota pertenece al usuario
        if (!nota.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para actualizar esta nota");
        }

        nota.setTitulo(notaDTO.getTitulo());
        nota.setContenido(notaDTO.getContenido());
        nota.setArchivada(notaDTO.isArchivada());

        // Actualizar categorías
        if (notaDTO.getCategoriaIds() != null) {
            Set<Categoria> categorias = new HashSet<>();
            for (Long categoriaId : notaDTO.getCategoriaIds()) {
                Categoria categoria = categoriaRepository.findById(categoriaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
                categorias.add(categoria);
            }
            nota.setCategorias(categorias);
        }

        Nota notaActualizada = notaRepository.save(nota);
        return convertirANotaDTO(notaActualizada);
    }

    @Override
    public void eliminarNota(Long id, Usuario usuario) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada"));

        if (!nota.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar esta nota");
        }

        notaRepository.delete(nota);
    }

    @Override
    public NotaDTO obtenerNota(Long id, Usuario usuario) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada"));

        if (!nota.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para ver esta nota");
        }

        return convertirANotaDTO(nota);
    }

    @Override
    public List<NotaDTO> listarNotasActivas(Usuario usuario) {
        return notaRepository.findByUsuarioAndArchivadaFalse(usuario)
                .stream()
                .map(this::convertirANotaDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotaDTO> listarNotasArchivadas(Usuario usuario) {
        return notaRepository.findByUsuarioAndArchivadaTrue(usuario)
                .stream()
                .map(this::convertirANotaDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotaDTO> listarTodasNotas(Usuario usuario) {
        return notaRepository.findByUsuario(usuario)
                .stream()
                .map(this::convertirANotaDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotaDTO archivarNota(Long id, Usuario usuario) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada"));

        if (!nota.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para archivar esta nota");
        }

        nota.setArchivada(true);
        Nota notaArchivada = notaRepository.save(nota);
        return convertirANotaDTO(notaArchivada);
    }

    @Override
    public NotaDTO desarchivarNota(Long id, Usuario usuario) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada"));

        if (!nota.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para desarchivar esta nota");
        }

        nota.setArchivada(false);
        Nota notaDesarchivada = notaRepository.save(nota);
        return convertirANotaDTO(notaDesarchivada);
    }

    @Override
    public List<NotaDTO> filtrarNotasPorCategoria(Long categoriaId, Usuario usuario) {
        return notaRepository.findByUsuarioAndCategoria(usuario, categoriaId)
                .stream()
                .map(this::convertirANotaDTO)
                .collect(Collectors.toList());
    }

    // Método privado para convertir Nota a NotaDTO
    private NotaDTO convertirANotaDTO(Nota nota) {
        NotaDTO notaDTO = new NotaDTO();
        notaDTO.setId(nota.getId());
        notaDTO.setTitulo(nota.getTitulo());
        notaDTO.setContenido(nota.getContenido());
        notaDTO.setFechaCreacion(nota.getFechaCreacion());
        notaDTO.setFechaActualizacion(nota.getFechaActualizacion());
        notaDTO.setArchivada(nota.isArchivada());

        // Convertir categorías a IDs
        Set<Long> categoriaIds = nota.getCategorias()
                .stream()
                .map(Categoria::getId)
                .collect(Collectors.toSet());
        notaDTO.setCategoriaIds(categoriaIds);

        return notaDTO;
    }
}