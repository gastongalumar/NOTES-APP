package com.notasapp;

import com.notasapp.model.Role;
import com.notasapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestRolesInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role((short) 1, "USER"));
            roleRepository.save(new Role((short) 2, "ADMIN"));
        }
    }
}
