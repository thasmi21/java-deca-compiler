package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.ima.pseudocode.instructions.SUB;
import fr.ensimag.ima.pseudocode.instructions.WINT;

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
            compiler.labelDivZero(compiler);
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
        compiler.addInstruction(new REM(Register.R0, register));
        GestionRegistre.IncCompteurTempUsage();
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
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
        compiler.labelDivZero(compiler);
        compiler.addInstruction(new WINT());
    }

}
