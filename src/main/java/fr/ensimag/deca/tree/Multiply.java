package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "*";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, r1 );
            getRightOperand().evalueExpr(compiler, register );
            compiler.addInstruction(new MUL(r1, register));
            if (this.getType().isFloat()) {
                compiler.labelOverflow(compiler);
            }
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
        compiler.addInstruction(new MUL(Register.R0, register));
        if (this.getType().isFloat()) {
            if (!compiler.getN()){
                compiler.labelOverflow(compiler);
            }
        }
        //System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExprEnd(compiler, r1 );
            getRightOperand().evalueExprEnd(compiler, register );
            compiler.addInstructionEnd(new MUL(r1, register));
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        GestionRegistre.pushETpopEnd(compiler, getRightOperand(), getLeftOperand(), register);
        compiler.addInstructionEnd(new MUL(Register.R0, register));
        //System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, register );
            getRightOperand().evalueExpr(compiler, r1 );
            compiler.addInstruction(new MUL(r1, register));
        }else{
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            compiler.addInstruction(new MUL(Register.R0, register));
        }

        if(r1 != null){
            r1.setInUse(false);
        }// il faut verifier ici si R1 a été obtenu et le rendre si oui
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, register );
            getRightOperand().evalueExpr(compiler, r1 );
            compiler.addInstruction(new MUL(r1, register));
        }else{
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            compiler.addInstruction(new MUL(Register.R0, register));
        }

        if (this.getType() instanceof IntType) {
            compiler.addInstruction(new WINT());
        } else if (this.getType() instanceof FloatType) {
            compiler.addInstruction(new WFLOAT());
        }

        if(r1 != null){
            r1.setInUse(false);
        }// il faut verifier ici si R1 a été obtenu et le rendre si oui

    }
    @Override
    public void codeGenPrintMethod(DecacCompiler compiler){
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExprEnd(compiler, register );
            getRightOperand().evalueExprEnd(compiler, r1 );
            compiler.addInstructionEnd(new MUL(r1, register));
        }else{
            GestionRegistre.pushETpopEnd(compiler, getRightOperand(), getLeftOperand(), register);
            compiler.addInstructionEnd(new MUL(Register.R0, register));
        }

        if (this.getType() instanceof IntType) {
            compiler.addInstructionEnd(new WINT());
        } else if (this.getType() instanceof FloatType) {
            compiler.addInstructionEnd(new WFLOAT());
        }

        if(r1 != null){
            r1.setInUse(false);
        }// il faut verifier ici si R1 a été obtenu et le rendre si oui

    }
    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }
}
