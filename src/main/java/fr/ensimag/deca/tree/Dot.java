package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
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
        Type type=select_expr.verifyExpr(compiler, localEnv, currentClass);
        if(type instanceof ClassType){
            ClassType classType = (ClassType)type;
            EnvironmentExp environmentExp= classType.getDefinition().getMembers();
            ExpDefinition expDefinition = environmentExp.get(identifier.getName());
            if (expDefinition != null) {
                Type type1 = identifier.verifyExpr(compiler,localEnv,currentClass);
                this.setType(type1);
                return type1;
            }
            else{
                throw new ContextualError(select_expr.prettyPrint() + "n'a pas d'argument " + identifier.prettyPrint(),getLocation());
            }
        }
        else{
            throw new ContextualError(select_expr.prettyPrint() + "n'est pas une classe il ne peut donc avoir d'argument",getLocation());
        }

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

