package br.com.projetoApi.Entity.Bloco.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import br.com.projetoApi.Entity.Bloco.Dto.BlocoDTO;
import br.com.projetoApi.Entity.Bloco.Dto.StatusDTO;
import br.com.projetoApi.Entity.Bloco.Model.Bloco.StatusBloco;
import br.com.projetoApi.Entity.Bloco.Service.BlocoService;

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
@RequestMapping("/api/blocos")
@Tag(name = "Blocos", description = "Endpoints para gerenciamento de blocos")
public class BlocoController {

    @Autowired
    private BlocoService blocoService;

    @Operation(summary = "Listar todos os blocos", description = "Retorna uma lista de todos os blocos cadastrados ordenados por nome")
    @ApiResponse(responseCode = "200", description = "Lista de blocos retornada com sucesso")
    @GetMapping
    @PreAuthorize("hasAuthority('BLOCO_READ')")
    public ResponseEntity<List<BlocoDTO>> listarBlocos() {
        List<BlocoDTO> blocos = blocoService.listarBlocos();
        return ResponseEntity.ok(blocos);
    }

    @Operation(summary = "Obter bloco por ID", description = "Retorna os detalhes de um bloco específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bloco encontrado"),
        @ApiResponse(responseCode = "404", description = "Bloco não encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BLOCO_READ')")
    public ResponseEntity<?> obterBloco(
            @Parameter(description = "ID do bloco", required = true)
            @PathVariable Long id) {
        try {
            BlocoDTO bloco = blocoService.obterBloco(id);
            return ResponseEntity.ok(bloco);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Criar novo bloco", description = "Cria um novo bloco com os dados fornecidos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bloco criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou bloco já existente")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('BLOCO_WRITE')")
    public ResponseEntity<?> criarBloco(@Valid @RequestBody BlocoDTO blocoDTO) {
        try {
            BlocoDTO novoBloco = blocoService.criarBloco(blocoDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoBloco);
        } catch (BlocoService.BlocoAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Atualizar bloco", description = "Atualiza os dados de um bloco existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bloco atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Bloco não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BLOCO_WRITE')")
    public ResponseEntity<?> editarBloco(
            @Parameter(description = "ID do bloco", required = true)
            @PathVariable Long id, 
            @Valid @RequestBody BlocoDTO blocoDTO) {
        try {
            BlocoDTO blocoAtualizado = blocoService.editarBloco(id, blocoDTO);
            return ResponseEntity.ok(blocoAtualizado);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (BlocoService.BlocoAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Deletar bloco", description = "Remove um bloco do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bloco deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Bloco não encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BLOCO_WRITE')")
    public ResponseEntity<?> deletarBloco(
            @Parameter(description = "ID do bloco", required = true)
            @PathVariable Long id) {
        try {
            blocoService.deletarBloco(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Bloco deletado com sucesso");
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Alterar status do bloco", description = "Atualiza apenas o status de um bloco")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Bloco não encontrado")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('BLOCO_WRITE')")
    public ResponseEntity<?> alterarStatusBloco(
            @Parameter(description = "ID do bloco", required = true)
            @PathVariable Long id, 
            @Valid @RequestBody StatusDTO statusDTO) {
        try {
            BlocoDTO blocoAtualizado = blocoService.alterarStatusBloco(id, statusDTO);
            return ResponseEntity.ok(blocoAtualizado);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Listar blocos por status", description = "Retorna uma lista de blocos filtrados por status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('BLOCO_READ')")
    public ResponseEntity<List<BlocoDTO>> listarBlocosPorStatus(
            @Parameter(description = "Status do bloco", required = true)
            @PathVariable StatusBloco status) {
        List<BlocoDTO> blocos = blocoService.listarBlocosPorStatus(status);
        return ResponseEntity.ok(blocos);
    }

    @Operation(summary = "Listar blocos ativos", description = "Retorna uma lista de blocos com status ATIVO")
    @GetMapping("/ativos")
    @PreAuthorize("hasAuthority('BLOCO_READ')")
    public ResponseEntity<List<BlocoDTO>> listarBlocosAtivos() {
        List<BlocoDTO> blocos = blocoService.listarBlocosAtivos();
        return ResponseEntity.ok(blocos);
    }
}
