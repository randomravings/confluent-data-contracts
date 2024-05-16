#!/bin/bash
docker exec -i mssql /opt/mssql-tools/bin/sqlcmd -U sa -P Passw0rd << EOF
CREATE DATABASE [test]
GO
EOF