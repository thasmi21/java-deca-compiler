package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Equals extends AbstractOpExactCmp {

    public Equals(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "==";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
        GestionRegistre.IncCompteurTempUsage();
        compiler.addInstruction(new SUB(GPRegister.R0, register));
        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        if(getRightOperand().getType().isFloat()){
            compiler.addInstruction(new CMP(new ImmediateFloat(0.0F), register));
        }
        else{
            compiler.addInstruction(new CMP(0, register));
        }
        compiler.addInstruction(new BEQ(E));
        compiler.addInstruction(new LOAD(0, register));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addInstruction(new LOAD(1, register));
        compiler.addLabel(fin);
    }
    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register){
        GestionRegistre.pushETpopEnd(compiler, getRightOperand(), getLeftOperand(), register);
        GestionRegistre.IncCompteurTempUsage();
        compiler.addInstructionEnd(new SUB(GPRegister.R0, register));
        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        if(getRightOperand().getType().isFloat()){
            compiler.addInstructionEnd(new CMP(new ImmediateFloat(0.0F), register));
        }
        else{
            compiler.addInstructionEnd(new CMP(0, register));
        }

        compiler.addInstructionEnd(new BEQ(E));
        compiler.addInstructionEnd(new LOAD(0, register));
        compiler.addInstructionEnd(new BRA(fin));
        compiler.addLabelEnd(E);
        compiler.addInstructionEnd(new LOAD(1, register));
        compiler.addLabelEnd(fin);
    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister register = Register.R1;
        GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
        GestionRegistre.IncCompteurTempUsage();
        compiler.addInstruction(new SUB(GPRegister.R0, register));
        Label E = new Label(this.toString().replace('@', '_')+"if");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_if");
        if(getRightOperand().getType().isFloat()){
            compiler.addInstruction(new CMP(new ImmediateFloat(0.0F), register));
        }
        else{
            compiler.addInstruction(new CMP(0, register));
        }
        compiler.addInstruction(new BEQ(E));
        elseIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        thenIns.codeGenListInst(compiler);
        compiler.addLabel(fin);

    }

    @Override
    public void genWhileCode(DecacCompiler compiler, Label label){
        GPRegister register = Register.R1;
        GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
        GestionRegistre.IncCompteurTempUsage();
        compiler.addInstruction(new SUB(GPRegister.R0, register));

        if(getRightOperand().getType().isFloat()){
            compiler.addInstruction(new CMP(new ImmediateFloat(0.0F), register));
        }
        else{
            compiler.addInstruction(new CMP(0, register));
        }
        compiler.addInstruction(new BEQ(label));
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        GPRegister r2 = GestionRegistre.obtainFreeRegister();

        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label E = new Label(this.toString().replace('@', '_')+"pt");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_pt");
        compiler.addInstruction(new CMP(r2, r1));
        compiler.addInstruction(new BEQ(E));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }

    @Override
    public void genIfElseCodeMethod(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister register = Register.R1;
        GestionRegistre.pushETpopEnd(compiler, getRightOperand(), getLeftOperand(), register);
        GestionRegistre.IncCompteurTempUsage();
        compiler.addInstructionEnd(new SUB(GPRegister.R0, register));
        Label E = new Label(this.toString().replace('@', '_')+"if");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_if");
        if(getRightOperand().getType().isFloat()){
            compiler.addInstructionEnd(new CMP(new ImmediateFloat(0.0F), register));
        }
        else{
            compiler.addInstructionEnd(new CMP(0, register));
        }

        compiler.addInstructionEnd(new BEQ(E));
        elseIns.codeGenListInstMethod(compiler);
        compiler.addInstructionEnd(new BRA(fin));
        compiler.addLabelEnd(E);
        thenIns.codeGenListInstMethod(compiler);
        compiler.addLabelEnd(fin);

    }

    @Override
    public void genWhileCodeMethod(DecacCompiler compiler, Label label){
        GPRegister register = Register.R1;
        GestionRegistre.pushETpopEnd(compiler, getRightOperand(), getLeftOperand(), register);
        GestionRegistre.IncCompteurTempUsage();
        compiler.addInstructionEnd(new SUB(GPRegister.R0, register));
        if(getRightOperand().getType().isFloat()){
            compiler.addInstructionEnd(new CMP(new ImmediateFloat(0.0F), register));
        }
        else{
            compiler.addInstructionEnd(new CMP(0, register));
        }

        compiler.addInstructionEnd(new BEQ(label));
    }



    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        GPRegister r2 = GestionRegistre.obtainFreeRegister();

        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label E = new Label(this.toString().replace('@', '_')+"pt");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_pt");
        compiler.addInstruction(new CMP(r2, r1));
        compiler.addInstruction(new BEQ(E));
        compiler.addInstruction(new WSTR("false"));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addInstruction(new WSTR("true"));
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }
    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }

}
