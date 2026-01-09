package com.notasapp.dto;

public class CategoriaDTO {
    private Long id;
    private String nombre;
    private String color;
    private String descripcion;
    private Integer cantidadNotas; // Opcional

    // Constructores
    public CategoriaDTO() {}

    public CategoriaDTO(Long id, String nombre, String color) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
    }

    public CategoriaDTO(Long id, String nombre, String color, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCantidadNotas() { return cantidadNotas; }
    public void setCantidadNotas(Integer cantidadNotas) { this.cantidadNotas = cantidadNotas; }
}