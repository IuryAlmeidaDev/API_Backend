package br.com.projetoApi.Entity.Permission.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetoApi.Entity.Permission.Dto.PermissionCreateRequest;
import br.com.projetoApi.Entity.Permission.Service.PermissionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ResponseEntity<?> create(@Valid @RequestBody PermissionCreateRequest request) {
        return ResponseEntity.status(201).body(permissionService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_LIST')")
    public ResponseEntity<?> listAll() {
        return ResponseEntity.ok(permissionService.listAll());
    }
}
