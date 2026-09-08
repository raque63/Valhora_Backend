package com.valhora.backend.settings.dto;

public record PaymentSettingsResponse(
        String sinpePhoneNumber,
        String sinpeAccountHolder,
        String bankName,
        String bankAccountNumber,
        String bankAccountHolder,
        String bankAccountType
) {
}
