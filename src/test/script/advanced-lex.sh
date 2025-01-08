#! /bin/sh

# Auteur : Benjamin Sullet
# Version initiale : 08/01/2025

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

for i in ./src/test/deca/syntax/valid/*.deca
do
    echo "$i"
    test_lex "$i" > "${i%.deca}".res
done