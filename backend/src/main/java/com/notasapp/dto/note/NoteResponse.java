package com.notasapp.dto.note;

import com.notasapp.dto.NamedRef;
import com.notasapp.model.NotaEstado;

import java.time.Instant;
import java.util.List;

public record NoteResponse(
        Long id,
        String titulo,
        String contenido,
        boolean archivada,
        long version,
        Instant fechaVencimiento,
        int prioridad,
        NotaEstado estado,
        List<NamedRef> categorias,
        List<NamedRef> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
