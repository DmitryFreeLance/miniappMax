package com.maxminiapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String publicBaseUrl;
    private String uploadsDir;
    private Long bootstrapAdminId;
    private String corsOrigins;
    private String defaultPaymentDetails;
    private BigDecimal cityDeliveryFee = new BigDecimal("1000.00");
    private final Max max = new Max();

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getUploadsDir() {
        return uploadsDir;
    }

    public void setUploadsDir(String uploadsDir) {
        this.uploadsDir = uploadsDir;
    }

    public Long getBootstrapAdminId() {
        return bootstrapAdminId;
    }

    public void setBootstrapAdminId(Long bootstrapAdminId) {
        this.bootstrapAdminId = bootstrapAdminId;
    }

    public String getCorsOrigins() {
        return corsOrigins;
    }

    public void setCorsOrigins(String corsOrigins) {
        this.corsOrigins = corsOrigins;
    }

    public String getDefaultPaymentDetails() {
        return defaultPaymentDetails;
    }

    public void setDefaultPaymentDetails(String defaultPaymentDetails) {
        this.defaultPaymentDetails = defaultPaymentDetails;
    }

    public BigDecimal getCityDeliveryFee() {
        return cityDeliveryFee;
    }

    public void setCityDeliveryFee(BigDecimal cityDeliveryFee) {
        this.cityDeliveryFee = cityDeliveryFee;
    }

    public Max getMax() {
        return max;
    }

    public static class Max {
        private String token;
        private String webhookSecret;
        private String miniappUrl;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

        public String getMiniappUrl() {
            return miniappUrl;
        }

        public void setMiniappUrl(String miniappUrl) {
            this.miniappUrl = miniappUrl;
        }
    }

}
