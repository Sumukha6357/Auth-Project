package com.example.idp.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idp")
public class IdpProperties {
    private Key key = new Key();
    private Lockout lockout = new Lockout();
    private Bootstrap bootstrap = new Bootstrap();
    private Cors cors = new Cors();
    private Security security = new Security();
    private RateLimit rateLimit = new RateLimit();

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Lockout getLockout() {
        return lockout;
    }

    public void setLockout(Lockout lockout) {
        this.lockout = lockout;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public static class Key {
        private String encryptionSecret;
        private int rotationDays = 30;

        public String getEncryptionSecret() {
            return encryptionSecret;
        }

        public void setEncryptionSecret(String encryptionSecret) {
            this.encryptionSecret = encryptionSecret;
        }

        public int getRotationDays() {
            return rotationDays;
        }

        public void setRotationDays(int rotationDays) {
            this.rotationDays = rotationDays;
        }
    }

    public static class Lockout {
        private int maxFailures = 5;
        private int lockMinutes = 15;

        public int getMaxFailures() {
            return maxFailures;
        }

        public void setMaxFailures(int maxFailures) {
            this.maxFailures = maxFailures;
        }

        public int getLockMinutes() {
            return lockMinutes;
        }

        public void setLockMinutes(int lockMinutes) {
            this.lockMinutes = lockMinutes;
        }
    }

    public static class Bootstrap {
        private String adminEmail;
        private String adminPassword;

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }
    }

    public static class Cors {
        private boolean enabled;
        private List<String> allowedOrigins = List.of();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Security {
        private boolean httpsOnly = true;
        private String requiredAudience = "idp-admin-api";
        private boolean failFastSecrets = true;

        public boolean isHttpsOnly() {
            return httpsOnly;
        }

        public void setHttpsOnly(boolean httpsOnly) {
            this.httpsOnly = httpsOnly;
        }

        public String getRequiredAudience() {
            return requiredAudience;
        }

        public void setRequiredAudience(String requiredAudience) {
            this.requiredAudience = requiredAudience;
        }

        public boolean isFailFastSecrets() {
            return failFastSecrets;
        }

        public void setFailFastSecrets(boolean failFastSecrets) {
            this.failFastSecrets = failFastSecrets;
        }
    }

    public static class RateLimit {
        private int tokenEndpointPerMinute = 300;
        private int loginEndpointPerMinute = 120;
        private int introspectEndpointPerMinute = 120;

        public int getTokenEndpointPerMinute() {
            return tokenEndpointPerMinute;
        }

        public void setTokenEndpointPerMinute(int tokenEndpointPerMinute) {
            this.tokenEndpointPerMinute = tokenEndpointPerMinute;
        }

        public int getLoginEndpointPerMinute() {
            return loginEndpointPerMinute;
        }

        public void setLoginEndpointPerMinute(int loginEndpointPerMinute) {
            this.loginEndpointPerMinute = loginEndpointPerMinute;
        }

        public int getIntrospectEndpointPerMinute() {
            return introspectEndpointPerMinute;
        }

        public void setIntrospectEndpointPerMinute(int introspectEndpointPerMinute) {
            this.introspectEndpointPerMinute = introspectEndpointPerMinute;
        }
    }
}
