#!/bin/bash

echo ">>>>  Waiting until Open Product has initialized the database <<<<"
while true
do
    initiated=$(psql -U openproduct -d openproduct -t -A -c "SELECT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'authtoken_token');")
    if [ "t" = "${initiated}" ]
        then
            echo "Database is initialized. Running database setup scripts"
            for file in /docker-entrypoint-initdb.d/sql/*.sql
            do
                echo "Running $file"
                psql -U openproduct -d openproduct -f $file
            done
            break
        else
            echo ">>>> Database is not initialized by Open Product yet. Retrying in 10 seconds. <<<<"
            sleep 10
    fi
done
