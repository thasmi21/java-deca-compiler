package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl06
 * @date 01/01/2025
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type;
        if (!(typeLeft.isFloat() || typeLeft.isInt()) && !(typeRight.isFloat() || typeRight.isInt())) {
            throw new ContextualError("Les deux opérandes ne sont pas du même type (ou l'operation n'est pas prise en charge) !" + typeLeft + typeRight, getLocation());
        }
        if (typeLeft.isFloat() || typeRight.isFloat()) {
            this.setType(compiler.environmentType.FLOAT);
            if (typeLeft.isFloat()) {
                type = typeLeft;
            } else {
                type = typeRight;
            }
            //Pour ajouter l'enrichissement Convfloat
            if (!typeLeft.isFloat()) {
                setLeftOperand(new ConvFloat(getLeftOperand()));
                getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
            }
            if (!typeRight.isFloat()) {
                setRightOperand(new ConvFloat(getRightOperand()));
                getRightOperand().verifyExpr(compiler, localEnv, currentClass);
            }
            return type;
        }
        else if (typeLeft.isInt() && typeRight.isInt()) {
            this.setType(compiler.environmentType.INT);
            return typeLeft;
        }
        else {
            throw new ContextualError("Probleme lors de la vérification contextuelle !", getLocation());
        }
    }
}
