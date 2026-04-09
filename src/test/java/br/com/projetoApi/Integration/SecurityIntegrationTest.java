package br.com.projetoApi.Integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.projetoApi.Common.Security.SystemPermission;
import br.com.projetoApi.Entity.Role.Model.Role;
import br.com.projetoApi.main.BeckEndApplication;

@SpringBootTest(classes = BeckEndApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest extends AbstractIntegrationTest {

    private String adminToken;
    private String limitedToken;

    @BeforeEach
    void setup() throws Exception {
        appUserRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        Role adminRole = createAdminRole();
        Role limitedRole = createRoleWithPermissions("VISUALIZADOR", SystemPermission.USER_LIST.name());

        createUser("52998224725", "Admin", "senha123", true, adminRole);
        createUser("39053344705", "Limitado", "senha123", true, limitedRole);

        adminToken = loginAndGetToken("52998224725", "senha123");
        limitedToken = loginAndGetToken("39053344705", "senha123");
    }

    @Test
    void shouldDenyAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithoutPermission() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + limitedToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "cpf": "11144477735",
                                  "nome": "Novo Usuario",
                                  "senha": "senha123"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldInvalidateOldTokenAfterUserIsDeactivated() throws Exception {
        var user = appUserRepository.findByCpf("39053344705").orElseThrow();
        user.setAtivo(false);
        appUserRepository.save(user);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + limitedToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
