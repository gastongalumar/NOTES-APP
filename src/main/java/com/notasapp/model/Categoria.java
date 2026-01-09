package com.notasapp.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String color;

    @Column(length = 500)
    private String descripcion;

    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    private Set<Nota> notas = new HashSet<>();

    // Constructor vacío (obligatorio para JPA)
    public Categoria() {
    }

    // Constructor con parámetros básicos
    public Categoria(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    // Constructor con todos los parámetros
    public Categoria(String nombre, String color, String descripcion) {
        this.nombre = nombre;
        this.color = color;
        this.descripcion = descripcion;
    }

    // ========== GETTERS Y SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<Nota> getNotas() {
        return notas;
    }

    public void setNotas(Set<Nota> notas) {
        this.notas = notas;
    }

    // ========== MÉTODOS DE CONVENIENCIA ==========

    /**
     * Método para agregar una nota a la categoría
     * Mantiene la consistencia bidireccional
     */
    public void agregarNota(Nota nota) {
        this.notas.add(nota);
        nota.getCategorias().add(this);
    }

    /**
     * Método para remover una nota de la categoría
     * Mantiene la consistencia bidireccional
     */
    public void removerNota(Nota nota) {
        this.notas.remove(nota);
        nota.getCategorias().remove(this);
    }

    // ========== EQUALS Y HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categoria)) return false;

        Categoria categoria = (Categoria) o;

        // Usamos id para equals si está persistido
        if (id != null) {
            return id.equals(categoria.id);
        }

        // Si no tiene id, usamos nombre (debe ser único)
        return nombre != null && nombre.equals(categoria.nombre);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return nombre != null ? nombre.hashCode() : 0;
    }

    // ========== TO STRING ==========

    @Override
    public String toString() {
        return "Categoria{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", color='" + color + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}