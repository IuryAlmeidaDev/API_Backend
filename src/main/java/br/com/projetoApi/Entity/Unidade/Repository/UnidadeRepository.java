package br.com.projetoApi.Entity.Unidade.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projetoApi.Entity.Unidade.Model.Unidade;

@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
}
