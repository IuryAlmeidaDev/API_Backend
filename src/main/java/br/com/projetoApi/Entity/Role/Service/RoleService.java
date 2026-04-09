package br.com.projetoApi.Entity.Role.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetoApi.Common.Exception.ApiException;
import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.Permission.Model.Permission;
import br.com.projetoApi.Entity.Permission.Repository.PermissionRepository;
import br.com.projetoApi.Entity.Role.Dto.RoleCreateRequest;
import br.com.projetoApi.Entity.Role.Dto.RoleResponse;
import br.com.projetoApi.Entity.Role.Model.Role;
import br.com.projetoApi.Entity.Role.Repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, AuditService auditService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public RoleResponse create(RoleCreateRequest request) {
        String nome = request.getNome().trim().toUpperCase();
        if (roleRepository.existsByNomeIgnoreCase(nome)) {
            throw new ApiException(HttpStatus.CONFLICT, "Nao foi possivel criar a role.", "Ja existe role com este nome.");
        }

        Role role = new Role();
        role.setNome(nome);
        Role saved = roleRepository.save(role);
        auditService.register("ROLE_CREATE", "Role criada: " + saved.getNome());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RoleResponse addPermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Nao foi possivel associar permissao a role.", "Role nao encontrada."));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Nao foi possivel associar permissao a role.", "Permissao nao encontrada."));
        if (!role.getPermissions().add(permission)) {
            throw new ApiException(HttpStatus.CONFLICT, "Nao foi possivel associar permissao a role.", "A permissao ja esta vinculada a role.");
        }
        auditService.register("ROLE_PERMISSION_ASSIGN", "Permissao " + permission.getNome() + " associada a role " + role.getNome());
        return toResponse(role);
    }

    public RoleResponse toResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setNome(role.getNome());
        response.setPermissions(role.getPermissions().stream().map(Permission::getNome).sorted().toList());
        return response;
    }
}
