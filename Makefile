.phony: build run

NAMESPACE := iam
NAME := keycloak_fido_hibp

TAG := $(NAMESPACE)/$(NAME)

build:
	@docker build -t $(TAG) .

run: build
	@docker run -d --name $(NAME) --rm -it -p 80:8080 -p 443:8443 \
	 -e KEYCLOAK_USER_FILE=/tmp/secrets/user -e KEYCLOAK_PASSWORD_FILE=/tmp/secrets/password \
	 -e KEYCLOAK_IMPORT=/tmp/realmwebauthn.json \
	 -v $(shell pwd):/tmp \
	 $(TAG)

reload:
	@docker exec $(NAME) /opt/jboss/keycloak/bin/jboss-cli.sh --connect reload
