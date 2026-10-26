package com.factory.client.repository;

import com.factory.client.entity.ClientAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAddressRepository extends JpaRepository<ClientAddress, String> {

    List<ClientAddress> findByClientId(String clientId);

    Optional<ClientAddress> findByIdAndClientId(String id, String clientId);

    Optional<ClientAddress> findByClientIdAndIsDefaultTrue(String clientId);
}
