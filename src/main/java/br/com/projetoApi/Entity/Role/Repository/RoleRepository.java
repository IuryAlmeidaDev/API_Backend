package br.com.projetoApi.Entity.Role.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projetoApi.Entity.Role.Model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findById(Long id);

    Optional<Role> findByNomeIgnoreCase(String nome);
}
