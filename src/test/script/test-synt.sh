#! /bin/sh

# On se place dans le répertoire du projet
cd "$(dirname "$0")"/../../.. || exit 1

for input_file in src/test/deca/syntax/*/*.deca
do
  decac "$input_file"
done
#
#for input_file2 in src/test/deca/syntax/invalid/*.deca
#do
#  decac "$input_file2"
#done