package com.ams.authorizationserver.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ams.authorizationserver.entity.GoogleUser;

@Repository
public interface GoogleUserRepository extends JpaRepository<GoogleUser, Integer> {
	Optional<GoogleUser> findByEmail(String email);
}