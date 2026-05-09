package com.maxminiapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String publicBaseUrl;
    private String uploadsDir;
    private Long bootstrapAdminId;
    private String corsOrigins;
    private final Payment payment = new Payment();
    private final Max max = new Max();
    private final Yookassa yookassa = new Yookassa();

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

    public Payment getPayment() {
        return payment;
    }

    public Max getMax() {
        return max;
    }

    public Yookassa getYookassa() {
        return yookassa;
    }

    public static class Payment {
        private boolean mockEnabled;

        public boolean isMockEnabled() {
            return mockEnabled;
        }

        public void setMockEnabled(boolean mockEnabled) {
            this.mockEnabled = mockEnabled;
        }
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

    public static class Yookassa {
        private String shopId;
        private String secretKey;
        private String returnUrl;

        public String getShopId() {
            return shopId;
        }

        public void setShopId(String shopId) {
            this.shopId = shopId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getReturnUrl() {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }
    }
}
