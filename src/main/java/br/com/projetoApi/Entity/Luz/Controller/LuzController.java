package br.com.projetoApi.Entity.Luz.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import br.com.projetoApi.Entity.Luz.Dto.LuzDTO;
import br.com.projetoApi.Entity.Luz.Service.LuzService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/luzes")
@Tag(name = "Luzes", description = "Endpoints para gerenciamento de luzes")
public class LuzController {

    @Autowired
    private LuzService luzService;

    @Operation(summary = "Ligar todas as luzes do bloco", description = "Liga todas as luzes de um bloco específico")
    @PatchMapping("/bloco/{blocoId}/ligar-todas")
    @PreAuthorize("hasAuthority('LUZ_WRITE')")
    public ResponseEntity<List<LuzDTO>> ligarTodasLuzesDoBloco(
            @Parameter(description = "ID do bloco", required = true)
            @PathVariable Long blocoId) {
        List<LuzDTO> luzes = luzService.ligarTodasLuzesDoBloco(blocoId);
        return ResponseEntity.ok(luzes);
    }

    @Operation(summary = "Desligar todas as luzes do bloco", description = "Desliga todas as luzes de um bloco específico")
    @PatchMapping("/bloco/{blocoId}/desligar-todas")
    @PreAuthorize("hasAuthority('LUZ_WRITE')")
    public ResponseEntity<List<LuzDTO>> desligarTodasLuzesDoBloco(
            @Parameter(description = "ID do bloco", required = true)
            @PathVariable Long blocoId) {
        List<LuzDTO> luzes = luzService.desligarTodasLuzesDoBloco(blocoId);
        return ResponseEntity.ok(luzes);
    }

    @Operation(summary = "Ligar luz", description = "Liga uma luz específica")
    @PatchMapping("/{id}/ligar")
    @PreAuthorize("hasAuthority('LUZ_WRITE')")
    public ResponseEntity<?> ligarLuz(
            @Parameter(description = "ID da luz", required = true)
            @PathVariable Long id) {
        try {
            LuzDTO luzAtualizada = luzService.ligarLuz(id);
            return ResponseEntity.ok(luzAtualizada);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Desligar luz", description = "Desliga uma luz específica")
    @PatchMapping("/{id}/desligar")
    @PreAuthorize("hasAuthority('LUZ_WRITE')")
    public ResponseEntity<?> desligarLuz(
            @Parameter(description = "ID da luz", required = true)
            @PathVariable Long id) {
        try {
            LuzDTO luzAtualizada = luzService.desligarLuz(id);
            return ResponseEntity.ok(luzAtualizada);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Criar nova luz", description = "Cria uma nova luz com os dados fornecidos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Luz criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou luz já existente na sala"),
        @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('LUZ_WRITE')")
    public ResponseEntity<?> criarLuz(@Valid @RequestBody LuzDTO luzDTO) {
        try {
            LuzDTO novaLuz = luzService.criarLuz(luzDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaLuz);
        } catch (LuzService.LuzAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Atualizar luz", description = "Atualiza os dados de uma luz existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Luz atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Luz ou sala não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LUZ_WRITE')")
    public ResponseEntity<?> editarLuz(
            @Parameter(description = "ID da luz", required = true)
            @PathVariable Long id, 
            @Valid @RequestBody LuzDTO luzDTO) {
        try {
            LuzDTO luzAtualizada = luzService.editarLuz(id, luzDTO);
            return ResponseEntity.ok(luzAtualizada);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (LuzService.LuzAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Obter luz por ID", description = "Retorna os detalhes de uma luz específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Luz encontrada"),
        @ApiResponse(responseCode = "404", description = "Luz não encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LUZ_READ')")
    public ResponseEntity<?> obterLuz(
            @Parameter(description = "ID da luz", required = true)
            @PathVariable Long id) {
        try {
            LuzDTO luz = luzService.obterLuz(id);
            return ResponseEntity.ok(luz);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Deletar luz", description = "Remove uma luz do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Luz deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Luz não encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LUZ_WRITE')")
    public ResponseEntity<?> deletarLuz(
            @Parameter(description = "ID da luz", required = true)
            @PathVariable Long id) {
        try {
            luzService.deletarLuz(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Luz deletada com sucesso");
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
