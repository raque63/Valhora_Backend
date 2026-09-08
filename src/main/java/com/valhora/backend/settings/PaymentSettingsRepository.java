package com.valhora.backend.settings;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentSettingsRepository extends JpaRepository<PaymentSettings, UUID> {
}
