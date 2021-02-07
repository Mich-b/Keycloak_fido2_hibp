# Goal
This repository contains a Keycloak instance configured with password as first factor and WebAuthn (FIDO2) as second factor. The first factor is a password for which the guidelines mentioned in [NIST SP 800-63b](https://pages.nist.gov/800-63-3/sp800-63b.html) have been followed:
* at least 8 characters in length
* not allowed in case the password is present in a blacklist that contains values known to be commonly-used, expected, or compromised. In casu, the [HIBP API](https://haveibeenpwned.com/API/v3) is used. 

WebAutn can also be used as the sole factor and hence replace passwords completely. However, since I also wanted to showcase the HIBP API, I'm  using WebAuthn as a second factor and password as a first factor. Note that, although functionally very similar, I'm not using U2F (FIDO1) here. WebAuthn (FIDO2) is used. 

A client configured to use this Keycloak instance is available [here](https://github.com/Mich-b/Keycloak_client_oidc_spa).

# Policies
- UNTESTED: When a refresh token is used, it must adhere to https://tools.ietf.org/html/draft-ietf-oauth-browser-based-apps-07#section-8
- TESTED: Silent renew can also be used, but won't work when third party cookies are disabled. This is by default the case in Safari. Firefox still allows them by default but this may change in the future. To test it in Firefox with all third party cookies disabled use https://support.mozilla.org/en-US/kb/disable-third-party-cookies

# Demo
- currently offline: https://demo.michaelboeynaems.com
- host it on your own

# About token lifetimes
This repo is also used to test token refresh in single page applications. Typically, two refresh strategies can be used:
- silent renew (using an iframe and third-party cookies)
- refresh token (using XHR post)

Keycloak currently issues a refresh token by default when using the authorization code flow (even when offline_access is disabled). This cannot be disabled.

On the other hand, our SPA uses oidc-js, a client OIDC library which tries to do a refresh using the refresh token if such a token is available, before trying a silent renew. 

Combining the default behaviour of Keycloak (issuing a refresh token) with that of oidc-js, this always leads to a renew based on the refresh token. However, for training purposes we wish to also be able to test the silent renew based on iframe (and the impact of blocking third party cookies). 

The following was therefore set up:
* refresh token expires after 1 minute
* access token expires after 5 minutes
* silent renew starts after 200 seconds before AT expiry (when using these defaults that is after 1 minute and 30 seconds, so after the RT has expired).

This triggers the oidc-js library to try a refresh based on silent renew. Of course, should you wish to try out the refresh based on the refresh token, increase the expiry time of the refresh token:
* Keycloak -> Realm settings -> Token -> SSO Session Idle


# Based on
This work is based on
* https://www.keycloak.org/docs/latest/server_admin/index.html#_webauthn
* https://github.com/CACI-IIG/keycloak-hibp-password-policy 
* https://github.com/thomasdarimont/keycloak
* https://haveibeenpwned.com/API/v3

# Get started
## start the container
```
git clone https://github.com/Mich-b/Keycloak_fido2_hibp.git
cd Keycloak_fido2_hibp
mkdir secrets
echo <username> > secrets/user
echo <password> > secrets/password
cp ~/keycloak.jks secrets/keycloak.jks
cp ~/standalone-ha.xml secrets/standalone-ha.xml
make run
```

## When hosting for myself
### Set up hosts file
192.168.119.145	login.portasecura.com

### create the keycloak.jks file (selfhosting only)
- Create a certificate and key using a local CA and move the key and crt to ./secrets
- Copy the intermediate and root in the crt file, in order after the newly created one
- In the following commands, always use a password

Stop all running containers

```
openssl pkcs12 -export -in ./secrets/keycloak.crt -inkey ./secrets/keycloak.key -out ./secrets/keycloak.p12 -name login.portasecura.com
keytool -importkeystore -destkeystore ./secrets/keycloak.jks -srckeystore ./secrets/keycloak.p12 -srcstoretype PKCS12 -alias login.portasecura.com
```

## When hosting for the public
### create the keycloak.jks file (public demo only)
Since there is no out of the box way to integrate Let's Encrypt with Keycloak, I am not using Let's Encrypt (yet). 
I followed https://www.keycloak.org/docs/latest/server_installation/index.html#enabling-ssl-https-for-the-keycloak-server. 

Stop all running containers

```
sudo certbot certonly --standalone -d login.portasecura.com --email contact@portasecura.com
openssl pkcs12 -export -in /etc/letsencrypt/live/login.portasecura.com/fullchain.pem -inkey /etc/letsencrypt/live/login.portasecura.com/privkey.pem -out /etc/letsencrypt/live/login.portasecura.com/pkcs.p12 -name login.portasecura.com
keytool -importkeystore -destkeystore keycloak.jks -srckeystore /etc/letsencrypt/live/login.portasecura.com/pkcs.p12 -srcstoretype PKCS12 -alias login.portasecura.com
```

### Quick renew of cert on AWS machine (public demo only)
```
sudo -s
docker stop $(docker ps -a -q)
certbot certonly --standalone -d login.portasecura.com --email contact@portasecura.com
openssl pkcs12 -export -in /etc/letsencrypt/live/login.portasecura.com/fullchain.pem -inkey /etc/letsencrypt/live/login.portasecura.com/privkey.pem -out /etc/letsencrypt/live/login.portasecura.com/pkcs.p12 -name login.portasecura.com
exit
cd /home/ubuntu/Keycloak_fido2_hibp/secrets
keytool -importkeystore -destkeystore keycloak.jks -srckeystore /etc/letsencrypt/live/login.portasecura.com/pkcs.p12 -srcstoretype PKCS12 -alias login.portasecura.com
cd ..
make run
```

## create the standalone-ha.xml file
Start from the orignal standalone-ha.xml file, and apply the following changes:
```
sed -i 's/application.keystore/keycloak.jks/g' standalone-ha.xml
sed -i 's/alias="server"/alias="login.portasecura.com"/g' standalone-ha.xml
sed -i 's/keystore-password="password"/keystore-password="<newpassword>"/g' standalone-ha.xml
sed -i 's/key-password="password"//g' standalone-ha.xml
sed -i 's/generate-self-signed-certificate-host="localhost"//g' standalone-ha.xml
```

# Clean up everything

```
sudo -s
docker stop $(docker ps -a -q)
docker rm -f $(docker ps -a -q); docker rmi $(docker images -q)
```

# Troubleshooting
* In case only the Yubikey seems to work, make sure that all signature algorithms are permitted by Keycloak