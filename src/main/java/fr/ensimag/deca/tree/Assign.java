package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError, EnvironmentExp.DoubleDefException {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        if (typeRight.sameType(typeLeft) || ((typeLeft.isFloat() && typeRight.isInt() || typeLeft.isInt()) && (typeRight.isFloat()))) {
            if (typeRight.sameType(typeLeft)) {
                this.setType(typeLeft);
                return typeLeft;
            }
            if (typeLeft.isFloat()) {
                this.setType(typeLeft);
                return typeLeft;
            }
            this.setType(typeRight);
            return typeRight;
        } else {
            throw new ContextualError("Les deux opérandes ne sont pas du même type : " + typeLeft + typeRight, getLocation());
        }
    }

        @Override
        public void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                ClassDefinition currentClass, Type returnType) throws ContextualError, EnvironmentExp.DoubleDefException {
            // Vérifie le type de l'affectation
            Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
            Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

            // Vérifie la compatibilité des types
            if (typeRight.sameType(typeLeft) || (typeLeft.isFloat() && typeRight.isInt())) {
                if (typeLeft.isFloat() && typeRight.isInt()) {
                    // Promotion implicite de int vers float
                    setRightOperand(new ConvFloat(getRightOperand()));
                }
                this.setType(typeLeft);
            } else {
                throw new ContextualError(
                        "Erreur de type : impossible d'affecter une expression de type " +
                                typeRight + " à une variable de type " + typeLeft,
                        getLocation()
                );
            }
        }


    @Override
    protected String getOperatorName() {
        return "=";
    }

}
