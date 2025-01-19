package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class BooleanLiteral extends AbstractExpr {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type booleanType = compiler.environmentType.BOOLEAN;
        this.setType(booleanType);
        return booleanType;
    }

    @Override
    public int getEvalBooleanExpr(){
        return this.getValue() ?1:0;
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register ) {
        int val = this.getValue()?1:0;
        compiler.addInstruction(new LOAD(val, register));
    }

    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register ) {
        int val = this.getValue()?1:0;
        compiler.addInstructionEnd(new LOAD(val, register));
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        if(this.getValue()){
            compiler.addInstruction(new WSTR("true"));
        }
        else{
            compiler.addInstruction(new WSTR("false"));
        }
    }

    @Override
    protected void codeGenPrintMethod(DecacCompiler compiler) {
        if(this.getValue()){
            compiler.addInstructionEnd(new WSTR("true"));
        }
        else{
            compiler.addInstructionEnd(new WSTR("false"));
        }
    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

            GPRegister r =  GestionRegistre.obtainFreeRegister();
            boolean rWasNul = false;
            if(r == null){

                compiler.addInstruction(new PUSH(Register.R1));
                r = Register.R1;
                rWasNul = true;
            }
            int entier = (this.getValue())?1:0;
            compiler.addInstruction(new LOAD(entier, r));
            Label E = new Label(thenIns.toString().replace('@', '_')+"if");
            Label fin = new Label(thenIns.toString().replace('@', '_')+"fin_if");
            compiler.addInstruction(new CMP(0, r));
            compiler.addInstruction(new BNE(E));
            elseIns.codeGenListInst(compiler);
            compiler.addInstruction(new BRA(fin));
            compiler.addLabel(E);
            thenIns.codeGenListInst(compiler);
            compiler.addLabel(fin);
            if(!rWasNul){
                r.setInUse(false);
            }else{
                compiler.addInstruction(new POP(Register.R1));
            }

    }
    @Override
    public void genWhileCode(DecacCompiler compiler, Label label){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        boolean rWasNul = false;
        if(r == null){

            compiler.addInstruction(new PUSH(Register.R1));
            r = Register.R1;
            rWasNul = true;
        }
        int entier = (this.getValue())?1:0;
        compiler.addInstruction(new LOAD(entier, r));
        compiler.addInstruction(new CMP(0, r)); //si vrai
        compiler.addInstruction(new BNE(label));
        if(!rWasNul){
            r.setInUse(false);
        }else{
            compiler.addInstruction(new POP(Register.R1));
        }
    }
    @Override
    public void genIfElseCodeMethod(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r =  GestionRegistre.obtainFreeRegister();
        boolean rWasNul = false;
        if(r == null){

            compiler.addInstructionEnd(new PUSH(Register.R1));
            r = Register.R1;
            rWasNul = true;
        }
        int entier = (this.getValue())?1:0;
        compiler.addInstructionEnd(new LOAD(entier, r));
        Label E = new Label(thenIns.toString().replace('@', '_')+"if");
        Label fin = new Label(thenIns.toString().replace('@', '_')+"fin_if");
        compiler.addInstructionEnd(new CMP(0, r));
        compiler.addInstructionEnd(new BNE(E));
        elseIns.codeGenListInstMethod(compiler);
        compiler.addInstructionEnd(new BRA(fin));
        compiler.addLabelEnd(E);
        thenIns.codeGenListInstMethod(compiler);
        compiler.addLabelEnd(fin);
        if(!rWasNul){
            r.setInUse(false);
        }else{
            compiler.addInstructionEnd(new POP(Register.R1));
        }

    }
    @Override
    public void genWhileCodeMethod(DecacCompiler compiler, Label label){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        boolean rWasNul = false;
        if(r == null){

            compiler.addInstructionEnd(new PUSH(Register.R1));
            r = Register.R1;
            rWasNul = true;
        }
        int entier = (this.getValue())?1:0;
        compiler.addInstructionEnd(new LOAD(entier, r));
        compiler.addInstructionEnd(new CMP(0, r)); //si vrai
        compiler.addInstructionEnd(new BNE(label));
        if(!rWasNul){
            r.setInUse(false);
        }else{
            compiler.addInstructionEnd(new POP(Register.R1));
        }
    }
    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }
    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }
    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Boolean.toString(value));
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
    String prettyPrintNode() {
        return "BooleanLiteral (" + value + ")";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        GPRegister reg = GestionRegistre.obtainFreeRegister(); // On va partir du principe que ce reg n'est jamais null
        int entier = (this.getValue())?1:0;
        compiler.addInstruction(new LOAD(entier, reg));
        reg.setInUse(false);
        //throw new UnsupportedOperationException("Not finished yet en Cours");
    }
}
