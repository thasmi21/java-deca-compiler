#! /bin/sh

# Test des différentes options du compilateur

# On se place dans le répertoire du projet
cd "$(dirname "$0")"/../../.. || exit 1

ALL_PASSED=true

for input_file in src/test/deca/codegen/valid/*.deca
do
  decac -p "$input_file" > ""$input_file".res.deca"
  decac -p ""$input_file".res.deca" > ""$input_file".res"

  grep -v '^\s*//' "$input_file" > "$input_file.cleaned"
  grep -v '^\s*//' "$input_file.res" > "$input_file.res.cleaned"

  if diff -w --ignore-blank-lines "$input_file" ""$input_file".res"; then
    echo "$input_file : OK"
  else
    echo "$input_file : Erreur"
    ALL_PASSED=false
  fi

  rm "$input_file.cleaned" "$input_file.res.cleaned"

done

rm src/test/deca/codegen/valid/*.res.deca
rm src/test/deca/codegen/valid/*.res

if [ "$ALL_PASSED" = true ]; then
    echo "Tous les tests de non regressions ont reussi !"
else
    echo "Certains tests ont échoué. Il faut regarder les différences."
fi