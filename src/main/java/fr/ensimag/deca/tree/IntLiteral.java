package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.PrintStream;

/**
 * Integer literal
 *
 * @author gl06
 * @date 01/01/2025
 */
public class IntLiteral extends AbstractExpr {
    public int getValue() {
        return value;
    }

    private int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type intType = compiler.environmentType.INT;
        this.setType(intType);
        return intType;
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        compiler.addInstruction(new LOAD(value, register));

    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r =  GestionRegistre.obtainFreeRegister();
        compiler.addInstruction(new LOAD(value, r));
        Label E = new Label(thenIns.toString().replace('@', '_')+"if");
        Label fin = new Label(thenIns.toString().replace('@', '_')+"fin_if");
        compiler.addInstruction(new CMP(0, r));
        compiler.addInstruction(new BNE(E));
        elseIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        thenIns.codeGenListInst(compiler);
        compiler.addLabel(fin);
        r.setInUse(false);
    }
    @Override
    public void genWhileCode(DecacCompiler compiler, Label label){
        GPRegister r =  GestionRegistre.obtainFreeRegister();

        compiler.addInstruction(new LOAD(value, r));
        compiler.addInstruction(new CMP(0, r)); //si vrai
        compiler.addInstruction(new BNE(label));
        r.setInUse(false);
    }

    @Override
    String prettyPrintNode() {
        return "Int (" + getValue() + ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Integer.toString(value));
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
        compiler.addInstruction(new LOAD(this.getValue(),Register.R1));
        compiler.addInstruction(new WINT());
    }
    @Override
    protected void codeGenPrintX(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(this.getValue(),Register.R1));
        compiler.addInstruction(new WINT());
    }

}
