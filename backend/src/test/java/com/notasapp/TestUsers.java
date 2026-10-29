package com.notasapp;

import com.notasapp.repository.UsuarioRepository;

final class TestUsers {

    private TestUsers() {
    }

    static void verify(UsuarioRepository repository, String username) {
        repository.findByUsernameAndDeletedAtIsNull(username).ifPresent(usuario -> {
            usuario.setEmailVerified(true);
            repository.save(usuario);
        });
    }
}
