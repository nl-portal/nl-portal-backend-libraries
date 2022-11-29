#Portal

This is a Portal for local development.

####1 Docker-compose up

Clone external git repo https://github.com/valtimo-platform/valtimo-docker-profiles

Run in terminal:
```shell
cd valtimo-platform
docker-compose up -d
```

####2 Run Spring-boot-application

Run Gradle task in IntelliJ:
```nl-portal-backend-libraries -> app -> portal -> Tasks -> application -> bootRun```

Run in terminal:
```shell
cd app/portal
../../gradlew bootRun
```

---

## Keycloak - Test users

Keycloak management can be accessed on http://localhost:8093 with the default credentials of:
>Username: admin  
>Password: admin

Keycloak comes preconfigured with the following users. 

| Name | Role | Username | Password |
|---|---|---|---|
| James Vance | ROLE_USER | user | user |
| Asha Miller | ROLE_ADMIN | admin | admin |
| Morgan Finch | ROLE_DEVELOPER | developer | developer |

###Rabbit MQ
You can access rabbitMQ management via localhost:15672 login as guest with guest as credentials