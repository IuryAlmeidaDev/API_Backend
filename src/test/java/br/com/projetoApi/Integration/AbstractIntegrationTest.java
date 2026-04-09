package br.com.projetoApi.Integration;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.projetoApi.Common.Security.SystemPermission;
import br.com.projetoApi.Entity.Bloco.Repository.BlocoRepository;
import br.com.projetoApi.Entity.Luz.Repository.LuzRepository;
import br.com.projetoApi.Entity.Permission.Model.Permission;
import br.com.projetoApi.Entity.Permission.Repository.PermissionRepository;
import br.com.projetoApi.Entity.Role.Model.Role;
import br.com.projetoApi.Entity.Role.Repository.RoleRepository;
import br.com.projetoApi.Entity.Sala.Repository.SalaRepository;
import br.com.projetoApi.Entity.Unidade.Model.Unidade;
import br.com.projetoApi.Entity.Unidade.Repository.UnidadeRepository;
import br.com.projetoApi.Entity.User.Model.AppUser;
import br.com.projetoApi.Entity.User.Repository.AppUserRepository;

public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AppUserRepository appUserRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PermissionRepository permissionRepository;

    @Autowired
    protected UnidadeRepository unidadeRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected BlocoRepository blocoRepository;

    @Autowired
    protected SalaRepository salaRepository;

    @Autowired
    protected LuzRepository luzRepository;

    protected Role createRoleWithPermissions(String roleName, String... permissions) {
        Role role = new Role();
        role.setNome(roleName);
        for (String permissionName : permissions) {
            Permission permission = permissionRepository.findByNomeIgnoreCase(permissionName).orElseGet(() -> {
                Permission created = new Permission();
                created.setNome(permissionName);
                return permissionRepository.save(created);
            });
            role.getPermissions().add(permission);
        }
        return roleRepository.save(role);
    }

    protected AppUser createUser(String cpf, String nome, String senha, boolean ativo, Role... roles) {
        AppUser user = new AppUser();
        user.setCpf(cpf);
        user.setNome(nome);
        user.setSenhaHash(passwordEncoder.encode(senha));
        user.setAtivo(ativo);
        user.setRoles(Set.of(roles));
        return appUserRepository.save(user);
    }

    protected Unidade createUnidade(String nome) {
        Unidade unidade = new Unidade();
        unidade.setNome(nome);
        unidade.setTipo(Unidade.Tipo.CENTRAL);
        unidade.setAtivo(true);
        return unidadeRepository.save(unidade);
    }

    protected String loginAndGetToken(String cpf, String senha) throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "cpf": "%s",
                          "senha": "%s"
                        }
                        """.formatted(cpf, senha)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    protected Role createAdminRole() {
        return createRoleWithPermissions("ADMIN", SystemPermission.names().toArray(String[]::new));
    }
}
