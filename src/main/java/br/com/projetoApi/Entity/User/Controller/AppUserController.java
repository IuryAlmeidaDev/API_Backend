package br.com.projetoApi.Entity.User.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetoApi.Config.JwtUtil;
import br.com.projetoApi.Common.Exception.ApiException;
import br.com.projetoApi.Common.Security.CpfValidator;
import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.User.Dto.AuthLoginRequest;
import br.com.projetoApi.Entity.User.Dto.AuthResponse;
import br.com.projetoApi.Entity.User.Dto.UserCreateRequest;
import br.com.projetoApi.Entity.User.Dto.UserResponse;
import br.com.projetoApi.Entity.User.Dto.UserUpdateRequest;
import br.com.projetoApi.Entity.User.Service.AppUserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping
public class AppUserController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AppUserService appUserService;
    private final AuditService auditService;

    public AppUserController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            AppUserService appUserService,
            AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.appUserService = appUserService;
        this.auditService = auditService;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        String normalizedCpf = request.getCpf().replaceAll("\\D", "");
        if (!CpfValidator.isValid(normalizedCpf)) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "Falha na autenticacao.", "CPF invalido.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedCpf, request.getSenha()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserResponse user = appUserService.toResponse(appUserService.getByCpf(userDetails.getUsername()));

        AuthResponse response = new AuthResponse();
        response.setMessage("Autenticacao realizada com sucesso.");
        response.setToken(jwtUtil.generateToken(userDetails));
        response.setUser(user);
        auditService.register("AUTH_LOGIN", "Login efetuado com sucesso para o usuario " + user.getCpf());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/users")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(201).body(appUserService.create(request));
    }

    @GetMapping("/api/users")
    @PreAuthorize("hasAuthority('USER_LIST')")
    public ResponseEntity<?> listAll() {
        return ResponseEntity.ok(appUserService.listAll());
    }

    @PutMapping("/api/users/{userId}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserResponse> update(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(appUserService.update(userId, request));
    }

    @PatchMapping("/api/users/{userId}/activate")
    @PreAuthorize("hasAuthority('USER_TOGGLE_ACTIVE')")
    public ResponseEntity<UserResponse> activate(@PathVariable Long userId) {
        return ResponseEntity.ok(appUserService.activate(userId));
    }

    @PatchMapping("/api/users/{userId}/deactivate")
    @PreAuthorize("hasAuthority('USER_TOGGLE_ACTIVE')")
    public ResponseEntity<UserResponse> deactivate(@PathVariable Long userId) {
        return ResponseEntity.ok(appUserService.deactivate(userId));
    }

    @PostMapping("/api/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public ResponseEntity<UserResponse> linkRole(@PathVariable Long userId, @PathVariable Long roleId) {
        return ResponseEntity.ok(appUserService.linkRole(userId, roleId));
    }

    @PatchMapping("/api/users/{userId}/roles/{roleId}/remove")
    @PreAuthorize("hasAuthority('ROLE_REMOVE')")
    public ResponseEntity<UserResponse> unlinkRole(@PathVariable Long userId, @PathVariable Long roleId) {
        return ResponseEntity.ok(appUserService.unlinkRole(userId, roleId));
    }

    @PostMapping("/api/users/{userId}/unidades/{unidadeId}")
    @PreAuthorize("hasAuthority('USER_UNIDADE_ASSIGN')")
    public ResponseEntity<UserResponse> linkUnidade(@PathVariable Long userId, @PathVariable Long unidadeId) {
        return ResponseEntity.ok(appUserService.linkUnidade(userId, unidadeId));
    }

    @PatchMapping("/api/users/{userId}/unidades/{unidadeId}/remove")
    @PreAuthorize("hasAuthority('USER_UNIDADE_REMOVE')")
    public ResponseEntity<UserResponse> unlinkUnidade(@PathVariable Long userId, @PathVariable Long unidadeId) {
        return ResponseEntity.ok(appUserService.unlinkUnidade(userId, unidadeId));
    }
}
