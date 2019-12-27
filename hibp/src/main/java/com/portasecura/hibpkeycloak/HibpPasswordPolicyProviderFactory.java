package com.portasecura.hibpkeycloak;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PasswordPolicyProviderFactory;

public class HibpPasswordPolicyProviderFactory implements PasswordPolicyProviderFactory {

    public static final String ID = "HIBP";

    public PasswordPolicyProvider create(KeycloakSession session) {
        return new HibpPasswordPolicyProvider(session.getContext());
    }

    public void init(Config.Scope config) {
    }

    public void postInit(KeycloakSessionFactory factory) {

    }

    public void close() {

    }

    public String getId() {
        return ID;
    }

    public String getDisplayName() {
        return "HIBP Data Breaches. Format: <allowedNumberOfOccurrences>;<api-uri>";
    }

    public String getConfigType() {
        return PasswordPolicyProvider.STRING_CONFIG_TYPE;
    }

    public String getDefaultConfigValue() {
        return "0;https://api.pwnedpasswords.com/range/";
    }

    public boolean isMultiplSupported() {
        return false;
    }

}
