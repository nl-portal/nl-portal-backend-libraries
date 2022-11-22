# NL Portal Backend Libraries #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* There are 2 apps to choose from Portal or GZAC see readme's to get started

### GraphQL playground

* Go to localhost:8081/playground to play around with the graphql queries and mutations

#### Example: Mutation - Creating Portal Case

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

#### Example: Query - Portal CaseInstances

```javascript mutation{
query {
    allCaseInstances {
        id,
        submision
    }
}
```

#### Example: Query - Portal CaseInstance 

```javascript mutation{
query { 
    getCaseInstance(id: "uuid-id-value") {
        id,
        submision
    }
}
```

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact