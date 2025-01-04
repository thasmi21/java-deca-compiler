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

    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        EnvironmentType env = compiler.getEnvironmentType();

        String fullName = getName().getName();
        String baseName = WithoutBrackets(fullName);

        // Vérifie que le type de base (sans crochets) est défini
        Symbol baseSymbol = compiler.createSymbol(baseName);
        TypeDefinition baseTypeDef = env.defOfType(baseSymbol);
        if (baseTypeDef == null) {
            throw new ContextualError("Base type '" + baseName + "' is not defined", getLocation());
        }

        // Récupération du type de base
        Type currentType = baseTypeDef.getType();

        // Construction progressive des types tableaux
        for (int i = 0; i < dimensions; i++) {
            Symbol arraySymbol = compiler.createSymbol(fullName);
            currentType = new ArrayType(arraySymbol, currentType, i + 1);
        }

        // Associez le dernier type tableau à l'identifiant
        setDefinition(new TypeDefinition(currentType, getLocation()));

        return currentType;
    }

    public String getNameWithoutBrackets() {
        String fullName = getName().getName();
        return WithoutBrackets(fullName);
    }

    @Override
    String prettyPrintNode() {
        return "ArrayIdentifier (" + getNameWithoutBrackets() + ")";
    }


    public String WithoutBrackets(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }
}
