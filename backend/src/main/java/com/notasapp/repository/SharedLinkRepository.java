package com.notasapp.repository;

import com.notasapp.model.SharedLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedLinkRepository extends JpaRepository<SharedLink, Long> {
    Optional<SharedLink> findByTokenHash(String tokenHash);
}
