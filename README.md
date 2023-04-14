# NL Portal Backend Libraries #

![Kotlin 1.7.21](https://img.shields.io/badge/Kotlin-1.7.21-green)
![Spring boot 2.7.5](https://img.shields.io/badge/Spring%20boot-2.7.5-green)

## NL Portal

NL Portal is an application for communicating with citizens and third parties. It is built for use with the Dutch 
'VNG API’s for Zaakgericht Werken' - though not required. The development is based on Common Ground principles. Under 
the motto 'create once, use 340 times', the NL Portal has been built open source, so any (government) organization can 
use and improve it without restrictions.

Starting principles are:
- Open standards, open source under EUPL 1.2;
- Frontend UI based on NL Design System;
- Independent of process- or case management systems;
- Horizontal scalable;
- A-sync communication with other services.

## What is nl-portal backend libraries
This project is the backend, or backend for frontend (BFF), of the NL Portal.

## Contributing
Contributions are welcome! To get you in the right direction consult the [NL Portal documentation](https://docs.nl-portal.nl/readme/contributing) for guidelines on how to contribute.

## License
The source files in this repo are licensed to you under the EUPL 1.2. You can download the license in 23 languages: https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12. If you have any questions about the use of this codebase in a larger work: just ask us.

## More information about NL Portal
For more information check the following links.
- Documentation: https://docs.nl-portal.nl

## Getting started
* Clone `https://github.com/nl-portal/nl-portal-docker-compose` and run the following command: `docker compose up -d`.
    * When supporting ZGW services are needed, like Open Zaak, Objects API and Objecttypes API, the following command should be used: `docker compose --profile zgw up -d`
* Then run the Gradle `bootRun` task for your desired edition (for the GZAC edition this is `app/gzac/application/bootRun`)
