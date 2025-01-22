package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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

    private Set<Symbol> S;
    private final Map<Symbol, TypeDefinition> envTypes;
    private EnvironmentExp envExp;
    public EnvironmentType(DecacCompiler compiler) {

        envExp = new EnvironmentExp(null);
        envTypes = new HashMap<Symbol, TypeDefinition>();
        S = new HashSet<Symbol>();



        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));
        S.add(intSymb);

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));
        S.add(floatSymb);

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));
        S.add(voidSymb);

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));
        S.add(booleanSymb);

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.
        S.add(stringSymb);

        Symbol nullSymb = compiler.createSymbol("null");
        NULL_TYPE = new NullType(nullSymb);
        S.add(nullSymb);

        Symbol objetSymb=compiler.createSymbol("objet");
        OBJET=new ClassType(objetSymb);
        OBJET.definition=new ClassDefinition(OBJET,Location.BUILTIN,null);
        Signature signature= new Signature();
        signature.add(OBJET);
        MethodDefinition methodDefinition= new MethodDefinition(BOOLEAN,Location.BUILTIN,signature,0);
        OBJET.definition.incNumberOfMethods();
        OBJET.definition.getMembers().getLocalEnv().put(compiler.createSymbol("equals"),methodDefinition);
        envTypes.put(objetSymb,OBJET.getDefinition() );
        S.add(objetSymb);
    }

//    private int nbClasses;
//
//    public int getNbClasses() {
//        return nbClasses;
//    }
//
//    public void setNbClasses(int nbClasses) {
//        this.nbClasses = nbClasses;
//    }

    public void putClass(Symbol symbol,Symbol extend,Location location)throws ContextualError{
        if (S.contains(symbol) || !S.contains(extend)) {
            throw new ContextualError("Cannot put a type of " + symbol + " and extend " + extend,location);
        }
        TypeDefinition type = envTypes.get(extend);
        if(type.isClass()){
            S.add(symbol);
            ClassDefinition classDef = (ClassDefinition)type;
            envTypes.put(symbol, new ClassType(symbol,location,classDef).getDefinition());
            //setNbClasses(getNbClasses()+1); // On a une classe de plus
        }
        else {
            throw new ContextualError("Cannot put a type of " + symbol + " and extend " + extend,location);
        }
    }

    public void putClass(Symbol symbol,Location location)throws ContextualError{
        if (envTypes.containsKey(symbol)) {
            throw new ContextualError("Cannot put a type of " + symbol,location);
        }
        S.add(symbol);
        envTypes.put(symbol, new ClassType(symbol,location,OBJET.getDefinition()).getDefinition());
        //setNbClasses(getNbClasses()+1); // On ajoute une nouvelle classe
    }

    public EnvironmentExp getEnvironmentMain() {
        return envExp;
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
            return;
        }
        envTypes.put(s, typeDef);
    }

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final StringType  STRING;
    public final BooleanType BOOLEAN;
    public final NullType NULL_TYPE;
    public final ClassType    OBJET;
}
