package com.notasapp.service;

import com.notasapp.dto.tag.TagRequest;
import com.notasapp.dto.tag.TagResponse;

import java.util.List;

public interface TagService {
    List<TagResponse> list(Long usuarioId);

    TagResponse create(Long usuarioId, TagRequest request);

    TagResponse update(Long id, Long usuarioId, TagRequest request);

    void delete(Long id, Long usuarioId);
}
