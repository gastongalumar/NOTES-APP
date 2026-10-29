package com.notasapp.service;

import com.notasapp.dto.PageResponse;
import com.notasapp.dto.note.*;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface NoteService {

    PageResponse<NoteSummaryResponse> list(Long usuarioId, Boolean archived, String q,
                                           Long categoryId, Long tagId, Pageable pageable);

    NoteResponse get(Long id, Long usuarioId);

    NoteResponse create(Long usuarioId, NoteCreateRequest request);

    NoteResponse update(Long id, Long usuarioId, NoteUpdateRequest request);

    void delete(Long id, Long usuarioId);

    NoteResponse archive(Long id, Long usuarioId);

    NoteResponse unarchive(Long id, Long usuarioId);

    NoteResponse updateAgenda(Long id, Long usuarioId, AgendaUpdateRequest request);

    List<NoteSummaryResponse> agenda(Long usuarioId, Instant from, Instant to);
}
