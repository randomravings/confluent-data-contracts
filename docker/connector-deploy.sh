#!/bin/bash
curl -X PUT \
      -H 'Content-Type: application/json' \
      --data '{
            "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
            "value.converter": "io.confluent.connect.json.JsonSchemaConverter",
            "value.converter.schema.registry.url": "http://schema-registry:8081",
            "connection.url": "jdbc:sqlserver://mssql:1433;databaseName=test;",
            "connection.user": "sa",
            "connection.password": "Passw0rd",
            "tasks.max": "1",
            "topics":"test",
            "auto.create": "true",
            "group.id": "sink-cg",
            "consumer.use.latest.with.metadata": "application.major.version=1"
      }' \
      http://localhost:8083/connectors/mssql-sink-connector/config | jq .