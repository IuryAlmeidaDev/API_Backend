package br.com.projetoApi.Integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.projetoApi.Common.Security.SystemPermission;
import br.com.projetoApi.Entity.Role.Model.Role;
import br.com.projetoApi.Entity.Unidade.Model.Unidade;
import br.com.projetoApi.Entity.User.Model.AppUser;
import br.com.projetoApi.main.BeckEndApplication;

@SpringBootTest(classes = BeckEndApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserManagementIntegrationTest extends AbstractIntegrationTest {

    private String adminToken;
    private Role adminRole;

    @BeforeEach
    void setup() throws Exception {
        appUserRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        unidadeRepository.deleteAll();
        adminRole = createAdminRole();
        createUser("52998224725", "Admin", "senha123", true, adminRole);
        adminToken = loginAndGetToken("52998224725", "senha123");
    }

    @Test
    void shouldCreateUpdateLinkAndDeactivateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "cpf": "39053344705",
                                  "nome": "Usuario Teste",
                                  "senha": "senha123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("39053344705"))
                .andExpect(jsonPath("$.ativo").value(true));

        AppUser createdUser = appUserRepository.findByCpf("39053344705").orElseThrow();
        Unidade unidade = createUnidade("Unidade Matriz");
        Role role = createRoleWithPermissions("GESTOR", SystemPermission.USER_LIST.name());

        mockMvc.perform(post("/api/users/%s/roles/%s".formatted(createdUser.getId(), role.getId()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("GESTOR"));

        mockMvc.perform(post("/api/users/%s/unidades/%s".formatted(createdUser.getId(), unidade.getId()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidades[0]").value("Unidade Matriz"));

        mockMvc.perform(put("/api/users/%s".formatted(createdUser.getId()))
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Usuario Atualizado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Usuario Atualizado"));

        mockMvc.perform(patch("/api/users/%s/deactivate".formatted(createdUser.getId()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.cpf=='39053344705')]").exists());
    }

    @Test
    void shouldRejectDuplicateAndMissingLinks() throws Exception {
        AppUser createdUser = createUser("39053344705", "Usuario Teste", "senha123", true);
        Unidade unidade = createUnidade("Unidade Matriz");

        mockMvc.perform(post("/api/users/%s/unidades/%s".formatted(createdUser.getId(), unidade.getId()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/%s/unidades/%s".formatted(createdUser.getId(), unidade.getId()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.reason").value("O vinculo usuario-unidade ja existe."));

        mockMvc.perform(patch("/api/users/%s/unidades/999/remove".formatted(createdUser.getId()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreatePermissionRoleAndUnidadeThroughApi() throws Exception {
        mockMvc.perform(post("/api/permissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "CUSTOM_REPORT_VIEW"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("CUSTOM_REPORT_VIEW"));

        mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "ANALISTA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("ANALISTA"));

        var permission = permissionRepository.findByNomeIgnoreCase("CUSTOM_REPORT_VIEW").orElseThrow();
        var role = roleRepository.findByNomeIgnoreCase("ANALISTA").orElseThrow();

        mockMvc.perform(post("/api/roles/%s/permissions/%s".formatted(role.getId(), permission.getId()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions[0]").value("CUSTOM_REPORT_VIEW"));

        mockMvc.perform(post("/api/unidades")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Filial",
                                  "tipo": "ADMINISTRATIVA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Filial"))
                .andExpect(jsonPath("$.tipo").value("ADMINISTRATIVA"));
    }
}
