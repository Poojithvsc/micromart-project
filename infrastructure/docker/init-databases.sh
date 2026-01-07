#!/bin/bash
# ============================================================================
# PostgreSQL Multiple Database Initialization Script
# ============================================================================
# This script creates multiple databases from the POSTGRES_MULTIPLE_DATABASES
# environment variable. Databases are comma-separated.
# ============================================================================

set -e
set -u

function create_database() {
    local database=$1
    echo "Creating database '$database'..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
    echo "Database '$database' created successfully."
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Multiple databases requested: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
        create_database $db
    done
    echo "All databases created successfully!"
fi
