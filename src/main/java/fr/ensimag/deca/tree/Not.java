package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError, EnvironmentExp.DoubleDefException {
        AbstractExpr operand = getOperand();
        Type operandType = operand.verifyExpr(compiler, localEnv, currentClass);
        if (!operandType.isBoolean()) {
            throw new ContextualError("L'expression doit être un boolean!", getLocation());
        }
        operand.setType(operandType);
        return operandType;
    }


    @Override
    protected String getOperatorName() {
        return "!";
    }
}
