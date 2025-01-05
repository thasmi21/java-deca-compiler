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
    private String name;
    private int dimension;
    private int size;
    private List<AbstractExpr> coefficients= new ArrayList<>();

    // pour des assignations du type tab = new int[]
    public Array(String name) {
        this.name = removeIntegers(name);
        this.dimension = getDimensionFromName(name);
        this.size = getSizeFromName(name);
    }

        // pour des assignations du type tab = new int[] {1,2,3...}
    public Array(String name, List<AbstractExpr> coefficients) {
        this.name = removeIntegers(name);
        this.dimension = getDimensionFromName(name);
        this.size = getSizeFromName(name);
        this.coefficients=coefficients;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

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
        coefficients.set(i, coeff);
    }

    public AbstractExpr getCoefficients(int i) {
        return coefficients.get(i);
    }

    // Je vérifie que tous les coefficients du tableau sont du bon type
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType envType = compiler.getEnvironmentType();
        Type type = envType.defOfType(compiler.createSymbol(name)).getType();
        setType(type);
        // Vérification que tous les coefficients sont du bon type
        for (AbstractExpr coefficient : coefficients) {
            Type typeExpr = coefficient.verifyExpr(compiler, localEnv, currentClass);
            if (!typeExpr.isSubTypeOf(type)) {
                throw new ContextualError("Les coefficients ne sont pas tous du bon type. Type souhaité : " + getBaseType(compiler,type) + ". Type reçu : "+ typeExpr, getLocation());
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
