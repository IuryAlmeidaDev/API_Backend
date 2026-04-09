package br.com.projetoApi.Entity.Unidade.Dto;

import br.com.projetoApi.Entity.Unidade.Model.Unidade.Tipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UnidadeUpdateRequest {

    @NotBlank(message = "Nome da unidade e obrigatorio.")
    private String nome;

    @NotNull(message = "Tipo da unidade e obrigatorio.")
    private Tipo tipo;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
}
