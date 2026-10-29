package com.notasapp.repository;

import com.notasapp.model.AccountToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
    Optional<AccountToken> findByTokenHashAndTipo(String tokenHash, String tipo);
}
