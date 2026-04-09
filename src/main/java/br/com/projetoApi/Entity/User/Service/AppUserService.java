package br.com.projetoApi.Entity.User.Service;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetoApi.Common.Exception.ApiException;
import br.com.projetoApi.Common.Security.AuthenticatedUser;
import br.com.projetoApi.Common.Security.CpfValidator;
import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.Role.Model.Role;
import br.com.projetoApi.Entity.Role.Repository.RoleRepository;
import br.com.projetoApi.Entity.Unidade.Model.Unidade;
import br.com.projetoApi.Entity.Unidade.Repository.UnidadeRepository;
import br.com.projetoApi.Entity.User.Dto.UserCreateRequest;
import br.com.projetoApi.Entity.User.Dto.UserResponse;
import br.com.projetoApi.Entity.User.Dto.UserUpdateRequest;
import br.com.projetoApi.Entity.User.Model.AppUser;
import br.com.projetoApi.Entity.User.Repository.AppUserRepository;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UnidadeRepository unidadeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AppUserService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            UnidadeRepository unidadeRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.unidadeRepository = unidadeRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByCpf(normalizeCpf(cpf))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado para o CPF informado."));

        if (!user.isAtivo()) {
            throw new DisabledException("Usuario inativo nao pode autenticar.");
        }

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> (GrantedAuthority) new SimpleGrantedAuthority(permission.getNome()))
                .distinct()
                .toList();

        return new AuthenticatedUser(user.getId(), user.getCpf(), user.getNome(), user.getSenhaHash(), user.isAtivo(), authorities);
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String cpf = normalizeAndValidateCpf(request.getCpf());
        if (appUserRepository.existsByCpf(cpf)) {
            throw new ApiException(HttpStatus.CONFLICT, "Nao foi possivel criar o usuario.", "Ja existe usuario cadastrado com este CPF.");
        }

        AppUser user = new AppUser();
        user.setCpf(cpf);
        user.setNome(request.getNome().trim());
        user.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        user.setAtivo(request.getAtivo() == null || request.getAtivo());

        AppUser saved = appUserRepository.save(user);
        auditService.register("USER_CREATE", "Usuario criado: " + saved.getCpf());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return appUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        AppUser user = getUser(userId);
        user.setNome(request.getNome().trim());
        auditService.register("USER_UPDATE", "Usuario atualizado: " + user.getCpf());
        return toResponse(user);
    }

    @Transactional
    public UserResponse activate(Long userId) {
        AppUser user = getUser(userId);
        user.setAtivo(true);
        auditService.register("USER_ACTIVATE", "Usuario ativado: " + user.getCpf());
        return toResponse(user);
    }

    @Transactional
    public UserResponse deactivate(Long userId) {
        AppUser user = getUser(userId);
        user.setAtivo(false);
        auditService.register("USER_DEACTIVATE", "Usuario desativado: " + user.getCpf());
        return toResponse(user);
    }

    @Transactional
    public UserResponse linkRole(Long userId, Long roleId) {
        AppUser user = getUser(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Nao foi possivel vincular role ao usuario.", "Role nao encontrada."));
        if (!user.getRoles().add(role)) {
            throw new ApiException(HttpStatus.CONFLICT, "Nao foi possivel vincular role ao usuario.", "O vinculo usuario-role ja existe.");
        }
        auditService.register("USER_ROLE_ASSIGN", "Role " + role.getNome() + " vinculada ao usuario " + user.getCpf());
        return toResponse(user);
    }

    @Transactional
    public UserResponse unlinkRole(Long userId, Long roleId) {
        AppUser user = getUser(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Nao foi possivel remover role do usuario.", "Role nao encontrada."));
        if (!user.getRoles().remove(role)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Nao foi possivel remover role do usuario.", "O vinculo usuario-role nao existe.");
        }
        auditService.register("USER_ROLE_REMOVE", "Role " + role.getNome() + " removida do usuario " + user.getCpf());
        return toResponse(user);
    }

    @Transactional
    public UserResponse linkUnidade(Long userId, Long unidadeId) {
        AppUser user = getUser(userId);
        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Nao foi possivel vincular unidade ao usuario.", "Unidade nao encontrada."));
        if (!user.getUnidades().add(unidade)) {
            throw new ApiException(HttpStatus.CONFLICT, "Nao foi possivel vincular unidade ao usuario.", "O vinculo usuario-unidade ja existe.");
        }
        auditService.register("USER_UNIDADE_ASSIGN", "Unidade " + unidade.getNome() + " vinculada ao usuario " + user.getCpf());
        return toResponse(user);
    }

    @Transactional
    public UserResponse unlinkUnidade(Long userId, Long unidadeId) {
        AppUser user = getUser(userId);
        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Nao foi possivel remover unidade do usuario.", "Unidade nao encontrada."));
        if (!user.getUnidades().remove(unidade)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Nao foi possivel remover unidade do usuario.", "O vinculo usuario-unidade nao existe.");
        }
        auditService.register("USER_UNIDADE_REMOVE", "Unidade " + unidade.getNome() + " removida do usuario " + user.getCpf());
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AppUser getByCpf(String cpf) {
        return appUserRepository.findByCpf(normalizeCpf(cpf))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario nao encontrado.", "Nao existe usuario para o CPF informado."));
    }

    @Transactional
    public AppUser createBootstrapUser(String cpf, String nome, String senhaHash, Set<Role> roles) {
        AppUser user = appUserRepository.findByCpf(normalizeCpf(cpf)).orElseGet(AppUser::new);
        user.setCpf(normalizeAndValidateCpf(cpf));
        user.setNome(nome);
        user.setSenhaHash(senhaHash);
        user.setAtivo(true);
        user.setRoles(roles);
        return appUserRepository.save(user);
    }

    private AppUser getUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario nao encontrado.", "Nao existe usuario para o identificador informado."));
    }

    private String normalizeAndValidateCpf(String cpf) {
        String normalized = normalizeCpf(cpf);
        if (!CpfValidator.isValid(normalized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nao foi possivel processar o usuario.", "CPF invalido.");
        }
        return normalized;
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    public UserResponse toResponse(AppUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setCpf(user.getCpf());
        response.setNome(user.getNome());
        response.setAtivo(user.isAtivo());
        response.setRoles(user.getRoles().stream().map(Role::getNome).sorted().toList());
        response.setUnidades(user.getUnidades().stream().map(Unidade::getNome).sorted().toList());
        return response;
    }
}
