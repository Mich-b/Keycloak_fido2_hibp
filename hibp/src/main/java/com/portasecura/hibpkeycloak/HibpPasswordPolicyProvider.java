package com.portasecura.hibpkeycloak;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PolicyError;

import com.portasecura.hibpkeycloak.HaveIBeenPwned.ApiException;

public class HibpPasswordPolicyProvider implements PasswordPolicyProvider {
    private static final Logger LOG = Logger.getLogger(HibpPasswordPolicyProvider.class);

    private KeycloakContext context;
    private HaveIBeenPwned haveIBeenPwned;

    public HibpPasswordPolicyProvider(KeycloakContext context) {
        this.context = context;
        this.haveIBeenPwned = new HaveIBeenPwned();
    }

    public PolicyError validate(String user, String password) {
        String configuration = context.getRealm().getPasswordPolicy()
                .getPolicyConfig(HibpPasswordPolicyProviderFactory.ID);
        String[] configurationSplit = configuration.split(";");
        Integer allowedBreaches = Integer.parseInt(configurationSplit[0]);
        String apiUrl = configurationSplit[1];     
        LOG.debug("allowedBreaches set to " + allowedBreaches);
        LOG.debug("api url set to " + apiUrl);

        try {
            int breachOccurrences = haveIBeenPwned.lookup(password, apiUrl);
            if (breachOccurrences > allowedBreaches) {
                return new PolicyError("The entered password is present in "+breachOccurrences+" data breaches and cannot be used");
            }
        } catch (ApiException e) {
            LOG.error("Failed to lookup password with HIBP, allowing password", e);
            return new PolicyError("The connection to the HIBP API failed, potentially we are being rate limited. Please contact michael.boeynaems at portasecura.com");
        }
        return null;
    }

    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return validate(user.getUsername(), password);
    }


    public Object parseConfig(String value) {
        return value;
    }

    public void close() {
    }

}
