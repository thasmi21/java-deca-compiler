package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

public class Return extends AbstractInst{

    private AbstractExpr expr;

    public Return(AbstractExpr expr) {
        this.expr = expr;
    }


    public Type verifyInst(DecacCompiler compiler,
                                    EnvironmentExp localEnv, ClassDefinition currentClass,Type type)
            throws ContextualError{
        Type t =expr.verifyExpr(compiler,localEnv,currentClass);
        if (!t.sameType(type)) {
            throw new ContextualError("le return n'est pas du bon type",getLocation());
        }
        return t;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expr.decompile(s);
        s.println(";");
    }


    @Override
    public void prettyPrintChildren(PrintStream s,String indent) {
        expr.prettyPrintChildren(s, indent);
    }

    @Override
    public void iterChildren(TreeFunction f) {
        expr.iter(f);
    }
}
