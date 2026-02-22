package com.example.idp.repo;

import com.example.idp.domain.JwkKeyEntity;
import com.example.idp.domain.JwkStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwkKeyRepository extends JpaRepository<JwkKeyEntity, String> {
    Optional<JwkKeyEntity> findFirstByStatusOrderByCreatedAtDesc(JwkStatus status);

    List<JwkKeyEntity> findByStatusIn(List<JwkStatus> statuses);
}
