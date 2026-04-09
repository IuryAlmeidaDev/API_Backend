package br.com.projetoApi.Entity.Permission.Dto;

import jakarta.validation.constraints.NotBlank;

public class PermissionCreateRequest {

    @NotBlank(message = "Nome da permissao e obrigatorio.")
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
