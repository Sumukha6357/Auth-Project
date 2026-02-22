package com.example.idp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "jwk_keys")
public class JwkKeyEntity {
    @Id
    private String kid;

    @Column(name = "public_jwk", nullable = false, columnDefinition = "jsonb")
    private String publicJwk;

    @Lob
    @Column(name = "private_jwk_encrypted", nullable = false)
    private byte[] privateJwkEncrypted;

    @Column(nullable = false)
    private String algorithm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JwkStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "rotated_at")
    private Instant rotatedAt;

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getPublicJwk() {
        return publicJwk;
    }

    public void setPublicJwk(String publicJwk) {
        this.publicJwk = publicJwk;
    }

    public byte[] getPrivateJwkEncrypted() {
        return privateJwkEncrypted;
    }

    public void setPrivateJwkEncrypted(byte[] privateJwkEncrypted) {
        this.privateJwkEncrypted = privateJwkEncrypted;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public JwkStatus getStatus() {
        return status;
    }

    public void setStatus(JwkStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getRotatedAt() {
        return rotatedAt;
    }

    public void setRotatedAt(Instant rotatedAt) {
        this.rotatedAt = rotatedAt;
    }
}
