package com.notasapp.dto.note;

import com.notasapp.model.NotaEstado;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;

public record NoteUpdateRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String titulo,

        @Size(max = 100000, message = "El contenido es demasiado largo")
        String contenido,

        @Size(max = 20, message = "Máximo 20 categorías")
        Set<Long> categoriaIds,

        @Size(max = 30, message = "Máximo 30 tags")
        Set<Long> tagIds,

        Instant fechaVencimiento,

        @Min(value = 1, message = "La prioridad mínima es 1")
        @Max(value = 3, message = "La prioridad máxima es 3")
        Integer prioridad,

        NotaEstado estado,

        @NotNull(message = "La versión es obligatoria")
        @PositiveOrZero(message = "La versión no puede ser negativa")
        Long version
) {
}
