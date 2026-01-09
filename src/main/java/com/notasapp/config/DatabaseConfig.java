package com.notasapp.config;

import com.notasapp.model.Usuario;
import com.notasapp.model.Categoria;
import com.notasapp.repository.I_UsuarioRepository;
import com.notasapp.repository.I_CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseConfig {

    @Bean
    public CommandLineRunner initDatabase(I_UsuarioRepository usuarioRepository,
                                          I_CategoriaRepository categoriaRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Crear usuario admin por defecto si no existe
            Usuario admin = null;
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@notasapp.com");
                admin.setEnabled(true);
                // ❌ QUITA ESTA LÍNEA (si Usuario no tiene setRole):
                // admin.setRole("ADMIN");
                usuarioRepository.save(admin);
                System.out.println("=== USUARIO ADMIN CREADO ===");
                System.out.println("Usuario: admin");
                System.out.println("Contraseña: admin123");
                System.out.println("==============================");
            } else {
                admin = usuarioRepository.findByUsername("admin").get();
                System.out.println("✅ Admin ya existe: " + admin.getUsername());
            }

            // 2. Crear usuario de prueba si no existe
            if (usuarioRepository.findByUsername("usuario").isEmpty()) {
                Usuario usuario = new Usuario();
                usuario.setUsername("usuario");
                usuario.setPassword(passwordEncoder.encode("usuario123"));
                usuario.setEmail("usuario@notasapp.com");
                usuario.setEnabled(true);
                // ❌ QUITA ESTA LÍNEA:
                // usuario.setRole("USER");
                usuarioRepository.save(usuario);
                System.out.println("✅ Usuario de prueba 'usuario' creado");
            }

            // 3. Crear categorías del admin
            crearCategoriasDelAdmin(categoriaRepository, admin);
        };
    }

    private void crearCategoriasDelAdmin(I_CategoriaRepository categoriaRepository, Usuario admin) {
        if (admin == null) {
            System.out.println("⚠️ No se puede crear categorías: admin es null");
            return;
        }

        String[][] categoriasDefault = {
                {"General", "#4f46e5"},
                {"Personal", "#10b981"},
                {"Trabajo", "#f59e0b"},
                {"Estudio", "#3b82f6"}
        };

        int creadas = 0;
        for (String[] cat : categoriasDefault) {
            String nombre = cat[0];
            String color = cat[1];

            // Verificar si ya existe esta categoría para el admin
            if (categoriaRepository.findByUsuarioAndNombre(admin, nombre).isEmpty()) {
                try {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    categoria.setColor(color);
                    categoria.setUsuario(admin);
                    categoriaRepository.save(categoria);
                    creadas++;
                    System.out.println("✅ Categoría creada: " + nombre);
                } catch (Exception e) {
                    System.out.println("❌ Error creando categoría " + nombre + ": " + e.getMessage());
                }
            }
        }

        System.out.println("=== " + creadas + " CATEGORÍAS CREADAS ===");
    }
}