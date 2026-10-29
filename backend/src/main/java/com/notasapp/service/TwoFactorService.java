package com.notasapp.service;

import com.notasapp.dto.auth.TwoFactorSetupResponse;

import java.util.List;

public interface TwoFactorService {
    TwoFactorSetupResponse setup(Long usuarioId);

    List<String> verifyAndEnable(Long usuarioId, String code);

    boolean isEnabled(Long usuarioId);

    boolean verifyLogin(Long usuarioId, String code);
}
