package br.com.projetoApi.Integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.projetoApi.Entity.Bloco.Model.Bloco;
import br.com.projetoApi.Entity.Luz.Model.Luz;
import br.com.projetoApi.Entity.Role.Model.Role;
import br.com.projetoApi.Entity.Sala.Model.Sala;
import br.com.projetoApi.main.BeckEndApplication;

@SpringBootTest(classes = BeckEndApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LegacyModulesIntegrationTest extends AbstractIntegrationTest {

    private String adminToken;
    private String readOnlyToken;

    @BeforeEach
    void setup() throws Exception {
        appUserRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        unidadeRepository.deleteAll();
        luzRepository.deleteAll();
        salaRepository.deleteAll();
        blocoRepository.deleteAll();

        Role adminRole = createAdminRole();
        Role readOnlyRole = createRoleWithPermissions("OPERADOR_LEITURA", "BLOCO_READ", "SALA_READ", "LUZ_READ");

        createUser("52998224725", "Admin", "senha123", true, adminRole);
        createUser("39053344705", "Leitor", "senha123", true, readOnlyRole);

        adminToken = loginAndGetToken("52998224725", "senha123");
        readOnlyToken = loginAndGetToken("39053344705", "senha123");
    }

    @Test
    void shouldProtectLegacyWriteOperationsAndAllowReadOperations() throws Exception {
        mockMvc.perform(post("/api/blocos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Bloco A",
                                  "descricao": "Predio principal",
                                  "status": "ATIVO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Bloco A"));

        Bloco bloco = blocoRepository.findByNomeIgnoreCase("Bloco A").orElseThrow();

        mockMvc.perform(get("/api/blocos")
                        .header("Authorization", "Bearer " + readOnlyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Bloco A"));

        mockMvc.perform(post("/api/salas")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Sala 101",
                                  "capacidade": 30,
                                  "status": "LIVRE",
                                  "tipoSala": "SALA_DE_AULA",
                                  "blocoId": %s
                                }
                                """.formatted(bloco.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Sala 101"));

        Sala sala = salaRepository.findByNomeIgnoreCase("Sala 101").orElseThrow();

        mockMvc.perform(get("/api/salas/%s".formatted(sala.getId()))
                        .header("Authorization", "Bearer " + readOnlyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Sala 101"));

        mockMvc.perform(post("/api/luzes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "salaId": %s,
                                  "status": "DESLIGADO"
                                }
                                """.formatted(sala.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.salaNome").value("Sala 101"));

        Luz luz = luzRepository.findBySalaId(sala.getId()).stream().findFirst().orElseThrow();

        mockMvc.perform(get("/api/luzes/%s".formatted(luz.getId()))
                        .header("Authorization", "Bearer " + readOnlyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DESLIGADO"));

        mockMvc.perform(patch("/api/luzes/%s/ligar".formatted(luz.getId()))
                        .header("Authorization", "Bearer " + readOnlyToken))
                .andExpect(status().isForbidden());
    }
}
