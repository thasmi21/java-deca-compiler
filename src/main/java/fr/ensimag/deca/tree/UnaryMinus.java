package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.OPP;
import fr.ensimag.ima.pseudocode.instructions.SUB;
import fr.ensimag.ima.pseudocode.instructions.WINT;

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
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        getOperand().evalueExpr(compiler,r1);
        compiler.addInstruction(new OPP(r1, Register.R1));
        r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
        compiler.addInstruction(new WINT());
    }

}
