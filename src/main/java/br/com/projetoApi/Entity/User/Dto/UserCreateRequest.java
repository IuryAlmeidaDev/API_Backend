package br.com.projetoApi.Entity.User.Dto;

import jakarta.validation.constraints.NotBlank;

public class UserCreateRequest {

    @NotBlank(message = "CPF e obrigatorio.")
    private String cpf;

    @NotBlank(message = "Nome e obrigatorio.")
    private String nome;

    @NotBlank(message = "Senha e obrigatoria.")
    private String senha;

    private Boolean ativo;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
