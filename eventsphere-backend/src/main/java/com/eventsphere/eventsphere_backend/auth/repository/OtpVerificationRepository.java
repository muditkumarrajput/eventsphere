package com.eventsphere.eventsphere_backend.auth.repository;

import com.eventsphere.eventsphere_backend.auth.entity.OtpChannel;
import com.eventsphere.eventsphere_backend.auth.entity.OtpPurpose;
import com.eventsphere.eventsphere_backend.auth.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByTargetAndChannelAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
            String target,
            OtpChannel channel,
            OtpPurpose purpose
    );

    List<OtpVerification> findByTargetAndChannelAndPurposeAndVerifiedFalse(
            String target,
            OtpChannel channel,
            OtpPurpose purpose
    );
}
