package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        AbstractExpr operand = getOperand();
        Type operandType = operand.verifyExpr(compiler, localEnv, currentClass);
        // Cette condition if pose problème pour prendre l'opposé d'une variable
        // J'ai changé le OU en ET
        if (!operandType.isInt() && !operandType.isFloat()) {
            throw new ContextualError("L'expression doit être un nombre (entier ou décimal)!", getLocation());
        }
        setType(operandType);
        return operandType;
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getOperand().evalueExpr(compiler,r1);
            compiler.addInstruction(new OPP(r1, register));
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        getOperand().evalueExpr(compiler,register);
        compiler.addInstruction(new OPP(register, register));
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }
    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getOperand().evalueExprEnd(compiler,r1);
            compiler.addInstructionEnd(new OPP(r1, register));
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        getOperand().evalueExprEnd(compiler,register);
        compiler.addInstructionEnd(new OPP(register, register));
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister r1 = Register.getR(2);
        compiler.addInstruction(new PUSH(r1));
        if (this.getType() instanceof IntType) {
            getOperand().evalueExpr(compiler,r1);
            compiler.addInstruction(new OPP(r1, Register.R1));
            //r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstruction(new WINT());
        }else if (this.getType() instanceof FloatType) {
            getOperand().evalueExpr(compiler,r1);
            compiler.addInstruction(new OPP(r1, Register.R1));
            //r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstruction(new WFLOAT());
        }
        compiler.addInstruction(new POP(r1));
    }
    @Override
    public void codeGenPrintMethod(DecacCompiler compiler){
        GPRegister r1 = Register.getR(2);
        compiler.addInstructionEnd(new PUSH(r1));
        if (this.getType() instanceof IntType) {
            getOperand().evalueExprEnd(compiler,r1);
            compiler.addInstructionEnd(new OPP(r1, Register.R1));
            //r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstructionEnd(new WINT());
        }else if (this.getType() instanceof FloatType) {
            getOperand().evalueExprEnd(compiler,r1);
            compiler.addInstructionEnd(new OPP(r1, Register.R1));
            //r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstructionEnd(new WFLOAT());
        }
        compiler.addInstructionEnd(new POP(r1));
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getOperand().evalueExpr(compiler,r1);
            compiler.addInstruction(new OPP(r1, register));
            r1.setInUse(false);
            return;
        }
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
        // Je pense qu'on va avoir un problème ici, car si on n'a plus de registre dispo on va appeler la méthode push et pop
        // mais celle-ci prend deux operand en paramètre et ici on n'en a qu'une
        // Peut-être qu'il faut re-créer la methode pushPop avec une seule operand en paramètre
    }

}
