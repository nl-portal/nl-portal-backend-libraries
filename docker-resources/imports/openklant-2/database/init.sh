#!/bin/bash

echo ">>>>  Starting Open Klant data import script  <<<<"

sh /docker-entrypoint-initdb.d/sql/fill-data-on-startup.sh  &