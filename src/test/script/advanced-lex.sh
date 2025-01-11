#! /bin/sh

# Auteur : Benjamin Sullet
# Version modifiée : 10/01/2025

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

for i in ./src/test/deca/syntax/valid/*.deca
do
    # Obtenir le répertoire contenant les fichiers .deca
    dir=$(dirname "$i")
    # Créer le dossier "solution" dans ce répertoire s'il n'existe pas
    mkdir -p "$dir/solution"

    echo "$i"
    test_lex "$i" > "$dir/solution/$(basename "${i%.deca}").res"
done
