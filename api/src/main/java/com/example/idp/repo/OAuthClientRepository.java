package com.example.idp.repo;

import com.example.idp.domain.OAuthClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthClientRepository extends JpaRepository<OAuthClientEntity, String> {
}
