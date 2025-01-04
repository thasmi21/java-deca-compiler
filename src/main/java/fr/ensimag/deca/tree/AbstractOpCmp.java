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
            ClassDefinition currentClass) throws ContextualError, EnvironmentExp.DoubleDefException {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        if(!(typeLeft.isInt() || typeLeft.isFloat()) || !(typeRight.isInt() || typeRight.isFloat()) ) {
            throw new ContextualError("L'une des deux opérandes n'est pas un nombre (int ou float)! Op gauche : "+ typeLeft+", Op droit : "+ typeRight, getLocation());
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
