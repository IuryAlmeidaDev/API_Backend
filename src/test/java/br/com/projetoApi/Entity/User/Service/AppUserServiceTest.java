package br.com.projetoApi.Entity.User.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.projetoApi.Common.Exception.ApiException;
import br.com.projetoApi.Entity.Audit.Service.AuditService;
import br.com.projetoApi.Entity.Role.Model.Role;
import br.com.projetoApi.Entity.Role.Repository.RoleRepository;
import br.com.projetoApi.Entity.Unidade.Model.Unidade;
import br.com.projetoApi.Entity.Unidade.Repository.UnidadeRepository;
import br.com.projetoApi.Entity.User.Dto.UserCreateRequest;
import br.com.projetoApi.Entity.User.Model.AppUser;
import br.com.projetoApi.Entity.User.Repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UnidadeRepository unidadeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditService auditService;

    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        appUserService = new AppUserService(appUserRepository, roleRepository, unidadeRepository, passwordEncoder, auditService);
    }

    @Test
    void shouldRejectInvalidCpfOnCreate() {
        UserCreateRequest request = new UserCreateRequest();
        request.setCpf("123");
        request.setNome("Usuario");
        request.setSenha("123456");

        ApiException exception = assertThrows(ApiException.class, () -> appUserService.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateRoleLink() {
        AppUser user = new AppUser();
        Role role = new Role();
        role.setNome("ADMIN");
        user.getRoles().add(role);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));

        ApiException exception = assertThrows(ApiException.class, () -> appUserService.linkRole(1L, 2L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void shouldRejectInactiveUserDuringAuthentication() {
        AppUser user = new AppUser();
        user.setCpf("52998224725");
        user.setNome("Inativo");
        user.setSenhaHash("hash");
        user.setAtivo(false);

        when(appUserRepository.findByCpf("52998224725")).thenReturn(Optional.of(user));

        assertThrows(DisabledException.class, () -> appUserService.loadUserByUsername("52998224725"));
    }

    @Test
    void shouldLinkUnitOnlyOnce() {
        AppUser user = new AppUser();
        user.setCpf("52998224725");
        Unidade unidade = new Unidade();
        unidade.setNome("Matriz");

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(unidadeRepository.findById(3L)).thenReturn(Optional.of(unidade));

        appUserService.linkUnidade(1L, 3L);
        assertEquals(Set.of(unidade), user.getUnidades());

        ApiException exception = assertThrows(ApiException.class, () -> appUserService.linkUnidade(1L, 3L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }
}
