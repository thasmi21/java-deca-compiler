package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

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
    protected void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();
        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new CMP(r1, r2));
        compiler.addInstruction(new BNE(E));
        compiler.addInstruction(new LOAD(1, register));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addInstruction(new LOAD(0, register));
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();


        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new CMP(r1, r2));
        compiler.addInstruction(new BNE(E));
        thenIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        elseIns.codeGenListInst(compiler);
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }
    
}
