package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

public class LengthArray extends AbstractExpr {
    private AbstractIdentifier ident;
    public LengthArray(AbstractIdentifier ident) {
        this.ident = ident;
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type t = ident.verifyExpr(compiler, localEnv, currentClass);
        if(!t.isArray()){
            new ContextualError("Length ne peut être appliquée qu'à un tableau !", getLocation());
        }
        Type typeInt = compiler.environmentType.INT;
        this.setType(typeInt);
        return typeInt;
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        ident.prettyPrint(s,prefix,true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }
}
