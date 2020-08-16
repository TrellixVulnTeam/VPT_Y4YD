#! /bin/bash

echo "Input project name: "
read -p "-> " PROJNAME

cd static
mkdir $PROJNAME
cd $PROJNAME
touch script.js
mkdir imgs
cd ..
cd ..
cd templates
mkdir $PROJNAME
cd $PROJNAME
touch index.html
cd ..
cd ..
cd projBE_modules
touch "${PROJNAME}_BE.py"

