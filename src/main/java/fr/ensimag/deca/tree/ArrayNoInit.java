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

public class ArrayNoInit extends AbstractExpr{
    private String name;
    private List<AbstractExpr> indices;


    public ArrayNoInit(String name, List<DecaParser.ExprContext> indices) {
        Validate.notNull(indices);
        this.name = name;
        this.indices = new ArrayList<>();
        for(DecaParser.ExprContext expr : indices) {
            this.indices.add(expr.tree);
        }
    }

    public List<AbstractExpr> getIndices() {
        return indices;
    }

       @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType envType = compiler.getEnvironmentType();
        //String name=getType().getName().getName();
        Type type = envType.defOfType(compiler.createSymbol(removeIntegers(name))).getType();
        if(type.sameType(getBaseType(compiler,type))){
            throw new ContextualError("L'array n'est pas de la bonne dimension !", getLocation());
        }

        for(AbstractExpr expr : indices) {
            Type typeIndice = expr.verifyExpr(compiler, localEnv, currentClass);
            if(!typeIndice.isInt()){
                throw new ContextualError("Les indices ne sont pas des entiers !", getLocation());
            }
        }
        this.setType(type);
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
