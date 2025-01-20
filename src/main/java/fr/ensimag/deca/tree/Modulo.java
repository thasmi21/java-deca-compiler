package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        if(typeRight.sameType(typeLeft) && typeLeft.isInt()) {
            setType(typeLeft);
            return typeLeft;
        }
        else{
            throw new ContextualError("Les deux opérandes ne sont pas du même type ou entiers : "+ typeLeft+ typeRight, getLocation());
        }
    }


    @Override
    protected String getOperatorName() {
        return "%";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, register);
            getRightOperand().evalueExpr(compiler, r1 );
            compiler.addInstruction(new REM(r1, register));
            if (!compiler.getN()){
                compiler.labelDivZero(compiler);
            }
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
        compiler.addInstruction(new REM(Register.R0, register));
        if (!compiler.getN()){
            compiler.labelDivZero(compiler);
        }
        GestionRegistre.IncCompteurTempUsage();
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExprEnd(compiler, register);
            getRightOperand().evalueExprEnd(compiler, r1 );
            compiler.addInstructionEnd(new REM(r1, register));
            compiler.labelDivZeroEnd(compiler);
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        GestionRegistre.pushETpopEnd(compiler, getRightOperand(), getLeftOperand(), register);
        compiler.addInstructionEnd(new REM(Register.R0, register));
        GestionRegistre.IncCompteurTempUsage();
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if(r1 != null){
            getLeftOperand().evalueExpr(compiler, register );
            getRightOperand().evalueExpr(compiler, r1 );
            compiler.addInstruction(new REM(r1, register));
            r1.setInUse(false);
        }else{
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            compiler.addInstruction(new MUL(Register.R0, register));
            GestionRegistre.IncCompteurTempUsage();
        }
        if (!compiler.getN()){
            compiler.labelDivZero(compiler);
        }
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if(r1 != null){
            getLeftOperand().evalueExpr(compiler, register );
            getRightOperand().evalueExpr(compiler, r1 );
            compiler.addInstruction(new REM(r1, register));
            r1.setInUse(false);
        }else{
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            compiler.addInstruction(new MUL(Register.R0, register));
            GestionRegistre.IncCompteurTempUsage();
        }
        if (!compiler.getN()){
            compiler.labelDivZero(compiler);
        }
        compiler.addInstruction(new WINT());
    }
    @Override
    public void codeGenPrintMethod(DecacCompiler compiler){
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if(r1 != null){
            getLeftOperand().evalueExprEnd(compiler, register );
            getRightOperand().evalueExprEnd(compiler, r1 );
            compiler.addInstructionEnd(new REM(r1, register));
            r1.setInUse(false);
        }else{
            GestionRegistre.pushETpopEnd(compiler, getRightOperand(), getLeftOperand(), register);
            compiler.addInstructionEnd(new MUL(Register.R0, register));
            GestionRegistre.IncCompteurTempUsage();
        }
        compiler.labelDivZeroEnd(compiler);
        compiler.addInstructionEnd(new WINT());
    }

    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        //ne fait rien
        //compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
        GPRegister register = Register.R1;
        GPRegister r1 = Register.R0;
        GestionRegistre.pushETpopEnd(compiler, getRightOperand(), getLeftOperand(), register);
        GestionRegistre.IncCompteurTempUsage();
        if (this.getType() instanceof IntType) {

            compiler.addInstructionEnd(new REM(r1, register));
            if (!compiler.getN()){
                compiler.labelDivZeroEnd(compiler);
            }
            //compiler.addInstructionEnd(new WINT());
        } else if (this.getType() instanceof FloatType) {
            compiler.addInstructionEnd(new REM(r1, register));
            if (!compiler.getN()){
                compiler.labelDivZeroEnd(compiler);
            }
            //compiler.addInstructionEnd(new WFLOAT());
        }
    }

}
