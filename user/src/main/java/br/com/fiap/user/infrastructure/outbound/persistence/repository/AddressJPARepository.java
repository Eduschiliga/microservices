package br.com.fiap.user.infrastructure.outbound.persistence.repository;

import br.com.fiap.user.infrastructure.outbound.persistence.entity.AddressJPAEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressJPARepository extends JpaRepository<AddressJPAEntity, String> {
}
