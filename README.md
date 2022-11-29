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
- Compatible with multiple formbuilders;
- Frontend UI based in Nl Design System;
- Independent of underlying process- or case management systems;
- Horizontal scalable;
- A-sync communication with underlying systems.

## What is nl-portal backend libraries

This project is the backend-for-frontend for Portals of Dutch municipalities.

There is a React and Angular frontend version available. The frontend development of the React version is hosted on the GitHub page of the
municipality of The Hague: [nl-portal-libraries](https://github.com/Gemeente-DenHaag/nl-portal-libraries).

More information about NL design can be found on https://nldesignsystem.nl/.


## How does this fit the 5-layers model from the Dutch Common Ground initiative?
This is typically layer-4 - so process and business logic. In itself the application is a layered model as well 
(it has a database for example), as any other application.

### How do I get set up? ###

* There are 2 apps to choose from Portal or GZAC:
  * [Portal](/app/portal) is mostly used for local development
  * [GZAC](/app/gzac) is used for test, acceptance and production environments

## GraphQL playground

To test GraphQL queries and mutations this project comes with the GraphQL playground.
After starting the application the playground can be reached with the following URL:

* Go to localhost:8081/playground

### Example: Mutation - Creating Portal Case

```javascript 
mutation {
    processSubmission(
        submission: { 
            firstName:"aValue" 
        },
        caseDefinitionId: "person"
    ) {
        caseId
    }
}
```

### Example: Query - Portal CaseInstances

```javascript mutation{
query {
    allCaseInstances {
        id,
        submision
    }
}
```

### Example: Query - Portal CaseInstance 

```javascript mutation{
query { 
    getCaseInstance(id: "uuid-id-value") {
        id,
        submision
    }
}
```

### Contribution guidelines ###

* Write tests
* Create pull requests to contribute to the code