package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import java.io.PrintStream;
import static fr.ensimag.deca.tree.DeclVar.countArrayDimensions;

import org.apache.commons.lang.Validate;

/**
 * Deca Identifier
 *
 * @author gl06
 * @date 01/01/2025
 */
public class  Identifier extends AbstractIdentifier {
    
    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("Identifier " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a class definition.
     */
    @Override
    public ClassDefinition getClassDefinition() {
        try {
            return (ClassDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a class identifier, you can't call getClassDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a method definition.
     */
    @Override
    public MethodDefinition getMethodDefinition() {
        try {
            return (MethodDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a method identifier, you can't call getMethodDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public FieldDefinition getFieldDefinition() {
        try {
            return (FieldDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a field identifier, you can't call getFieldDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public VariableDefinition getVariableDefinition() {
        try {
            return (VariableDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a ExpDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public ExpDefinition getExpDefinition() {
        try {
            return (ExpDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Exp identifier, you can't call getExpDefinition on it");
        }
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Symbol getName() {
        return name;
    }

    private Symbol name;

    public void setName(Symbol name) {
        this.name = name;
    }

    public Identifier(Symbol name) {
        Validate.notNull(name);
        this.name = name;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        ExpDefinition def = localEnv.get(name);
        if (def != null) {
            return def.getType();
        }
        // 🔹 2️⃣ Vérifier si c'est un type connu dans `env_types`
        TypeDefinition typeDef = compiler.getEnvironmentType().defOfType(name);
        if (typeDef != null) {
            ExpDefinition newDef = new VariableDefinition(typeDef.getType(), getLocation());
            // Ajoute la définition du type dans `localEnv`
            try {
                localEnv.declare(getName(), newDef);
            } catch(EnvironmentExp.DoubleDefException e) {
                throw new ContextualError("La variable "+getName()+" est déjà définie !",this.getLocation());
            }
            return typeDef.getType();
        }

        // 🔹 3️⃣ Si l'identifiant n'est trouvé ni dans `localEnv` ni dans `env_types`, lever une erreur
        throw new ContextualError("Identifier '" + name.getName() + "' is not defined", getLocation());
    }

    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * @param compiler contains "env_types" attribute
     */
    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        EnvironmentType env = compiler.getEnvironmentType();
        String fullName = getName().getName();
        String baseName = WithoutBrackets(fullName);
        int dim = countArrayDimensions(fullName);
        // Vérifie que le type de base (sans crochets) est défini
        Symbol baseSymbol = compiler.createSymbol(baseName);
        //setName(baseSymbol);
        TypeDefinition baseTypeDef = env.defOfType(baseSymbol);
        if (baseTypeDef == null) {
            throw new ContextualError("Base type '" + baseName + "' is not defined", getLocation());
        }

        // Récupération du type de base
        Type baseType = baseTypeDef.getType();
        Type currentType = baseType;
        String currentName = baseName;
        // Construction progressive des types tableaux
        for (int i = 0; i < dim; i++) {
            currentName += "[]";
            SymbolTable.Symbol arraySymbol = compiler.createSymbol(currentName);
            currentType = new ArrayType(arraySymbol,baseType,i+1);
            // Ajouter le type tableau dans l'environnement global s'il n'existe pas déjà
            if (env.defOfType(arraySymbol) == null) {
                compiler.getEnvironmentType().setTypeDefinition(arraySymbol, new TypeDefinition(currentType, getLocation()));
            }
        }

        // Associez le dernier type tableau à l'identifiant
        setDefinition(new TypeDefinition(currentType, getLocation()));
        return currentType;
    }

    public String getNameWithoutBrackets() {
        String fullName = getName().getName();
        return WithoutBrackets(fullName);
    }

    public String WithoutBrackets(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }
    private Definition definition;

    public int getDimensions(){
        int dimensions = 0;
        for (char caractere : name.getName().toCharArray()) {
            if (caractere == '[') {
                dimensions += 1;
            }
        }
        return dimensions;
    }
    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(name.toString());
    }

    @Override
    String prettyPrintNode() {
        return "Identifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
    }
}
