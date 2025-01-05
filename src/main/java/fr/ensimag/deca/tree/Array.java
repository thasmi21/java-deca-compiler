package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import org.apache.commons.lang.Validate;
import java.io.PrintStream;
import java.util.ArrayList;

public class Array extends AbstractExpr{
    private String name;
    private int dimension;
    private int size;
    private AbstractExpr[] coefficients;

    // pour des assignations du type tab = new int[]
    public Array(String name) {
        this.name = removeIntegers(name);
        this.dimension = getDimensionFromName(name);
        this.size = getSizeFromName(name);
    }

//    public Array(ArrayList<Integer> sizes, ArrayList<Type> types) {
//        Validate.notNull(types);
//        Validate.notNull(sizes);
//        Type type = types.get(types.size()-1);
//        Validate.notNull(type);
//        int size = sizes.get(0);
//        if(sizes.size()!=2){
//            sizes.remove(0);
//            types.remove(types.size()-1);
//            if(size <= 0 ){
//                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
//            }
//            this.size = size;
//            this.coefficients = new AbstractExpr[size];
//            for(int i = 0; i < size; i++){
//                this.coefficients[i]= new Array(sizes, types);
//            }
//        }
//        else{
//            if(size <= 0 ){
//                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
//            }
//            this.size = size;
//            this.coefficients = new AbstractExpr[size];
//            for(int i = 0; i < size; i++){
//                this.coefficients[i]= new Array(size, type);
//            }
//        }
//    }

//    // pour des assignations du type tab = new int[] {1,2,3...}
//    public Array(int size, Type type, AbstractExpr[] coefficients){
//        this.type = type;
//        if(size <= 0 ){
//            throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
//        }
//        this.size = size;
//        if(coefficients.length!=size) {
//            throw new IllegalArgumentException("La taille de l'array doit être égale au nombre de coefficients !");
//        }
//        this.coefficients = coefficients;
//    }

    public int getSize() {
        return size;
    }

    public int getSizeFromName(String name) {
        int product = 1;
        boolean hasDigit = false;

        for (char c : name.toCharArray()) {
            if (Character.isDigit(c)) {
                product *= Character.getNumericValue(c);
                hasDigit = true;
            }
        }

        if(hasDigit) {
            return product;
        }
        else{
            return 0;
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

    public void setCoefficients(int i, AbstractExpr coeff) {
        coefficients[i]=coeff;
    }

    public AbstractExpr getCoefficients(int i) {
        return coefficients[i];
    }

    // Je vérifie que tous les coefficients du tableau sont du bon type
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {

        EnvironmentType envType = compiler.getEnvironmentType();
        System.out.println(envType);
        //System.out.println(name);
        Type type = envType.defOfType(compiler.createSymbol(name)).getType();
        setType(type);
//        for (AbstractExpr coefficient : coefficients) {
//            Type type = coefficient.verifyExpr(compiler, localEnv, currentClass);
//            if (type.isSubTypeOf(typeArray)) {
//                throw new ContextualError("Les coefficients ne sont pas tous du bon type. Type souhaité : " + typeArray + ". Type reçu : "+ type, getLocation());
//            }
//        }

        return type;
    }

        public static String removeIntegers(String input) {
        return input.replaceAll("\\d", ""); // Remplace tous les chiffres par une chaîne vide
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
