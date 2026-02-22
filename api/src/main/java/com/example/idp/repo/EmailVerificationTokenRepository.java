package com.example.idp.repo;

import com.example.idp.domain.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, String> {
}
