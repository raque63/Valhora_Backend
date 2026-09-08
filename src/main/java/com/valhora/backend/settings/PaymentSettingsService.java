package com.valhora.backend.settings;

import com.valhora.backend.settings.dto.PaymentSettingsRequest;
import com.valhora.backend.settings.dto.PaymentSettingsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentSettingsService {

    private final PaymentSettingsRepository paymentSettingsRepository;

    public PaymentSettingsService(PaymentSettingsRepository paymentSettingsRepository) {
        this.paymentSettingsRepository = paymentSettingsRepository;
    }

    public PaymentSettingsResponse get() {
        return toResponse(getOrCreate());
    }

    @Transactional
    public PaymentSettingsResponse update(PaymentSettingsRequest request) {
        PaymentSettings settings = getOrCreate();
        settings.setSinpePhoneNumber(request.sinpePhoneNumber());
        settings.setSinpeAccountHolder(request.sinpeAccountHolder());
        settings.setBankName(request.bankName());
        settings.setBankAccountNumber(request.bankAccountNumber());
        settings.setBankAccountHolder(request.bankAccountHolder());
        settings.setBankAccountType(request.bankAccountType());
        return toResponse(paymentSettingsRepository.save(settings));
    }

    private PaymentSettings getOrCreate() {
        return paymentSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> paymentSettingsRepository.save(PaymentSettings.builder().build()));
    }

    private PaymentSettingsResponse toResponse(PaymentSettings settings) {
        return new PaymentSettingsResponse(
                settings.getSinpePhoneNumber(),
                settings.getSinpeAccountHolder(),
                settings.getBankName(),
                settings.getBankAccountNumber(),
                settings.getBankAccountHolder(),
                settings.getBankAccountType());
    }
}
