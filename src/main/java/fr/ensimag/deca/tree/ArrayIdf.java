package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ArrayType;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;


public class ArrayIdf extends Identifier {
    private int dimensions;
    private ArrayList<Integer> taille;


    public ArrayIdf(Symbol name) {
        super(name);
        taille = new ArrayList<Integer>();
        dimensions = 0;
        boolean entreCrochet = false;
        String nbr="";
        for (char caractere : name.getName().toCharArray()) {
            if (caractere == ']') {
                entreCrochet = false;
                taille.add(parseInt(nbr));
                nbr = "";
            }
            if (entreCrochet) {
                nbr += caractere;
            }
            if (caractere == '[') {
                entreCrochet = true;
                dimensions += 1;
            }
        }
    }

    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        EnvironmentType env = compiler.getEnvironmentType();

        String fullName = getName().getName();
        String baseName = extractBaseType(fullName);

        // Vérifiez que le type de base (sans crochets) est défini
        Symbol baseSymbol = compiler.createSymbol(baseName);
        TypeDefinition baseTypeDef = env.defOfType(baseSymbol);
        if (baseTypeDef == null) {
            throw new ContextualError("Base type '" + baseName + "' is not defined", getLocation());
        }

        // Construisez un type tableau avec les dimensions spécifiées
        Type baseType = baseTypeDef.getType();
        Symbol fullSymbol = compiler.createSymbol(fullName); // Créez le symbole complet
        Type arrayType = new ArrayType(fullSymbol, baseType, dimensions);
        // Associez le type tableau à l'identifiant
        setDefinition(new TypeDefinition(arrayType, getLocation()));
        return arrayType;
    }

    @Override
    String prettyPrintNode() {
        return "Array" + super.prettyPrintNode();
    }

    public static String extractBaseType(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }

    private int countArrayDimensions(String fullName) {
        int count = 0;
        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i) == '[') {
                count++;
            }
        }
        return count; // Chaque "[" correspond à une dimension
    }
}
