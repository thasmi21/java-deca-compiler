package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ArrayType;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import org.apache.commons.lang.Validate;

public class ArrayIdentifier extends Identifier {
    private int dimensions;

    public ArrayIdentifier(Symbol name) {
        super(name);
        dimensions = 0;
        for (char caractere : name.getName().toCharArray()) {
            if (caractere == '[') {
                dimensions += 1;
            }
        }
    }

    // j'ai envie de mettre un deuxieme constructeur pour "new int[]" mais pour ça il faut l'attribut name mais il
    // vaut null dans ce cas. Peut être extends Identifier est une mauvaise idée.

//    public int getSize() {
//        return size;
//    }

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

    private String extractBaseType(String fullName) {
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
