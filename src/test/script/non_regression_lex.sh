#!/bin/bash

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

INPUT_DIR="./src/test/deca/syntax/valid"
RES_DIR="./src/test/deca/syntax/valid/solution"
TEMP_DIR="./src/test/deca/syntax/valid/temp"

mkdir -p "$TEMP_DIR"

ALL_PASSED=true
FAILED=0

for input_file in "$INPUT_DIR"/*.deca
do
    base_name=$(basename "$input_file")
    echo "$input_file"
    echo "$base_name"

    test_lex "$input_file" > "$TEMP_DIR/$(basename "${input_file%.deca}").res"

    if diff "$TEMP_DIR/$(basename "${input_file%.deca}").res" "$RES_DIR/$(basename "${input_file%.deca}").res" > /dev/null; then
        echo "Test $base_name : OK"
    else
        echo "Test $base_name : ECHEC"
        ALL_PASSED=false
        FAILED=$((FAILED+1))
    fi

done
rm -rf "$TEMP_DIR"
if [ "$ALL_PASSED" = true ]; then
    echo "Tous les tests de non regressions ont reussi !"
else
    echo "Certains tests ont échoué. Il faut regarder les différences."
fi
exit $FAILED