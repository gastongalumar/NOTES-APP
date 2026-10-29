package com.notasapp.service;

import com.notasapp.dto.note.NoteResponse;
import com.notasapp.dto.share.ShareRequest;
import com.notasapp.dto.share.ShareResponse;

public interface ShareService {
    ShareResponse create(Long noteId, Long usuarioId, ShareRequest request);

    NoteResponse publicNote(String rawToken);
}
