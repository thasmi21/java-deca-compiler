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


    // pour des assignations du type tab = new int[] {1,2,3...}
    /*
    public Array(String name, List<AbstractExpr> coefficients) {
        this.name = removeIntegers(name);
        this.dimension = getDimensionFromName(name);
        this.size = getSizeFromName(name);
        this.coefficients=coefficients;
    }
    */



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
                this.coefficients[i]= new Array(sizes.get(1));
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
        String name=getType().getName().getName();
        Type type = envType.defOfType(compiler.createSymbol(name)).getType();
        // Vérification que tous les coefficients sont du bon type
        for (AbstractExpr coefficient : coefficients) {
            if (coefficient != null) {
                coefficient.setType(envType.defOfType(compiler.createSymbol(name.substring(0, name.length() - 2))).getType());
                coefficient.verifyExpr(compiler, localEnv, currentClass);
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
