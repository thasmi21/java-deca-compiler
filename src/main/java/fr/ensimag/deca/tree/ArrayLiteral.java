package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.DeclVar;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
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
            setSizeFromNameInit(name);
            this.dimension = getDimensionFromNameInit(name);
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
        boolean entreAccolades = false;
        int counter = 0;
        for (char caractere : name.toCharArray()) {
            if (caractere == '}') {
                entreAccolades = false;
                sizes.add(counter);
            }
            if(entreAccolades && caractere== ','){
                counter += 1;
            }
            if (caractere == '{') {
                entreAccolades = true;
                counter = 0;
            }
        }
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

    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }

}
