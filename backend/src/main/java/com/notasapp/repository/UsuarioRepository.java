package com.notasapp.repository;

import com.notasapp.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByUsernameAndDeletedAtIsNull(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByEmailAndDeletedAtIsNull(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUsernameAndDeletedAtIsNull(String username);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    long countByDeletedAtIsNull();

    @EntityGraph(attributePaths = "roles")
    Page<Usuario> findAllByDeletedAtIsNull(Pageable pageable);

    @Query("select count(u) from Usuario u join u.roles r where r.nombre = 'ADMIN' and u.deletedAt is null and u.enabled = true")
    long countActiveAdmins();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id and u.deletedAt is null")
    Optional<Usuario> findForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = "roles")
    @Query("select u from Usuario u where u.deletedAt is null and " +
            "(:q is null or lower(u.username) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%')))")
    Page<Usuario> search(@Param("q") String q, Pageable pageable);
}
