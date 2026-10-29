package com.notasapp.mapper;

import com.notasapp.dto.auth.UserResponse;
import com.notasapp.model.Role;
import com.notasapp.model.Usuario;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(Usuario usuario);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(Role::getNombre).collect(Collectors.toSet());
    }
}
