package com.notasapp.dto.note;

import com.notasapp.dto.NamedRef;
import com.notasapp.model.NotaEstado;

import java.time.Instant;
import java.util.List;

public record NoteSummaryResponse(
        Long id,
        String titulo,
        String extracto,
        boolean archivada,
        Instant fechaVencimiento,
        int prioridad,
        NotaEstado estado,
        List<NamedRef> categorias,
        List<NamedRef> tags,
        Instant updatedAt
) {
}
