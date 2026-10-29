package com.notasapp.dto;

public record NamedRef(Long id, String nombre, String color) {
    public static NamedRef category(com.notasapp.model.Categoria categoria) {
        return new NamedRef(categoria.getId(), categoria.getNombre(), categoria.getColor());
    }

    public static NamedRef tag(com.notasapp.model.Tag tag) {
        return new NamedRef(tag.getId(), tag.getNombre(), null);
    }
}
