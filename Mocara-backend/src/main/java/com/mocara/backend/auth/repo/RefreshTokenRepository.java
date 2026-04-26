package com.mocara.backend.auth.repo;

import com.mocara.backend.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    List<RefreshTokenEntity> findAllByUserIdAndRevokedFalse(Long userId);

    @Modifying
    @Query("""
            update RefreshTokenEntity rt
            set rt.revoked = true
            where rt.user.id = :userId and rt.revoked = false
            """)
    int revokeAllActiveByUserId(Long userId);
}
