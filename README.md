# Introduction

This project attempts to demonstrate migration rules which are possible now in Data Contracts for Confluent Platform.

Migration rules make it possible to perform what one would generally consider breaking changes to a schemas. How data contracts work in general is detailed in the Confluent [documentation](https://docs.confluent.io/platform/current/schema-registry/fundamentals/data-contracts.html).

## Prerequisites

This demo is built using:
- Java Version 17
- Maven Model Version 4
- Docker and Docker Compose

## Getting Started

This demo has been created with three different versions of a simple person schema represented as JSON schemas. All of them contain the same data but the formats are not compatible in the normal sense.

To shows how it works:
- Open a terminal and navigate to the repository root folder
- Compile the project using Maven: `mvn compile`
- Start the confluent platform in Docker: `docker-compose -f ./docker/docker-compose.yml up -d`
- Open three more terminals and in each start the consumers running the three versions respectively as:
  - `mvn exec:java -D"exec.args=-c -v 1"`
  - `mvn exec:java -D"exec.args=-c -v 2"`
  - `mvn exec:java -D"exec.args=-c -v 3"`
- From the original terminal run the following commands one after the other:
  - `mvn exec:java -D"exec.args=-p -v 1"`
  - `mvn exec:java -D"exec.args=-p -v 2"`
  - `mvn exec:java -D"exec.args=-p -v 3"`

What you should be seeing is that the producers will produce 3 different sets of 15 records that seem similar in content but different schema representations, but the consumers show the same structure for all sets. This is because they are able to use the upgrade and downgrade transform rules. 
