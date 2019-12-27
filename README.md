# Goal
This repository contains a Keycloak instance configured with password as first factor and WebAuthn (FIDO2) as second factor. The first factor is a password for which the guidelines mentioned in [NIST SP 800-63b](https://pages.nist.gov/800-63-3/sp800-63b.html) have been followed:
* at least 8 characters in length
* not allowed in case the password is present in a blacklist that contains values known to be commonly-used, expected, or compromised. In casu, the [HIBP API](https://haveibeenpwned.com/API/v3) is used. 

WebAutn can also be used as the sole factor and hence replace passwords completely. However, since I also wanted to showcase the HIBP API, I'm  using WebAuthn as a second factor and password as a first factor. Note that, although functionally very similar, I'm not using U2F (FIDO1) here. WebAuthn (FIDO2) is used. 

# Based on
This work is based on
* https://www.keycloak.org/docs/latest/server_admin/index.html#_webauthn
* https://github.com/CACI-IIG/keycloak-hibp-password-policy 
* https://github.com/thomasdarimont/keycloak
* https://haveibeenpwned.com/API/v3

# Get started
## (optional) Connect to AWS EC2 machine
ssh -i .ssh/mboeynaems_aws.pem ubuntu@ec2-52-17-213-101.eu-west-1.compute.amazonaws.com

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

## creating the keycloak.jks file
Since there is no out of the box way to integrate Let's Encrypt with Keycloak, I am not using Let's Encrypt (yet). 
I followed https://www.keycloak.org/docs/latest/server_installation/index.html#enabling-ssl-https-for-the-keycloak-server

```
sudo docker exec -it keycloak_fido_hibp /bin/sh
keytool -genkey -alias login.michaelboeynaems.com -keyalg RSA -keystore keycloak.jks -keysize 2048
keytool -certreq -alias login.michaelboeynaems.com -keystore keycloak.jks -ext SAN=dns:login.michaelboeynaems.com > keycloak.careq
```

Use an online provider which will hand you a free certificate (note: some providers like sslforfree expect `-----BEGIN CERTIFICATE REQUEST-----` instead of `-----BEGIN NEW CERTIFICATE REQUEST-----`)
* copy the root.crt to the keycloak server
* copy the intermediate.crt to the keycloak server
* copy the certificate.crt to the keycloak server

```
keytool -import -keystore keycloak.jks -file root.crt -alias rootca
keytool -import -keystore keycloak.jks -file intermediate.crt -alias intermediateca
keytool -import -alias login.michaelboeynaems.com -keystore keycloak.jks -file certificate.crt
```

## creating the standalone-ha.xml file
Start from the orignal standalone-ha.xml file, and apply the following changes:
```
sed -i 's/application.keystore/keycloak.jks/g' standalone-ha.xml
sed -i 's/alias="server"/alias="login.michaelboeynaems.com"/g' standalone-ha.xml
sed -i 's/keystore-password="password"/keystore-password="<newpassword>"/g' standalone-ha.xml
sed -i 's/key-password="password"//g' standalone-ha.xml
sed -i 's/generate-self-signed-certificate-host="localhost"//g' standalone-ha.xml
```

# Troubleshooting
* In case only the Yubikey seems to work, make sure that all signature algorithms are permitted by Keycloak