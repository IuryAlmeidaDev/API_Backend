package br.com.projetoApi.Config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.projetoApi.Common.Security.SystemPermission;
import br.com.projetoApi.Entity.Permission.Model.Permission;
import br.com.projetoApi.Entity.Permission.Service.PermissionService;
import br.com.projetoApi.Entity.Role.Model.Role;
import br.com.projetoApi.Entity.Role.Repository.RoleRepository;
import br.com.projetoApi.Entity.User.Service.AppUserService;

@Component
public class SecurityBootstrap implements CommandLineRunner {

    private final PermissionService permissionService;
    private final RoleRepository roleRepository;
    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;

    @Value("${BOOTSTRAP_ADMIN_CPF:}")
    private String bootstrapAdminCpf;

    @Value("${BOOTSTRAP_ADMIN_NOME:Administrador}")
    private String bootstrapAdminNome;

    @Value("${BOOTSTRAP_ADMIN_PASSWORD:}")
    private String bootstrapAdminPassword;

    public SecurityBootstrap(
            PermissionService permissionService,
            RoleRepository roleRepository,
            AppUserService appUserService,
            PasswordEncoder passwordEncoder) {
        this.permissionService = permissionService;
        this.roleRepository = roleRepository;
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Set<Permission> defaultPermissions = new HashSet<>();
        for (String permissionName : SystemPermission.names()) {
            defaultPermissions.add(permissionService.ensureDefaultPermission(permissionName));
        }

        if (bootstrapAdminCpf.isBlank() || bootstrapAdminPassword.isBlank()) {
            return;
        }

        Role adminRole = roleRepository.findByNomeIgnoreCase("ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setNome("ADMIN");
            return roleRepository.save(role);
        });
        adminRole.getPermissions().clear();
        adminRole.getPermissions().addAll(defaultPermissions);
        Role savedRole = roleRepository.save(adminRole);

        appUserService.createBootstrapUser(
                bootstrapAdminCpf,
                bootstrapAdminNome,
                passwordEncoder.encode(bootstrapAdminPassword),
                Set.of(savedRole));
    }
}
