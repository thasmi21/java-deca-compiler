package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WINT;

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

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        if(!(ident instanceof Identifier)){
            throw new UnsupportedOperationException("Le tableau n'est pas initialisé !");
        }
        Identifier ident = (Identifier) this.ident;
        ArrayDefinition identDef = (ArrayDefinition) ident.getVariableDefinition();
        if(identDef.getInitialization().getExpression() instanceof ArrayNoInit){
            ArrayNoInit expr = (ArrayNoInit) identDef.getInitialization().getExpression();
            if(expr.getIndices().get(0) instanceof IntLiteral){
                compiler.addInstructionEnd(new LOAD(new ImmediateInteger(((IntLiteral) expr.getIndices().get(0)).getValue()), register));
            }
            else {
                throw new UnsupportedOperationException("pas encore implémenté !");
            }
        }
        else if (identDef.getInitialization().getExpression() instanceof ArrayLiteral){
            ArrayLiteral expr = (ArrayLiteral) identDef.getInitialization().getExpression();
            compiler.addInstructionEnd(new LOAD(new ImmediateInteger(expr.getArray().getSize()), register));
        }
        else {
            throw new UnsupportedOperationException("erreur inattendue");
        }
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        if(!(ident instanceof Identifier)){
            throw new UnsupportedOperationException("Le tableau n'est pas initialisé !");
        }
        Identifier ident = (Identifier) this.ident;
        ArrayDefinition identDef = (ArrayDefinition) ident.getVariableDefinition();
        if(identDef.getInitialization().getExpression() instanceof ArrayNoInit){
            ArrayNoInit expr = (ArrayNoInit) identDef.getInitialization().getExpression();
            if(expr.getIndices().get(0) instanceof IntLiteral){
                compiler.addInstruction(new LOAD(new ImmediateInteger(((IntLiteral) expr.getIndices().get(0)).getValue()), Register.R1));
            }
            else {
                throw new UnsupportedOperationException("pas encore implémenté !");
            }
        }
        else if (identDef.getInitialization().getExpression() instanceof ArrayLiteral){
            ArrayLiteral expr = (ArrayLiteral) identDef.getInitialization().getExpression();
            compiler.addInstruction(new LOAD(new ImmediateInteger(expr.getArray().getSize()), Register.R1));
        }
        else if(identDef.getInitialization().getExpression() instanceof GetArray){
            GetArray expr = (GetArray) identDef.getInitialization().getExpression();
            int size = expr.getSize();
            compiler.addInstruction(new LOAD(new ImmediateInteger(size), Register.R1));
        }
        else {
            throw new UnsupportedOperationException("erreur inattendue");
        }
        compiler.addInstruction(new WINT());
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler){

    }
}
