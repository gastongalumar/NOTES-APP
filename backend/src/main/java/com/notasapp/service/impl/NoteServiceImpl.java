package com.notasapp.service.impl;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.note.*;
import com.notasapp.exception.ConflictException;
import com.notasapp.exception.NotFoundException;
import com.notasapp.mapper.NoteMapper;
import com.notasapp.model.Categoria;
import com.notasapp.model.Nota;
import com.notasapp.model.NotaEstado;
import com.notasapp.model.Recordatorio;
import com.notasapp.model.Tag;
import com.notasapp.model.Usuario;
import com.notasapp.repository.CategoriaRepository;
import com.notasapp.repository.NotaRepository;
import com.notasapp.repository.RecordatorioRepository;
import com.notasapp.repository.TagRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.service.AuditService;
import com.notasapp.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NotaRepository notaRepository;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final NoteMapper noteMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoteSummaryResponse> list(Long usuarioId, Boolean archived, String q,
                                                  Long categoryId, Long tagId, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return PageResponse.from(
                notaRepository.search(usuarioId, archived, query, categoryId, tagId, pageable),
                noteMapper::toSummary
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse get(Long id, Long usuarioId) {
        return noteMapper.toResponse(findOwned(id, usuarioId));
    }

    @Override
    @Transactional
    public NoteResponse create(Long usuarioId, NoteCreateRequest request) {
        Usuario usuario = findUser(usuarioId);
        Nota nota = new Nota();
        nota.setUsuario(usuario);
        apply(nota, request.titulo(), request.contenido(), request.fechaVencimiento(),
                request.prioridad(), request.estado());
        nota.setCategorias(resolveCategories(request.categoriaIds(), usuarioId));
        nota.setTags(resolveTags(request.tagIds(), usuarioId));
        Nota saved = notaRepository.save(nota);
        upsertReminder(saved);
        auditService.log("NOTE_CREATE", usuarioId, "NOTA", saved.getId(), null, null);
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NoteResponse update(Long id, Long usuarioId, NoteUpdateRequest request) {
        Nota nota = findOwned(id, usuarioId);
        if (nota.getVersion() != request.version()) {
            throw new ConflictException("CONFLICT", "La nota fue modificada por otra sesión");
        }
        apply(nota, request.titulo(), request.contenido(), request.fechaVencimiento(),
                request.prioridad(), request.estado());
        nota.setCategorias(resolveCategories(request.categoriaIds(), usuarioId));
        nota.setTags(resolveTags(request.tagIds(), usuarioId));
        Nota saved = notaRepository.save(nota);
        upsertReminder(saved);
        auditService.log("NOTE_UPDATE", usuarioId, "NOTA", saved.getId(), null, null);
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, Long usuarioId) {
        Nota nota = findOwned(id, usuarioId);
        nota.setDeletedAt(Instant.now());
        notaRepository.save(nota);
        recordatorioRepository.deleteByNotaIdAndEstado(id, "PENDIENTE");
        auditService.log("NOTE_DELETE", usuarioId, "NOTA", id, null, null);
    }

    @Override
    @Transactional
    public NoteResponse archive(Long id, Long usuarioId) {
        Nota nota = findOwned(id, usuarioId);
        nota.setArchivada(true);
        return noteMapper.toResponse(notaRepository.save(nota));
    }

    @Override
    @Transactional
    public NoteResponse unarchive(Long id, Long usuarioId) {
        Nota nota = findOwned(id, usuarioId);
        nota.setArchivada(false);
        return noteMapper.toResponse(notaRepository.save(nota));
    }

    @Override
    @Transactional
    public NoteResponse updateAgenda(Long id, Long usuarioId, AgendaUpdateRequest request) {
        Nota nota = findOwned(id, usuarioId);
        nota.setFechaVencimiento(request.fechaVencimiento());
        if (request.prioridad() != null) {
            nota.setPrioridad(request.prioridad());
        }
        if (request.estado() != null) {
            nota.setEstado(request.estado());
        }
        return noteMapper.toResponse(notaRepository.save(nota));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteSummaryResponse> agenda(Long usuarioId, Instant from, Instant to) {
        return notaRepository.findAgenda(usuarioId, from, to).stream()
                .map(noteMapper::toSummary)
                .toList();
    }

    private void apply(Nota nota, String titulo, String contenido, Instant fechaVencimiento,
                       Integer prioridad, NotaEstado estado) {
        nota.setTitulo(titulo.trim());
        nota.setContenido(contenido);
        nota.setFechaVencimiento(fechaVencimiento);
        nota.setPrioridad(prioridad == null ? 2 : prioridad);
        nota.setEstado(estado == null ? NotaEstado.PENDIENTE : estado);
    }

    private Set<Categoria> resolveCategories(Set<Long> ids, Long usuarioId) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        Set<Categoria> categorias = categoriaRepository.findAllByIdInAndUsuarioIdAndDeletedAtIsNull(ids, usuarioId);
        if (categorias.size() != ids.size()) {
            throw new NotFoundException("Alguna categoría no existe");
        }
        return categorias;
    }

    private Set<Tag> resolveTags(Set<Long> ids, Long usuarioId) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = tagRepository.findAllByIdInAndUsuarioIdAndDeletedAtIsNull(ids, usuarioId);
        if (tags.size() != ids.size()) {
            throw new NotFoundException("Algún tag no existe");
        }
        return tags;
    }

    private void upsertReminder(Nota nota) {
        recordatorioRepository.deleteByNotaIdAndEstado(nota.getId(), "PENDIENTE");
        if (nota.getFechaVencimiento() == null) {
            return;
        }
        Instant remindAt = nota.getFechaVencimiento().minusSeconds(3600);
        if (remindAt.isBefore(Instant.now())) {
            remindAt = Instant.now().plusSeconds(60);
        }
        Recordatorio recordatorio = new Recordatorio();
        recordatorio.setNota(nota);
        recordatorio.setUsuario(nota.getUsuario());
        recordatorio.setRecordarAt(remindAt);
        recordatorio.setEstado("PENDIENTE");
        recordatorio.setCreatedAt(Instant.now());
        recordatorioRepository.save(recordatorio);
    }

    private Nota findOwned(Long id, Long usuarioId) {
        return notaRepository.findByIdAndUsuarioIdAndDeletedAtIsNull(id, usuarioId)
                .orElseThrow(() -> new NotFoundException("Nota no encontrada"));
    }

    private Usuario findUser(Long usuarioId) {
        return usuarioRepository.findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
}
