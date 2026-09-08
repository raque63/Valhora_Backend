package com.valhora.backend.settings;

import com.valhora.backend.settings.dto.PaymentSettingsRequest;
import com.valhora.backend.settings.dto.PaymentSettingsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/payment-settings")
public class PaymentSettingsAdminController {

    private final PaymentSettingsService paymentSettingsService;

    public PaymentSettingsAdminController(PaymentSettingsService paymentSettingsService) {
        this.paymentSettingsService = paymentSettingsService;
    }

    @GetMapping
    public PaymentSettingsResponse get() {
        return paymentSettingsService.get();
    }

    @PutMapping
    public PaymentSettingsResponse update(@RequestBody PaymentSettingsRequest request) {
        return paymentSettingsService.update(request);
    }
}
