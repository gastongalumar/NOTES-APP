package com.notasapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    private Short id;

    @Column(nullable = false, unique = true, length = 20)
    private String nombre;

    public Role(Short id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
