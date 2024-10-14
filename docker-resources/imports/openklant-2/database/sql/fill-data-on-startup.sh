#!/bin/bash

echo ">>>>  Waiting until Open Klant has initialized the database <<<<"
while true
do
    initiated=$(psql -U openklant -d openklant -t -A -c "SELECT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'accounts_user');")
    if [ "t" = "${initiated}" ]
        then
            echo "Running database setup scripts"
            for file in /docker-entrypoint-initdb.d/sql/*.sql
            do
                echo "Running $file"
                psql -U openklant -d openklant -f $file
            done
            break
        else
            echo "Open Klanten is not initiated yet"
            sleep 5
    fi
done