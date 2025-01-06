package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.WINT;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class Plus extends AbstractOpArith {
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
 

    @Override
    protected String getOperatorName() {
        return "+";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = Register.getR(4);
        GPRegister r2 = Register.getR(5);
        getLeftOperand().evalueExpr(compiler, r1 );
        getRightOperand().evalueExpr(compiler, register );
        compiler.addInstruction(new ADD(r1, register));
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister r1 = Register.getR(4);
        GPRegister r2 = Register.getR(1);
        getLeftOperand().evalueExpr(compiler, r1 );
        getRightOperand().evalueExpr(compiler, r2 );
        compiler.addInstruction(new ADD(r1, r2));
        compiler.addInstruction(new WINT());
    }
}
