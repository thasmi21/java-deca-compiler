package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BLE;
import fr.ensimag.ima.pseudocode.instructions.BLT;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Lower extends AbstractOpIneq {

    public Lower(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "<";
    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();


        getLeftOperand().evalueExpr(compiler, r1);
        getRightOperand().evalueExpr(compiler, r2);
        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new CMP(r2, r1));
        compiler.addInstruction(new BLT(E));
        elseIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        thenIns.codeGenListInst(compiler);
        compiler.addLabel(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }
}
