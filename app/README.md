###GZAC

This is a GZAC Portal edition for local development.
Includes an array of additional services for OpenZaak/OpenFormulieren/OpenKlant

####1 Docker-compose up

Clone external git repo https://github.com/nl-portal/nl-portal-docker-compose

Run in terminal:
```shell
cd gzac-platform
docker-compose up -d
```

####2 Run Spring-boot-application

Run Gradle task in IntelliJ:
```nl-portal-backend-libraries -> app -> gzac -> Tasks -> application -> bootRun```

Run in terminal:
```shell
cd app/gzac
../../gradlew bootRun
```

---

## Keycloak - Test users

Keycloak management can be accessed on http://localhost:8093 with the default credentials of:
>Username: admin  
>Password: admin

Keycloak comes preconfigured with the following users. 

| Name | Property | Username | Password | 
|---|---|---|---|
| Jan Jansen | bsn: 569312863 | burger | burger |
| Bedrijf A | kvk: 14127293 | bedrijf | bedrijf |

### Objects API 

Admin can be accessed on http://localhost:8000.

To create superuser and to add demo data. Terminal:
```shell
docker-compose exec objects-api src/manage.py createsuperuser
docker-compose exec objects-api src/manage.py loaddata demodata
```

Create a token for the access under Home › API authorizations > Token authorizations and use this in the connector config


### ObjectTypes API

Admin can be accessed on http://localhost:8010.

To create superuser and to add demo data. Terminal:
```shell
docker-compose exec objecttypes-api src/manage.py createsuperuser
docker-compose exec objecttypes-api src/manage.py loaddata demodata
```

Create a token for the access under Home › API authorizations > Token authorizations and use this in the connector config


### Open Notificaties

Admin can be accessed on http://localhost:8002.

By default, an admin user is created with the following credentials
>Username: admin  
>Password: admin

### Haal Centraal BRP
Uses a public test server at https://www.haalcentraal.nl/haalcentraal/api/brp.
This server is only accessible with a valid apiKey, use the [haal centraal documentation](https://vng-realisatie.github.io/Haal-Centraal-BRP-bevragen//getting-started) to find out how to get your apiKey

Once you have received an apiKey you can set this in the application.yml at nl-portal.haalcentraal.apiKey, or you can set an environment variable named NLPORTAL_HAALCENTRAAL_APIKEY