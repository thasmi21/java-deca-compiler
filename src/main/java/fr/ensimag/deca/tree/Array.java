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


    public Array(ArrayList<Integer> sizes, List<AbstractExpr> coefficients) {
        Validate.notNull(sizes);
        this.size = sizes.get(0);
        if (sizes.size() != 2){
            sizes.remove(0);
            if(size <= 0 ){
                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
            }
            this.coefficients = new AbstractExpr[size];
            for (int i = 0; i < size; i++) {
                this.coefficients[i] = new Array(sizes, null);
            }
        }
        else {
            this.coefficients = new AbstractExpr[size];
            for (int i = 0; i < size; i++) {
                this.coefficients[i] = new Array(sizes.get(1), null);
            }
        }

        if(coefficients != null && coefficients.size() != 0){
            this.coefficients = coefficients.toArray(new AbstractExpr[size]);
        }
    }

    public AbstractExpr getCoefficients(int index) {
        return coefficients[index];
    }

    public AbstractExpr[] getCoefficients() {
        return coefficients;
    }

    public Array(int size, List<AbstractExpr> coefficients) {
        if(size ==0 ){
            this.size = 0;
            this.coefficients = new AbstractExpr[0];
        }
        if (size < 0) {
            throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
        }
        this.size = size;
        if(coefficients != null){
            this.coefficients = coefficients.toArray(new AbstractExpr[size]);
        }
        else {
            this.coefficients = new AbstractExpr[size];
            for(int i = 0; i < size; i++){
                this.coefficients[i] = null;
            }
        }
    }

    public int getSize() {
        return size;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType envType = compiler.getEnvironmentType();
        String name=getType().getName().getName();
        Type type = envType.defOfType(compiler.createSymbol(name)).getType();
        if(type.sameType(getBaseType(compiler,type))){
            throw new ContextualError("L'array n'est pas de la bonne dimension !", coefficients[0].getLocation() );
        }
        Type typeChild = envType.defOfType(compiler.createSymbol(name.substring(0, name.length() - 2))).getType();
        // Vérification que tous les coefficients sont du bon type
        for (AbstractExpr coefficient : coefficients) {
            if (coefficient != null) {
                coefficient.setType(typeChild);
                if(coefficient instanceof ArrayLiteral || coefficient instanceof Array){
                    coefficient.verifyExpr(compiler, localEnv, currentClass);
                }
                else{
                    Type typeCoeff = coefficient.verifyExpr(compiler, localEnv, currentClass);
                    if(!typeChild.sameType(typeCoeff)){
                        throw new ContextualError("Le coefficient n'est pas du type souhaité. Type voulu : "+typeChild+". Type reçu : "+typeCoeff, coefficient.getLocation());
                    }
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
        for (AbstractExpr coeff : coefficients){
            if(coeff != null){
                coeff.prettyPrint(s,prefix,false);
            }
            else {
                System.out.println("null");
            }
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }

    public String toString(){
        System.out.println("[");
        for (AbstractExpr coefficient : coefficients) {
            System.out.print(coefficient+",");
        }
        return "]";
    }
}
