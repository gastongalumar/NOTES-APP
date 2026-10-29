package com.notasapp.repository;

import com.notasapp.model.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = {"usuario", "usuario.roles"})
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Query("select count(distinct rt.familyId) from RefreshToken rt " +
            "where rt.usuario.id = :usuarioId and rt.revokedAt is null and rt.expiresAt > :now")
    long countActiveFamilies(@Param("usuarioId") Long usuarioId, @Param("now") Instant now);

    @Query("select rt from RefreshToken rt where rt.usuario.id = :usuarioId " +
            "and rt.revokedAt is null and rt.expiresAt > :now order by rt.issuedAt asc")
    List<RefreshToken> findActiveTokens(@Param("usuarioId") Long usuarioId, @Param("now") Instant now);

    @Modifying
    @Query("update RefreshToken rt set rt.revokedAt = :now where rt.familyId = :familyId and rt.revokedAt is null")
    int revokeFamily(@Param("familyId") String familyId, @Param("now") Instant now);

    @Modifying
    @Query("delete from RefreshToken rt where rt.expiresAt < :before or (rt.revokedAt is not null and rt.revokedAt < :before)")
    int purgeExpired(@Param("before") Instant before);
}
