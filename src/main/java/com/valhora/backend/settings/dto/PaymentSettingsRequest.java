package com.valhora.backend.settings.dto;

public record PaymentSettingsRequest(
        String sinpePhoneNumber,
        String sinpeAccountHolder,
        String bankName,
        String bankAccountNumber,
        String bankAccountHolder,
        String bankAccountType
) {
}
