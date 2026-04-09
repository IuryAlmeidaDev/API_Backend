package br.com.projetoApi.Integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.projetoApi.main.BeckEndApplication;

@SpringBootTest(classes = BeckEndApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @BeforeEach
    void clean() {
        appUserRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    void shouldAuthenticateActiveUser() throws Exception {
        createUser("52998224725", "Admin", "senha123", true, createAdminRole());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "cpf": "52998224725",
                                  "senha": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.cpf").value("52998224725"));
    }

    @Test
    void shouldRejectInactiveUserLogin() throws Exception {
        createUser("52998224725", "Inativo", "senha123", false, createAdminRole());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "cpf": "52998224725",
                                  "senha": "senha123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Falha na autenticacao."));
    }
}
