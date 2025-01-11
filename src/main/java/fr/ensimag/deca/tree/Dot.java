package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;

public class Dot extends AbstractLValue {

    private AbstractExpr select_expr;
    private AbstractIdentifier identifier;

    public Dot(AbstractExpr select_expr, AbstractIdentifier identifier) {
        this.select_expr = select_expr;
        this.identifier = identifier;
    }



    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        //A faire
        Type booleanType = compiler.environmentType.BOOLEAN;
        this.setType(booleanType);
        return booleanType;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        select_expr.decompile(s);
        s.print(".");
        identifier.decompile(s);
        s.println(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        select_expr.iter(f);
        identifier.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        select_expr.prettyPrint(s,prefix,false);
        identifier.prettyPrint(s,prefix,true);
    }


}

