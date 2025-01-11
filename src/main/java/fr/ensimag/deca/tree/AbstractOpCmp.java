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
        Type type;
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if(!(typeLeft.sameType(typeRight) || (typeLeft.isFloat() && typeRight.isInt())  || (typeRight.isFloat() && typeLeft.isInt()))) {
            throw new ContextualError("Les deux opérandes ne sont pas comparables : Op gauche: "+ typeLeft+", Op droit : "+ typeRight, getLocation());
        }

        if( this instanceof AbstractOpIneq &&( getLeftOperand().getType().isBoolean() || getRightOperand().getType().isBoolean())){
            throw new ContextualError("Impossible de faire ce type de comparaison avec des booleans ", getLocation());
        }

        if(!typeLeft.isFloat() && typeRight.isFloat()){
            setLeftOperand(new ConvFloat(getLeftOperand()));
            getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        }
        if(!typeRight.isFloat() && typeLeft.isFloat()){
            setRightOperand(new ConvFloat(getRightOperand()));
            getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        }


        this.setType(compiler.environmentType.BOOLEAN);

        return compiler.environmentType.BOOLEAN;
    }
}
