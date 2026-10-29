package com.notasapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "two_factor")
@Getter
@Setter
@NoArgsConstructor
public class TwoFactor {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "secret_encrypted", nullable = false, length = 255)
    private String secretEncrypted;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recovery_codes_hash", columnDefinition = "json")
    private String recoveryCodesHash;

    @Column(name = "enabled_at")
    private Instant enabledAt;
}
