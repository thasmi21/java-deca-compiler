package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

public class SetArray extends AbstractExpr {
    private GetArray getArray;
    private AbstractExpr expr;

    public SetArray(GetArray getArray, AbstractExpr expr) {
        this.getArray = getArray;
        this.expr = expr;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type typeVoulu = getArray.verifyExpr(compiler, localEnv, currentClass);
        Type typeExpr = expr.verifyExpr(compiler, localEnv, currentClass);
        if(!typeVoulu.sameType(typeExpr)) {
            throw new ContextualError("Types incompatibles lors de l'assignation ! Attendu "+ typeVoulu+". Type trouvé : "+typeExpr, getLocation());
        }
        return typeExpr;
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        getArray.prettyPrint(s, prefix, true);
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }
}
