package com.ams.authorizationserver.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ams.authorizationserver.entity.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

	Optional<Client> findByClientId(String clientId);
}