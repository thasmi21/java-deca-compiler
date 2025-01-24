package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.DeclVar;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import org.apache.commons.lang.Validate;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class ArrayLiteral extends AbstractExpr{
    private String name;
    private int dimension;
    private ArrayList<Integer> sizes;
    private Array array;

    public Array getArray() {
        return array;
    }

    public ArrayLiteral(String name, List<DecaParser.ExprContext> coefficients) {
        this.sizes = new ArrayList<>();
        this.name = name;
        if(!thereIsInit()){
            this.name = removeIntegers(name);
            setSizeFromName(name);
            this.dimension = getDimensionFromName(name);
        }
        else{
            this.dimension = getDimensionFromNameInit(name);
            setSizeFromNameInit(name);
        }
        ArrayList<AbstractExpr> coeffs= new ArrayList<>();
        if(coefficients != null){
            for(DecaParser.ExprContext coefficient : coefficients){
                coeffs.add(coefficient.tree);
            }
        }
        if(sizes.size()==1){
            this.array = new Array(sizes.get(0), coeffs);
        }
        else {
            this.array = new Array(sizes, coeffs);
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public ArrayList<Integer> getSize() {
        return sizes;
    }

    public void setSizeFromNameInit(String name) {
        // Vider la liste sizes avant de calculer
        sizes.clear();

        // Cette liste va contenir la taille maximale pour chaque profondeur
        List<Integer> maxCounts = new ArrayList<>();
        // Cette liste va contenir le compteur courant pour chaque profondeur
        List<Integer> currentCounts = new ArrayList<>();

        int depth = 0;

        for (char c : name.toCharArray()) {
            switch (c) {
                case '{':
                    // On entre dans une nouvelle profondeur
                    depth++;
                    // S'assurer que les listes ont la bonne taille
                    while (currentCounts.size() < depth) {
                        currentCounts.add(0);
                    }
                    while (maxCounts.size() < depth) {
                        maxCounts.add(0);
                    }
                    // Réinitialiser le compteur pour cette profondeur
                    currentCounts.set(depth - 1, 0);
                    break;

                case ',':
                    // Nouvelle entrée dans la liste à la profondeur courante
                    if (depth > 0) {
                        currentCounts.set(depth - 1, currentCounts.get(depth - 1) + 1);
                    }
                    break;

                case '}':
                    // On ferme un niveau
                    if (depth > 0) {
                        // Incrément pour le dernier élément (celui qui n'est pas suivi d'une virgule)
                        currentCounts.set(depth - 1, currentCounts.get(depth - 1) + 1);

                        // Mettre à jour le max pour cette profondeur
                        int count = currentCounts.get(depth - 1);
                        if (count > maxCounts.get(depth - 1)) {
                            maxCounts.set(depth - 1, count);
                        }

                        // Quand on remonte, on remet le compteur à zéro
                        currentCounts.set(depth - 1, 0);
                        depth--;
                    }
                    break;

                default:
                    // On ignore les autres caractères (espaces, chiffres, etc.)
                    break;
            }
        }

        // Maintenant, on copie maxCounts dans sizes
        sizes.addAll(maxCounts);
    }



    // Calcul du nombre total d'éléments dans l'ensemble du tableau
    public int getTotalSize() {
        return calculateTotalSize(this.getArray());
    }

    // Méthode récursive pour parcourir toutes les dimensions du tableau
    private int calculateTotalSize(Array array) {
        int totalSize = 0;
        for (int i = 0; i < array.getSize(); i++) {
            Object coefficient = array.getCoefficients(i);
            if (coefficient instanceof ArrayLiteral) {
                // Descendre dans les sous-niveaux si c'est un ArrayLiteral
                totalSize += ((ArrayLiteral) coefficient).getTotalSize();
            } else {
                // Sinon, c'est un élément terminal (par ex : IntLiteral)
                totalSize++;
            }
        }
        return totalSize;
    }

    public void setSizeFromName(String name) {
        boolean entreCrochet = false;
        String nbr = "";
        for (char caractere : name.toCharArray()) {
            if (caractere == ']') {
                entreCrochet = false;
                sizes.add(parseInt(nbr));
                nbr = "";
            }
            if (entreCrochet) {
                nbr += caractere;
            }
            if (caractere == '[') {
                entreCrochet = true;
            }
        }

    }

    public int getDimension() {
        return dimension;
    }


    public int getDimensionFromName(String name) {
        int count = 0;
        for (char c : name.toCharArray()) {
            if (c == '[') {
                count++;
            }
        }
        return count;
    }

    public int getDimensionFromNameInit(String name){
        int count = 0;
        for (char c : name.toCharArray()) {
            if (c == '{') {
                count++;
            }
            else{
                return count;
            }
        }
        return count;
    }
    public boolean thereIsInit() {
        if (name.contains("{")){
            return true;
        }
        return false;
    }

    public boolean implicitDeclaration(){
        if (name.contains("{") && !name.contains("[")){
            return true;
        }
        return false;
    }

    // Je vérifie que tous les coefficients du tableau sont du bon type
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType envType = compiler.getEnvironmentType();
        Type type = null;
        if(!implicitDeclaration()){
            if(!envType.getEnvTypes().containsKey(compiler.createSymbol(removeBracesContent(name)))){
                throw new ContextualError("Type "+removeBracesContent(name)+" non existant !", getLocation());
            }
            type = envType.defOfType(compiler.createSymbol(removeBracesContent(name))).getType();
            setType(type);
            array.setType(type);
            array.verifyExpr(compiler, localEnv, currentClass);
            return type;
            // Vérification que tous les coefficients sont du bon type
        }
        else{
            if(getType() instanceof ArrayType){
                Type typeA = (ArrayType)getType();
                if (typeA.getDimensions() != dimension){
                throw new ContextualError("Le type devrait être "+typeA, getLocation());
                };
            }
            array.setType(getType());
            array.verifyExpr(compiler, localEnv, currentClass);
            return this.getType();
        }


    }

    public String removeBracesContent(String name){
        // Supprimer tout ce qui se trouve après la première occurrence de '{'
        if(name.contains("{")){
            int braceIndex = name.indexOf('{');
            name = name.substring(0, braceIndex);
        }
        return name;
    }

    public static String removeIntegers(String input) {
        return input.replaceAll("\\d", ""); // Remplace tous les chiffres par une chaîne vide
    }

    // méthode qui récupère le type de base, par ex(int[][][] -> int)
    public Type getBaseType(DecacCompiler compiler,Type type) throws ContextualError {
        String typeName = type.getName().getName();
        String baseTypeName = extractBaseType(typeName); // Enlève les `[]`
        SymbolTable.Symbol baseSymbol = compiler.createSymbol(baseTypeName);
        TypeDefinition baseTypeDef = compiler.getEnvironmentType().defOfType(baseSymbol);
        if (baseTypeDef == null) {
            throw new ContextualError("Le type de base '" + baseTypeName + "' n'est pas défini.", getLocation());
        }
        return baseTypeDef.getType();
    }

    public String extractBaseType(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        array.prettyPrintChildren(s,prefix);
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }

}
