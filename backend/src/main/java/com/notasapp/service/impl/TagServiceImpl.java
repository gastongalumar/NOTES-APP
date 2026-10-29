package com.notasapp.service.impl;

import com.notasapp.dto.tag.TagRequest;
import com.notasapp.dto.tag.TagResponse;
import com.notasapp.exception.ConflictException;
import com.notasapp.exception.NotFoundException;
import com.notasapp.mapper.TagMapper;
import com.notasapp.model.Tag;
import com.notasapp.model.Usuario;
import com.notasapp.repository.TagRepository;
import com.notasapp.repository.UsuarioRepository;
import com.notasapp.service.AuditService;
import com.notasapp.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UsuarioRepository usuarioRepository;
    private final TagMapper tagMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> list(Long usuarioId) {
        return tagRepository.findAllByUsuarioIdAndDeletedAtIsNullOrderByNombreAsc(usuarioId)
                .stream().map(tagMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public TagResponse create(Long usuarioId, TagRequest request) {
        Usuario usuario = findUser(usuarioId);
        String nombre = request.nombre().trim();
        if (tagRepository.existsByUsuarioIdAndNombreIgnoreCaseAndDeletedAtIsNull(usuarioId, nombre)) {
            throw new ConflictException("TAG_EXISTS", "Ya existe un tag con ese nombre");
        }
        Tag tag = new Tag();
        tag.setUsuario(usuario);
        tag.setNombre(nombre);
        Tag saved = tagRepository.save(tag);
        auditService.log("TAG_CREATE", usuarioId, "TAG", saved.getId(), null, null);
        return tagMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TagResponse update(Long id, Long usuarioId, TagRequest request) {
        Tag tag = findOwned(id, usuarioId);
        String nombre = request.nombre().trim();
        if (!tag.getNombre().equalsIgnoreCase(nombre)
                && tagRepository.existsByUsuarioIdAndNombreIgnoreCaseAndDeletedAtIsNull(usuarioId, nombre)) {
            throw new ConflictException("TAG_EXISTS", "Ya existe un tag con ese nombre");
        }
        tag.setNombre(nombre);
        Tag saved = tagRepository.save(tag);
        auditService.log("TAG_UPDATE", usuarioId, "TAG", saved.getId(), null, null);
        return tagMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, Long usuarioId) {
        Tag tag = findOwned(id, usuarioId);
        tag.getNotas().forEach(nota -> nota.getTags().remove(tag));
        tag.setDeletedAt(Instant.now());
        tagRepository.save(tag);
        auditService.log("TAG_DELETE", usuarioId, "TAG", id, null, null);
    }

    private Tag findOwned(Long id, Long usuarioId) {
        return tagRepository.findByIdAndUsuarioIdAndDeletedAtIsNull(id, usuarioId)
                .orElseThrow(() -> new NotFoundException("Tag no encontrado"));
    }

    private Usuario findUser(Long usuarioId) {
        return usuarioRepository.findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
}
