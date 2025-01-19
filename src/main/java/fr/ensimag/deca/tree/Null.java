package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

import javax.lang.model.type.NullType;
import java.io.PrintStream;

public class Null extends AbstractExpr {


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        Type nullType = compiler.environmentType.NULL_TYPE;
        this.setType(nullType);
        return nullType;
    }

    @Override
    String prettyPrintNode() {
        return "null";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("null");
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
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R1));
        compiler.addInstruction(new WSTR("null"));
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        GPRegister reg = GestionRegistre.obtainFreeRegister();
        compiler.addInstruction(new LOAD(new NullOperand(),reg));
        reg.setInUse(false);
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register) {
        compiler.addInstruction(new LOAD(new NullOperand(), register));
    }

}