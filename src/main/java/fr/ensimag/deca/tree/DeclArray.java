package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import org.apache.commons.lang.Validate;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class DeclArray extends DeclVar {

    public DeclArray(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        super(type, varName, initialization);
    }

    public DeclArray(AbstractIdentifier type, int dimension, AbstractIdentifier varName, AbstractInitialization initialization) {
        super(type, varName, initialization);
        Validate.isTrue(dimension > 0);
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
                                 EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType env = compiler.getEnvironmentType();
        //Vérification du type de base
        Type baseType = this.getType().verifyType(compiler);
        // Vérification du type vide
        if (baseType.isVoid()) {
            throw new ContextualError("La variable ne peut pas être de type void !", this.getLocation());
        }
        //Dimension de la variable
        int dimVar = countArrayDimensions(this.varName.getName().getName());
        // Construction du nouveau type (en prenant en compte la variable)
        Type arrayType = baseType;
        String arrayTypeName = baseType.getName().getName();
        for (int i = 0; i < dimVar; i++) {
            arrayTypeName += "[]";
            Symbol arraySymbol = compiler.createSymbol(arrayTypeName);
            arrayType = new ArrayType(arraySymbol,arrayType,i+1);
            // Ajouter le type tableau dans l'environnement global s'il n'existe pas déjà
            if (env.defOfType(arraySymbol) == null) {
                compiler.getEnvironmentType().setTypeDefinition(arraySymbol, new TypeDefinition(arrayType, getLocation()));
            }
        }

        VariableDefinition varDef = new VariableDefinition(arrayType, this.getLocation());
        initialization.verifyInitialization(compiler, arrayType, localEnv, currentClass);
        try {
            localEnv.declare(this.varName.getName(), varDef);
        } catch(EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("La variable "+varName.getName()+" est déjà définie !",this.getLocation());
        }
        this.varName.setDefinition(varDef);
        this.varName.setType(arrayType);
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

    public int countArrayDimensions(String fullName) {
        int count = 0;
        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i) == '[') {
                count++;
            }
        }
        return count; // Chaque "[" correspond à une dimension
    }
}
