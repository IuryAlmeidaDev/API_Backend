package br.com.projetoApi.Common.Security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

    private final Long id;
    private final String cpf;
    private final String nome;
    private final String senhaHash;
    private final boolean ativo;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUser(Long id, String cpf, String nome, String senhaHash, boolean ativo, List<GrantedAuthority> authorities) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.senhaHash = senhaHash;
        this.ativo = ativo;
        this.authorities = authorities;
    }

    public Long getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return cpf;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
