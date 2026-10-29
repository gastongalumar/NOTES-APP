package com.notasapp.mapper;

import com.notasapp.dto.NamedRef;
import com.notasapp.dto.note.NoteResponse;
import com.notasapp.dto.note.NoteSummaryResponse;
import com.notasapp.model.Categoria;
import com.notasapp.model.Nota;
import com.notasapp.model.Tag;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class NoteMapper {

    public NoteSummaryResponse toSummary(Nota nota) {
        return new NoteSummaryResponse(
                nota.getId(),
                nota.getTitulo(),
                extracto(nota.getContenido()),
                nota.isArchivada(),
                nota.getFechaVencimiento(),
                nota.getPrioridad(),
                nota.getEstado(),
                categories(nota),
                tags(nota),
                nota.getUpdatedAt()
        );
    }

    public NoteResponse toResponse(Nota nota) {
        return new NoteResponse(
                nota.getId(),
                nota.getTitulo(),
                nota.getContenido(),
                nota.isArchivada(),
                nota.getVersion(),
                nota.getFechaVencimiento(),
                nota.getPrioridad(),
                nota.getEstado(),
                categories(nota),
                tags(nota),
                nota.getCreatedAt(),
                nota.getUpdatedAt()
        );
    }

    private List<NamedRef> categories(Nota nota) {
        return nota.getCategorias().stream()
                .sorted(Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(NamedRef::category)
                .toList();
    }

    private List<NamedRef> tags(Nota nota) {
        return nota.getTags().stream()
                .sorted(Comparator.comparing(Tag::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(NamedRef::tag)
                .toList();
    }

    private String extracto(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            return "";
        }
        String flat = contenido.replaceAll("\\s+", " ").trim();
        return flat.length() <= 180 ? flat : flat.substring(0, 180) + "…";
    }
}
