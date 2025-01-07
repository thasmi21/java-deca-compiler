package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        if(!(typeLeft.sameType(typeRight))) {
            throw new ContextualError("Les deux opérandes ne sont pas du même type : "+ typeLeft+", Op droit : "+ typeRight, getLocation());
        }
        Type typeRes;
        if(typeLeft.isFloat() || typeRight.isFloat()) {
            typeRes = compiler.environmentType.FLOAT;
            }
        else {
            typeRes = compiler.environmentType.INT;
        }
        setType(typeRes);
        return typeRes;
    }
}
