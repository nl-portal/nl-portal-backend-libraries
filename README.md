# NL Portal Backend Libraries #

![Kotlin 1.7.21](https://img.shields.io/badge/Kotlin-1.7.21-green)
![Spring boot 2.7.5](https://img.shields.io/badge/Spring%20boot-2.7.5-green)

## What is nl-portal

TODO

## What is nl-portal backend libraries

This project is the backend-for-frontend code for Portals for Dutch municipalities.

The frontend is currently located at the GitHub page of the municipality of The Hague: 
[nl-portal-libraries](https://github.com/Gemeente-DenHaag/nl-portal-libraries)


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