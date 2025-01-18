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
    public void genIfElseCodeMethod(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();


        getLeftOperand().evalueExprEnd(compiler, r1);
        getRightOperand().evalueExprEnd(compiler, r2);
        Label sinon = new Label(this.toString().replace('@', '_')+"sinon_and");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_and");
        compiler.addInstructionEnd(new CMP(0, r1));
        compiler.addInstructionEnd(new BEQ(sinon));
        compiler.addInstructionEnd(new CMP(0, r2));
        compiler.addInstructionEnd(new BEQ(sinon));
        thenIns.codeGenListInstMethod(compiler);
        compiler.addInstructionEnd(new BRA(fin));
        compiler.addLabelEnd(sinon);
        elseIns.codeGenListInstMethod(compiler);
        compiler.addLabelEnd(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();
        boolean r1EtaitNull = false;
        boolean r2EtaitNull = false;
        if(r1 == null){
            r1EtaitNull = true;
            r1 = register;
        }
        if(r2 == null){
            r2EtaitNull = true;
            //getRightOperand().evalueExpr(compiler, r2);
        }
        getLeftOperand().evalueExpr(compiler, r1);


        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new CMP(0, r1));
        compiler.addInstruction(new BEQ(E));
        if(r2EtaitNull){
            // si r2 etait nul, il faut charger exp2 dans register
            r2 = register;
        }
        getRightOperand().evalueExpr(compiler, r2);
        compiler.addInstruction(new CMP(0, r2));
        compiler.addInstruction(new BEQ(E));
        compiler.addInstruction(new LOAD(1, register));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addInstruction(new LOAD(0, register));
        compiler.addLabel(fin);
        if(!r1EtaitNull){
            r1.setInUse(false);
        }
        if(!r2EtaitNull){
            r2.setInUse(false);
        }

    }


    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register){
        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();
        boolean r1EtaitNull = false;
        boolean r2EtaitNull = false;
        if(r1 == null){
            r1EtaitNull = true;
            r1 = register;
        }
        if(r2 == null){
            r2EtaitNull = true;
            //getRightOperand().evalueExpr(compiler, r2);
        }
        getLeftOperand().evalueExprEnd(compiler, r1);


        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstructionEnd(new CMP(0, r1));
        compiler.addInstructionEnd(new BEQ(E));
        if(r2EtaitNull){
            // si r2 etait nul, il faut charger exp2 dans register
            r2 = register;
        }
        getRightOperand().evalueExprEnd(compiler, r2);
        compiler.addInstructionEnd(new CMP(0, r2));
        compiler.addInstructionEnd(new BEQ(E));
        compiler.addInstructionEnd(new LOAD(1, register));
        compiler.addInstructionEnd(new BRA(fin));
        compiler.addLabelEnd(E);
        compiler.addInstructionEnd(new LOAD(0, register));
        compiler.addLabelEnd(fin);
        if(!r1EtaitNull){
            r1.setInUse(false);
        }
        if(!r2EtaitNull){
            r2.setInUse(false);
        }

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

    @Override
    public void genWhileCodeMethod(DecacCompiler compiler, Label label){
        GPRegister r1 =  GestionRegistre.obtainFreeRegister();
        GPRegister r2 =  GestionRegistre.obtainFreeRegister();

        getLeftOperand().evalueExprEnd(compiler, r1);
        getRightOperand().evalueExprEnd(compiler, r2);
        Label fin = new Label(this.toString().replace('@', '_')+"fin_while");
        compiler.addInstructionEnd(new CMP(0, r1));
        compiler.addInstructionEnd(new BEQ(fin));
        compiler.addInstructionEnd(new CMP(0, r2));
        compiler.addInstructionEnd(new BNE(label));
        compiler.addLabelEnd(fin);
        r1.setInUse(false);
        r2.setInUse(false);
    }

    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }


}
