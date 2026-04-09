package br.com.projetoApi.Entity.Permission.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetoApi.Common.Exception.ApiException;
import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.Permission.Dto.PermissionCreateRequest;
import br.com.projetoApi.Entity.Permission.Dto.PermissionResponse;
import br.com.projetoApi.Entity.Permission.Model.Permission;
import br.com.projetoApi.Entity.Permission.Repository.PermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    public PermissionService(PermissionRepository permissionRepository, AuditService auditService) {
        this.permissionRepository = permissionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PermissionResponse create(PermissionCreateRequest request) {
        String nome = normalize(request.getNome());
        if (permissionRepository.existsByNomeIgnoreCase(nome)) {
            throw new ApiException(HttpStatus.CONFLICT, "Nao foi possivel criar a permissao.", "Ja existe permissao com este nome.");
        }

        Permission permission = new Permission();
        permission.setNome(nome);
        Permission saved = permissionRepository.save(permission);
        auditService.register("PERMISSION_CREATE", "Permissao criada: " + saved.getNome());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listAll() {
        return permissionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public Permission ensureDefaultPermission(String nome) {
        String normalized = normalize(nome);
        return permissionRepository.findByNomeIgnoreCase(normalized).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setNome(normalized);
            return permissionRepository.save(permission);
        });
    }

    public PermissionResponse toResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setNome(permission.getNome());
        return response;
    }

    private String normalize(String nome) {
        return nome.trim().toUpperCase();
    }
}
