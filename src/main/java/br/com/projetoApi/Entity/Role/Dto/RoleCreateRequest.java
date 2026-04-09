package br.com.projetoApi.Entity.Role.Dto;

import jakarta.validation.constraints.NotBlank;

public class RoleCreateRequest {

    @NotBlank(message = "Nome da role e obrigatorio.")
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
