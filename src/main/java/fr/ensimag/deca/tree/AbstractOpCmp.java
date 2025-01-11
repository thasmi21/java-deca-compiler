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
        if(!(typeLeft.sameType(typeRight)) && !(typeLeft.isFloat() || typeLeft.isInt()) && !(typeRight.isFloat() || typeRight.isInt())) {
            throw new ContextualError("Les deux opérandes ne sont pas du même type : "+ typeLeft+", Op droit : "+ typeRight, getLocation());
        }
        if(typeLeft.isFloat() || typeRight.isFloat()) {
            this.setType(compiler.environmentType.BOOLEAN);
            //Pour ajouter l'enrichissement Convfloat
            if(!typeLeft.isFloat()){
                setLeftOperand(new ConvFloat(getLeftOperand()));
                getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
            }
            if(!typeRight.isFloat()){
                setRightOperand(new ConvFloat(getRightOperand()));
                getRightOperand().verifyExpr(compiler, localEnv, currentClass);
            }
        }
        else {
            this.setType(compiler.environmentType.BOOLEAN);
        }
        return compiler.environmentType.BOOLEAN;
    }
}
