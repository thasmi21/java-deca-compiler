package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
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
        if(typeRight.sameType(typeLeft)) {
            setType(typeLeft);
            return typeLeft;
        }
        else{
            throw new ContextualError("Les deux opérandes ne sont pas du même type : "+ typeLeft+ typeRight, getLocation());
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
            getLeftOperand().evalueExpr(compiler, r1 );
            getRightOperand().evalueExpr(compiler, register );
            compiler.addInstruction(new REM(r1, register));
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        GPRegister r2 = Register.R1;
        getLeftOperand().evalueExpr(compiler, r1 );
        getRightOperand().evalueExpr(compiler, r2 );
        compiler.addInstruction(new REM(r1, r2));
        r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
        compiler.addInstruction(new WINT());
    }

}
