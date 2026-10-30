package com.notasapp.config;

import com.notasapp.model.Role;
import com.notasapp.model.Usuario;
import com.notasapp.repository.RoleRepository;
import com.notasapp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BootstrapAdminRunner implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.countActiveAdmins() > 0) {
            return;
        }
        if (isBlank(properties.getUsername()) || isBlank(properties.getPassword())) {
            return;
        }
        Role adminRole = roleRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Falta el rol ADMIN; revisar migraciones"));
        Usuario usuario = usuarioRepository.findByUsernameAndDeletedAtIsNull(properties.getUsername())
                .orElseGet(Usuario::new);
        if (usuario.getId() == null) {
            usuario.setUsername(properties.getUsername());
            usuario.setEmail(isBlank(properties.getEmail())
                    ? properties.getUsername() + "@notasapp.local"
                    : properties.getEmail());
            usuario.setNombre("Administrador");
            usuario.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
            usuario.setEnabled(true);
        }
        usuario.getRoles().add(adminRole);
        usuario.setEmailVerified(true);
        usuarioRepository.save(usuario);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
