package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.STORE;

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
            ClassDefinition currentClass) throws ContextualError {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        if(typeRight.sameType(typeLeft) || ((typeLeft.isFloat() && typeRight.isInt()|| typeLeft.isInt()) && (typeRight.isFloat()))) {
            if(typeRight.sameType(typeLeft)){
                this.setType(typeLeft);
                return typeLeft;
            }
            if(typeLeft.isFloat()){
                this.setType(typeLeft);
                return typeLeft;
            }
            this.setType(typeRight);
            return typeRight;
        }
        else{
            throw new ContextualError("Les deux opérandes ne sont pas du même type : "+ typeLeft+ typeRight, getLocation());
        }

    }


    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {

        // On prend un registre libre
        //compiler.addInstruction(new STORE(Register.getR(3), getLeftOperand().getMemoryLocation()));
    }




}
