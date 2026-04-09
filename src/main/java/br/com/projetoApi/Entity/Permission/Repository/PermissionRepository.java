package br.com.projetoApi.Entity.Permission.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projetoApi.Entity.Permission.Model.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    Optional<Permission> findByNomeIgnoreCase(String nome);
}
