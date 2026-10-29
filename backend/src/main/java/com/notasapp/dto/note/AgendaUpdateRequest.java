package com.notasapp.dto.note;

import com.notasapp.model.NotaEstado;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Instant;

public record AgendaUpdateRequest(
        Instant fechaVencimiento,
        @Min(1) @Max(3) Integer prioridad,
        NotaEstado estado
) {
}
