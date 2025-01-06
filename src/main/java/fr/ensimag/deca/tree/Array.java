package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.DeclVar;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import org.apache.commons.lang.Validate;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Array extends AbstractExpr{
    private int size;
    private AbstractExpr[] coefficients;
    private String name;

    // pour des assignations du type tab = new int[] {1,2,3...}
    /*
    public Array(String name, List<AbstractExpr> coefficients) {
        this.name = removeIntegers(name);
        this.dimension = getDimensionFromName(name);
        this.size = getSizeFromName(name);
        this.coefficients=coefficients;
    }
    */

    public Array(ArrayList<Integer> sizes, ArrayList<Type> types) {
        Validate.notNull(types);
        Validate.notNull(sizes);
        Type type = types.get(types.size()-1);
        Validate.notNull(type);
        int size = sizes.get(0);
        if(sizes.size()!=2){
            sizes.remove(0);
            types.remove(types.size()-1);
            if(size <= 0 ){
                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
            }
            this.size = size;
            this.coefficients = new AbstractExpr[size];
            for(int i = 0; i < size; i++){
                this.coefficients[i]= new Array(sizes, types);
            }
        }
        else{
            if(size <= 0 ){
                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
            }
            this.size = size;
            this.coefficients = new AbstractExpr[size];
            for(int i = 0; i < size; i++){
                this.coefficients[i]= new Array(size, type);
            }
        }
    }

    public Array(ArrayList<Integer> sizes) {
        Validate.notNull(sizes);
        int size = sizes.get(0);
        if(sizes.size()!=2){
            sizes.remove(0);
            if(size <= 0 ){
                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
            }
            this.size = size;
            this.coefficients = new AbstractExpr[size];
            for(int i = 0; i < size; i++){
                this.coefficients[i]= new Array(sizes);
            }
        }
        else{
            if(size <= 0 ){
                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
            }
            this.size = size;
            this.coefficients = new AbstractExpr[size];
            for(int i = 0; i < size; i++){
                this.coefficients[i]= new Array(size);
            }
        }
    }

    public Array(int size) {
        // size est-il utile ? je sais pas
        if (size <= 0) {
            throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
        }
        this.size = size;
        this.coefficients = new AbstractExpr[size];
        for (int i = 0; i < size; i++) {
            this.coefficients[i] = null;
        }
    }

    public Array(int size, Type type) {
        //mettre le type comme ça ? Ou alors créer une classe ArrayType qui extends Type
        Validate.notNull(type);
        setType(type);
        // size est-il utile ? je sais pas
        if(size <= 0 ){
            throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
        }
        this.size = size;
        this.coefficients = new AbstractExpr[size];
        for(int i = 0; i < size; i++){
            this.coefficients[i] = null;
        }
    }

/*
    public Array(List<AbstractExpr> coefficients) {
        this.dimension = 1;
        this.size = coefficients.size();
        //ça fonctionne pas psk je peux pas recup le type
//        String name = ""+coefficients.get(0).();
//        for (int i = 0; i < dimension; i++) {
//            name = name + "[]";
//        }
//        this.name = name;
        this.coefficients=coefficients;
    }
*/


    public int getSize() {
        return size;
    }



    // Je vérifie que tous les coefficients du tableau sont du bon type
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType envType = compiler.getEnvironmentType();
        Type type = envType.defOfType(compiler.createSymbol(name)).getType();
        setType(type);
        // Vérification que tous les coefficients sont du bon type
        for (AbstractExpr coefficient : coefficients) {
            if(coefficient.getType().isArray()){
                Array array = (Array)coefficient;
                array.setName();
                Type typeExpr = coefficient.verifyExpr(compiler, localEnv, currentClass);
                if (!typeExpr.isSubTypeOf(type)) {
                    throw new ContextualError("Les coefficients ne sont pas tous du bon type. Type souhaité : " + getBaseType(compiler,type) + ". Type reçu : "+ typeExpr, getLocation());
                }
            }

        }
        return type;
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

    public void setName(String name) {
        this.name = name;
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
