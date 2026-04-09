package br.com.projetoApi.Entity.Unidade.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetoApi.Entity.Unidade.Dto.UnidadeCreateRequest;
import br.com.projetoApi.Entity.Unidade.Dto.UnidadeResponse;
import br.com.projetoApi.Entity.Unidade.Dto.UnidadeUpdateRequest;
import br.com.projetoApi.Entity.Unidade.Service.UnidadeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('UNIDADE_CREATE')")
    public ResponseEntity<UnidadeResponse> create(@Valid @RequestBody UnidadeCreateRequest request) {
        return ResponseEntity.status(201).body(unidadeService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('UNIDADE_LIST')")
    public ResponseEntity<?> listAll() {
        return ResponseEntity.ok(unidadeService.listAll());
    }

    @PutMapping("/{unidadeId}")
    @PreAuthorize("hasAuthority('UNIDADE_UPDATE')")
    public ResponseEntity<UnidadeResponse> update(@PathVariable Long unidadeId, @Valid @RequestBody UnidadeUpdateRequest request) {
        return ResponseEntity.ok(unidadeService.update(unidadeId, request));
    }

    @PatchMapping("/{unidadeId}/activate")
    @PreAuthorize("hasAuthority('UNIDADE_TOGGLE_ACTIVE')")
    public ResponseEntity<UnidadeResponse> activate(@PathVariable Long unidadeId) {
        return ResponseEntity.ok(unidadeService.activate(unidadeId));
    }

    @PatchMapping("/{unidadeId}/deactivate")
    @PreAuthorize("hasAuthority('UNIDADE_TOGGLE_ACTIVE')")
    public ResponseEntity<UnidadeResponse> deactivate(@PathVariable Long unidadeId) {
        return ResponseEntity.ok(unidadeService.deactivate(unidadeId));
    }
}
