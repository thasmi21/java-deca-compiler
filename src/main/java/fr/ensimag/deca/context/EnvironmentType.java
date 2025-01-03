package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import java.util.HashMap;
import java.util.Map;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

// A FAIRE: étendre cette classe pour traiter la partie "avec objet" de Déca
/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl06
 * @date 01/01/2025
 */
public class EnvironmentType {

    private final Map<Symbol, TypeDefinition> envTypes;
    public EnvironmentType(DecacCompiler compiler) {
        
        envTypes = new HashMap<Symbol, TypeDefinition>();
        
        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.
    }

    public Map<Symbol, TypeDefinition> getEnvTypes() {
        return envTypes;
    }

    public TypeDefinition defOfType(Symbol s) {

        return envTypes.get(s);
    }

    /**
     * Ajoute un type dynamique dans l'environnement.
     * @param s Le symbole du type
     * @param typeDef La définition du type
     * @throws IllegalArgumentException si le type existe déjà
     */
    public void setTypeDefinition(Symbol s, TypeDefinition typeDef) {
        if (envTypes.containsKey(s)) {
            throw new IllegalArgumentException("Le type '" + s.getName() + "' est déjà défini dans l'environnement.");
        }
        envTypes.put(s, typeDef);
    }

    // Méthode qui sert a debugger
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EnvironmentType contains the following keys:\n");

        for (Symbol symbol : envTypes.keySet()) {
            sb.append(" - ").append(symbol.getName()).append("\n");
        }

        return sb.toString();
    }

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final StringType  STRING;
    public final BooleanType BOOLEAN;
}
