package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, register );
            getRightOperand().evalueExpr(compiler, r1 );
        }
        else{
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            GestionRegistre.IncCompteurTempUsage();
            r1 = Register.R0;
        }

        //on verifie juste l'operande gauche car verifiyExpr aura verifie que les operandes sont de même type
        if (getLeftOperand().getType() instanceof IntType ) {
            compiler.addInstruction(new QUO(r1, register));
            if (!compiler.getN()) {
                compiler.labelDivZero(compiler);
            }
        } else if (getLeftOperand().getType() instanceof FloatType) {
            compiler.addInstruction(new DIV(r1, register));
            if (!compiler.getN()) {
                compiler.labelDivZero(compiler);
                compiler.labelOverflow(compiler);
            }
        }
        if(r1 != null){
            r1.setInUse(false);
        }
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, register );
            getRightOperand().evalueExpr(compiler, r1 );

        }else{
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            GestionRegistre.IncCompteurTempUsage();
            r1=Register.R0;

        }

        if (getLeftOperand().getType().isInt()){
            compiler.addInstruction(new QUO(r1, register));
            if (!compiler.getN()) {
                compiler.labelDivZero(compiler);
            }
        } else if (getRightOperand().getType().isFloat()) {
            compiler.addInstruction(new DIV(r1, register));
            if (!compiler.getN()) {
                compiler.labelDivZero(compiler);
                compiler.labelOverflow(compiler);
            }
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

        }else{
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            GestionRegistre.IncCompteurTempUsage();
            r1=Register.R0;

        }

        if (this.getType() instanceof IntType) {

            compiler.addInstruction(new QUO(r1, register));
            if (!compiler.getN()){
                compiler.labelDivZero(compiler);
            }
            compiler.addInstruction(new WINT());
        } else if (this.getType() instanceof FloatType) {
            compiler.addInstruction(new DIV(r1, register));
            if (!compiler.getN()){
                compiler.labelDivZero(compiler);
                compiler.labelOverflow(compiler);
            }
            compiler.addInstruction(new WFLOAT());
        }

        if(r1 != null){
            r1.setInUse(false);
        }// il faut verifier ici si R1 a été obtenu et le rendre si oui
    }
}
