package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.deca.tools.SymbolTable;
import org.apache.commons.lang.Validate;
/**
 * @author gl06
 * @date 01/01/2025
 */
public class DeclVar extends AbstractDeclVar {

    
    final protected AbstractIdentifier type;
    final protected AbstractIdentifier varName;
    final protected AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    public AbstractIdentifier getType() {
        return this.type;
    }


    public AbstractInitialization getInitialization() {
        return this.initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
                                 EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType env = compiler.getEnvironmentType();
        //Vérification du type de base
        Type identType = this.getType().verifyType(compiler);
        // Vérification du type vide
        if (identType.isVoid()) {
            throw new ContextualError("La variable ne peut pas être de type void !", this.getLocation());
        }
        //Dimension de la variable + du type
        int dimVar = countArrayDimensions(this.varName.getName().getName());
        int dimType = countArrayDimensions(this.type.getName().getName());
        int dimTotal = dimVar + dimType;

        // Construction du nouveau type (en prenant en compte la variable)
        Type baseType = getBaseType(compiler, identType);
        Type arrayType = baseType;
        String arrayTypeName = arrayType.getName().getName();
        for (int i = 0; i < dimTotal; i++) {
            arrayTypeName += "[]";
            SymbolTable.Symbol arraySymbol = compiler.createSymbol(arrayTypeName);
            arrayType = new ArrayType(arraySymbol,baseType,i+1);
            // Ajouter le type tableau dans l'environnement global s'il n'existe pas déjà
            if (env.defOfType(arraySymbol) == null) {
                compiler.getEnvironmentType().setTypeDefinition(arraySymbol, new TypeDefinition(arrayType, getLocation()));
            }
        }
        VariableDefinition varDef = new VariableDefinition(arrayType, this.getLocation());

        if (!(initialization instanceof Initialization && ((Initialization) initialization).getExpression() instanceof ArrayLiteral && ((ArrayLiteral) ((Initialization) initialization).getExpression()).implicitDeclaration())){
            initialization.verifyInitialization(compiler, arrayType, localEnv, currentClass);
        }
        else {
            ((Initialization) initialization).getExpression().setType(arrayType);
            initialization.verifyInitialization(compiler, arrayType, localEnv, currentClass);
        }
        String varNameWoBrackets = withoutBrackets(varName.getName().getName());
        try {
            localEnv.declare(compiler.createSymbol(varNameWoBrackets), varDef);
        } catch(EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("La variable "+varName.getName()+" est déjà définie !",this.getLocation());
        }
        this.varName.setName(compiler.createSymbol(varNameWoBrackets));
        this.varName.setDefinition(varDef);
        //this.varName.setType(arrayType);
    }

    // méthode qui récupère le type de base, par ex(int[][][] -> int)
    public static Type getBaseType(DecacCompiler compiler,Type type) throws ContextualError {
        String typeName = type.getName().getName();
        String baseTypeName = extractBaseType(typeName); // Enlève les `[]`
        SymbolTable.Symbol baseSymbol = compiler.createSymbol(baseTypeName);
        TypeDefinition baseTypeDef = compiler.getEnvironmentType().defOfType(baseSymbol);
        return baseTypeDef.getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    public static int countArrayDimensions(String fullName) {
        int count = 0;
        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i) == '[') {
                count++;
            }
        }
        return count; // Chaque "[" correspond à une dimension
    }

        public static String extractBaseType(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }

    public String withoutBrackets(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }
}