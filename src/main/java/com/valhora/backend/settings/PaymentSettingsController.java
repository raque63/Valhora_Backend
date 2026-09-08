package com.valhora.backend.settings;

import com.valhora.backend.settings.dto.PaymentSettingsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment-settings")
public class PaymentSettingsController {

    private final PaymentSettingsService paymentSettingsService;

    public PaymentSettingsController(PaymentSettingsService paymentSettingsService) {
        this.paymentSettingsService = paymentSettingsService;
    }

    @GetMapping
    public PaymentSettingsResponse get() {
        return paymentSettingsService.get();
    }
}
