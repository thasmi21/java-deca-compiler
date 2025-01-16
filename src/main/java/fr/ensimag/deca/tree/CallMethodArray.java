package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CallMethodArray extends AbstractExpr{
    private List<AbstractExpr> tab;
    private String method;

    public CallMethodArray(List<DecaParser.ExprContext> tab, String method) {
        ArrayList<AbstractExpr> tableaux = new ArrayList<>();
        for (DecaParser.ExprContext expr : tab) {
            tableaux.add(expr.tree);
        }
        this.tab = tableaux;
        this.method = method;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        return null;
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        for(AbstractExpr expr : tab) {
            expr.prettyPrint(s,prefix,false);
        }
    }

    @Override
    protected String prettyPrintNode(){
        return method+" Array";
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }
}
