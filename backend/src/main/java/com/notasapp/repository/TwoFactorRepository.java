package com.notasapp.repository;

import com.notasapp.model.TwoFactor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwoFactorRepository extends JpaRepository<TwoFactor, Long> {
}
