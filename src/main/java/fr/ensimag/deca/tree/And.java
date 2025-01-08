package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();


        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label sinon = new Label(this.toString().replace('@', '_')+"sinon_and");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_and");
        compiler.addInstruction(new CMP(0, r1));
        compiler.addInstruction(new BEQ(sinon));
        compiler.addInstruction(new CMP(0, r2));
        compiler.addInstruction(new BEQ(sinon));
        thenIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(sinon);
        elseIns.codeGenListInst(compiler);
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }

    @Override
    protected void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();
        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new CMP(0, r1));
        compiler.addInstruction(new BEQ(E));
        compiler.addInstruction(new CMP(0, r2));
        compiler.addInstruction(new BEQ(E));
        compiler.addInstruction(new LOAD(1, register));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addInstruction(new LOAD(0, register));
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        GPRegister r2 = GestionRegistre.obtainFreeRegister();

        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label E = new Label(this.toString().replace('@', '_')+"pt");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_pt");
        compiler.addInstruction(new CMP(0, r1));
        compiler.addInstruction(new BLE(E));
        compiler.addInstruction(new CMP(0, r2));
        compiler.addInstruction(new BLE(E));
        compiler.addInstruction(new WSTR("true"));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addInstruction(new WSTR("false"));
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }

    @Override
    public void genWhileCode(DecacCompiler compiler, Label label){
        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();

        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label fin = new Label(this.toString().replace('@', '_')+"fin_while");
        compiler.addInstruction(new CMP(0, r1));
        compiler.addInstruction(new BEQ(fin));
        compiler.addInstruction(new CMP(0, r2));
        compiler.addInstruction(new BNE(label));
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }


}
