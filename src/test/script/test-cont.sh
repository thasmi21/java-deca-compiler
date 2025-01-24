#! /bin/sh

# On se place dans le répertoire du projet
cd "$(dirname "$0")"/../../.. || exit 1

for input_file in src/test/deca/context/*/*.deca
do
  decac "$input_file"
done

for input_files in src/test/deca/codegen/valid/*.deca
do
  decac -p "$input_files"
  decac -d "$input_files"
  decac -v "$input_files"
  decac -n "$input_files"
done

decac -P src/test/deca/codegen/valid/*.deca

#for input_file2 in src/test/deca/context/invalid/*.deca
#do
#  decac "$input_file2"
#done