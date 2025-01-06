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

import static java.lang.Integer.parseInt;

public class ArrayLiteral extends AbstractExpr{
    private String name;
    private int dimension;
    private ArrayList<Integer> sizes;
    private Array array;


    // pour des assignations du type tab = new int[]
    public ArrayLiteral(String name) {
        this.name = removeIntegers(name);
        this.dimension = getDimensionFromName(name);
        this.sizes = new ArrayList<>();
        getSizeFromName(name);
        this.array = new Array(sizes);
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

    public void getSizeFromName(String name) {
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


    // Je vérifie que tous les coefficients du tableau sont du bon type
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType envType = compiler.getEnvironmentType();
        Type type = envType.defOfType(compiler.createSymbol(name)).getType();
        setType(type);
        array.setType(type);
        // Vérification que tous les coefficients sont du bon type
        Type typeExpr = array.verifyExpr(compiler, localEnv, currentClass);
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
