package br.com.projetoApi.Entity.Sala.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import br.com.projetoApi.Entity.Sala.Dto.SalaDTO;
import br.com.projetoApi.Entity.Sala.Dto.SalaStatusDTO;
import br.com.projetoApi.Entity.Sala.Model.Sala.StatusSala;
import br.com.projetoApi.Entity.Sala.Model.Sala.TipoSala;
import br.com.projetoApi.Entity.Sala.Service.SalaService;

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
@RequestMapping("/api/salas")
@Tag(name = "Salas", description = "Endpoints para gerenciamento de salas")
public class SalaController {

    @Autowired
    private SalaService salaService;

    @Operation(summary = "Listar todas as salas", description = "Retorna uma lista de todas as salas cadastradas ordenadas por nome")
    @ApiResponse(responseCode = "200", description = "Lista de salas retornada com sucesso")
    @GetMapping
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> listarSalas() {
        List<SalaDTO> salas = salaService.listarSalas();
        return ResponseEntity.ok(salas);
    }

    @Operation(summary = "Obter sala por ID", description = "Retorna os detalhes de uma sala específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sala encontrada"),
        @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<?> obterSala(
            @Parameter(description = "ID da sala", required = true)
            @PathVariable Long id) {
        try {
            SalaDTO sala = salaService.obterSala(id);
            return ResponseEntity.ok(sala);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Criar nova sala", description = "Cria uma nova sala com os dados fornecidos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sala criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou sala já existente"),
        @ApiResponse(responseCode = "404", description = "Bloco não encontrado")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('SALA_WRITE')")
    public ResponseEntity<?> criarSala(@Valid @RequestBody SalaDTO salaDTO) {
        try {
            SalaDTO novaSala = salaService.criarSala(salaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaSala);
        } catch (SalaService.SalaAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Atualizar sala", description = "Atualiza os dados de uma sala existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sala atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sala ou bloco não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALA_WRITE')")
    public ResponseEntity<?> editarSala(
            @Parameter(description = "ID da sala", required = true)
            @PathVariable Long id, 
            @Valid @RequestBody SalaDTO salaDTO) {
        try {
            SalaDTO salaAtualizada = salaService.editarSala(id, salaDTO);
            return ResponseEntity.ok(salaAtualizada);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (SalaService.SalaAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Deletar sala", description = "Remove uma sala do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sala deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SALA_WRITE')")
    public ResponseEntity<?> deletarSala(
            @Parameter(description = "ID da sala", required = true)
            @PathVariable Long id) {
        try {
            salaService.deletarSala(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Sala deletada com sucesso");
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Alterar status da sala", description = "Atualiza apenas o status de uma sala")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SALA_WRITE')")
    public ResponseEntity<?> alterarStatusSala(
            @Parameter(description = "ID da sala", required = true)
            @PathVariable Long id, 
            @Valid @RequestBody SalaStatusDTO statusDTO) {
        try {
            SalaDTO salaAtualizada = salaService.alterarStatusSala(id, statusDTO);
            return ResponseEntity.ok(salaAtualizada);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Listar salas por status", description = "Retorna uma lista de salas filtradas por status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> listarSalasPorStatus(
            @Parameter(description = "Status da sala", required = true)
            @PathVariable StatusSala status) {
        List<SalaDTO> salas = salaService.listarSalasPorStatus(status);
        return ResponseEntity.ok(salas);
    }

    @Operation(summary = "Listar salas por tipo", description = "Retorna uma lista de salas filtradas por tipo")
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> listarSalasPorTipo(
            @Parameter(description = "Tipo da sala", required = true)
            @PathVariable TipoSala tipo) {
        List<SalaDTO> salas = salaService.listarSalasPorTipo(tipo);
        return ResponseEntity.ok(salas);
    }

    @Operation(summary = "Listar salas por bloco", description = "Retorna uma lista de salas de um bloco específico")
    @GetMapping("/bloco/{blocoId}")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> listarSalasPorBloco(
            @Parameter(description = "ID do bloco", required = true)
            @PathVariable Long blocoId) {
        List<SalaDTO> salas = salaService.listarSalasPorBloco(blocoId);
        return ResponseEntity.ok(salas);
    }

    @Operation(summary = "Listar salas livres", description = "Retorna uma lista de salas com status LIVRE")
    @GetMapping("/livres")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> listarSalasLivres() {
        List<SalaDTO> salas = salaService.listarSalasLivres();
        return ResponseEntity.ok(salas);
    }

    @Operation(summary = "Listar salas por capacidade mínima", description = "Retorna salas com capacidade maior ou igual ao valor especificado")
    @GetMapping("/capacidade/{capacidade}")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> listarSalasPorCapacidade(
            @Parameter(description = "Capacidade mínima", required = true)
            @PathVariable Integer capacidade) {
        List<SalaDTO> salas = salaService.listarSalasPorCapacidadeMinima(capacidade);
        return ResponseEntity.ok(salas);
    }

    @Operation(summary = "Listar salas livres com capacidade mínima", description = "Retorna salas livres com capacidade maior ou igual ao valor especificado")
    @GetMapping("/livres/capacidade/{capacidade}")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> listarSalasLivresComCapacidade(
            @Parameter(description = "Capacidade mínima", required = true)
            @PathVariable Integer capacidade) {
        List<SalaDTO> salas = salaService.listarSalasLivresComCapacidadeMinima(capacidade);
        return ResponseEntity.ok(salas);
    }

    @Operation(summary = "Buscar salas com filtros", description = "Retorna salas filtradas por múltiplos critérios")
    @GetMapping("/filtros")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> buscarSalasComFiltros(
            @Parameter(description = "ID do bloco (opcional)")
            @RequestParam(required = false) Long blocoId,
            @Parameter(description = "Status da sala (opcional)")
            @RequestParam(required = false) StatusSala status,
            @Parameter(description = "Tipo da sala (opcional)")
            @RequestParam(required = false) TipoSala tipoSala,
            @Parameter(description = "Capacidade mínima (opcional)")
            @RequestParam(required = false) Integer capacidadeMinima) {
        
        List<SalaDTO> salas = salaService.buscarSalasComFiltros(blocoId, status, tipoSala, capacidadeMinima);
        return ResponseEntity.ok(salas);
    }

    @Operation(summary = "Contar salas livres por bloco", description = "Retorna o número de salas livres em um bloco específico")
    @GetMapping("/bloco/{blocoId}/livres/contar")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<Map<String, Long>> contarSalasLivresPorBloco(
            @Parameter(description = "ID do bloco", required = true)
            @PathVariable Long blocoId) {
        Long count = salaService.contarSalasLivresPorBloco(blocoId);
        Map<String, Long> response = new HashMap<>();
        response.put("salasLivres", count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar salas por nome", description = "Retorna salas que contenham o nome especificado")
    @GetMapping("/buscar")
    @PreAuthorize("hasAuthority('SALA_READ')")
    public ResponseEntity<List<SalaDTO>> buscarSalasPorNome(
            @Parameter(description = "Nome da sala para busca", required = true)
            @RequestParam String nome) {
        List<SalaDTO> salas = salaService.buscarSalasPorNome(nome);
        return ResponseEntity.ok(salas);
    }
}
