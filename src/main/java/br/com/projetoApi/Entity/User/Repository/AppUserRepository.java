package br.com.projetoApi.Entity.User.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projetoApi.Entity.User.Model.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = { "roles", "roles.permissions", "unidades" })
    Optional<AppUser> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
