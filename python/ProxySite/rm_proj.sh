#!/bin/bash

echo "Enter project name to delete: "
read -p "-> " PROJNAME

echo "deleting project"

cd static
rm -r $PROJNAME
cd ..
cd templates
rm -r $PROJNAME
cd ..
cd projBE_modules
rm "${PROJNAME}_BE.py"
