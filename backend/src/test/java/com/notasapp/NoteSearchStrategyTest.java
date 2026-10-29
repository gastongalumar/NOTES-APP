package com.notasapp;

import com.notasapp.mapper.NoteMapper;
import com.notasapp.repository.*;
import com.notasapp.service.AuditService;
import com.notasapp.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NoteSearchStrategyTest {

    private final NotaRepository notaRepository = mock(NotaRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final RecordatorioRepository recordatorioRepository = mock(RecordatorioRepository.class);
    private final NoteMapper noteMapper = mock(NoteMapper.class);
    private final AuditService auditService = mock(AuditService.class);

    private final NoteServiceImpl service = new NoteServiceImpl(
            notaRepository, categoriaRepository, tagRepository, usuarioRepository,
            recordatorioRepository, noteMapper, auditService);

    @Test
    void usesFullTextForQueriesOfThreeOrMoreCharacters() {
        ReflectionTestUtils.setField(service, "fullTextEnabled", true);
        Pageable pageable = PageRequest.of(0, 20);
        Page<com.notasapp.model.Nota> empty = Page.empty(pageable);
        when(notaRepository.searchFullText(eq(1L), isNull(), eq("test"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(empty);

        service.list(1L, null, "test", null, null, pageable);

        verify(notaRepository).searchFullText(eq(1L), isNull(), eq("test"), isNull(), isNull(), any(Pageable.class));
        verify(notaRepository, never()).searchLike(any(), any(), any(), any(), any(), any());
    }

    @Test
    void usesLikeFallbackForShortQueries() {
        ReflectionTestUtils.setField(service, "fullTextEnabled", true);
        Pageable pageable = PageRequest.of(0, 20);
        when(notaRepository.searchLike(eq(1L), isNull(), eq("a"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        service.list(1L, null, "a", null, null, pageable);

        verify(notaRepository).searchLike(eq(1L), isNull(), eq("a"), isNull(), isNull(), any(Pageable.class));
        verify(notaRepository, never()).searchFullText(any(), any(), any(), any(), any(), any());
    }
}
