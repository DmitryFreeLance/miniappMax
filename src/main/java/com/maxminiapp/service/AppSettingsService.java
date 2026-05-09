package com.maxminiapp.service;

import com.maxminiapp.model.AppSetting;
import com.maxminiapp.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppSettingsService {

    public static final String PAYMENT_DETAILS_KEY = "payment.details";

    private final AppSettingRepository appSettingRepository;

    public AppSettingsService(AppSettingRepository appSettingRepository) {
        this.appSettingRepository = appSettingRepository;
    }

    @Transactional(readOnly = true)
    public String getPaymentDetails() {
        return appSettingRepository.findById(PAYMENT_DETAILS_KEY)
                .map(AppSetting::getValue)
                .orElse("");
    }

    @Transactional
    public void setPaymentDetails(String paymentDetails) {
        AppSetting setting = appSettingRepository.findById(PAYMENT_DETAILS_KEY)
                .orElseGet(() -> {
                    AppSetting created = new AppSetting();
                    created.setKey(PAYMENT_DETAILS_KEY);
                    return created;
                });
        setting.setValue(paymentDetails);
        appSettingRepository.save(setting);
    }

    @Transactional
    public void seedPaymentDetailsIfEmpty(String defaultValue) {
        String normalized = defaultValue == null ? "" : defaultValue.trim();
        if (normalized.isEmpty()) {
            return;
        }
        if (getPaymentDetails().isBlank()) {
            setPaymentDetails(normalized);
        }
    }
}
