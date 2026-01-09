package com.notasapp.config;

import com.notasapp.model.Categoria;
import com.notasapp.model.Usuario;
import com.notasapp.repository.I_CategoriaRepository;
import com.notasapp.repository.I_UsuarioRepository;
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
            crearUsuarioAdmin(usuarioRepository, passwordEncoder);

            // 2. Crear usuario de prueba si no existe
            crearUsuarioPrueba(usuarioRepository, passwordEncoder);

            // 3. Crear categorías globales si no existen
            crearCategoriasGlobales(categoriaRepository);
        };
    }

    private void crearUsuarioAdmin(I_UsuarioRepository usuarioRepository,
                                   PasswordEncoder passwordEncoder) {
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@notasapp.com");
            admin.setEnabled(true);
            usuarioRepository.save(admin);
            System.out.println("=== USUARIO ADMIN CREADO ===");
            System.out.println("Usuario: admin");
            System.out.println("Contraseña: admin123");
            System.out.println("==============================");
        } else {
            System.out.println("✅ Admin ya existe");
        }
    }

    private void crearUsuarioPrueba(I_UsuarioRepository usuarioRepository,
                                    PasswordEncoder passwordEncoder) {
        if (usuarioRepository.findByUsername("usuario").isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setUsername("usuario");
            usuario.setPassword(passwordEncoder.encode("usuario123"));
            usuario.setEmail("usuario@notasapp.com");
            usuario.setEnabled(true);
            usuarioRepository.save(usuario);
            System.out.println("✅ Usuario de prueba 'usuario' creado");
        }
    }

    private void crearCategoriasGlobales(I_CategoriaRepository categoriaRepository) {
        String[][] categoriasDefault = {
                {"General", "#4f46e5", "Notas de carácter general"},
                {"Personal", "#10b981", "Notas personales y privadas"},
                {"Trabajo", "#f59e0b", "Notas relacionadas con el trabajo"},
                {"Estudio", "#3b82f6", "Notas académicas y de estudio"},
                {"Urgente", "#ef4444", "Tareas y notas urgentes"},
                {"Ideas", "#8b5cf6", "Ideas y proyectos futuros"},
                {"Recordatorios", "#f97316", "Recordatorios importantes"}
        };

        int creadas = 0;
        int existentes = 0;

        for (String[] cat : categoriasDefault) {
            String nombre = cat[0];
            String color = cat[1];
            String descripcion = cat[2];

            if (categoriaRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
                try {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    categoria.setColor(color);
                    categoria.setDescripcion(descripcion);
                    categoriaRepository.save(categoria);
                    creadas++;
                    System.out.println("✅ Categoría global creada: " + nombre);
                } catch (Exception e) {
                    System.out.println("❌ Error creando categoría " + nombre + ": " + e.getMessage());
                }
            } else {
                existentes++;
                System.out.println("ℹ️ Categoría ya existe: " + nombre);
            }
        }

        System.out.println("========================================");
        System.out.println("CATEGORÍAS GLOBALES INICIALIZADAS");
        System.out.println("Creadas: " + creadas);
        System.out.println("Existentes: " + existentes);
        System.out.println("Total: " + (creadas + existentes));
        System.out.println("========================================");
    }
}