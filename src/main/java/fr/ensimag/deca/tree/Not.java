package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.*;

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
            ClassDefinition currentClass) throws ContextualError {
        AbstractExpr operand = getOperand();
        Type operandType = operand.verifyExpr(compiler, localEnv, currentClass);
        if (!operandType.isBoolean()) {
            throw new ContextualError("L'expression doit être un boolean!", getLocation());
        }
        setType(operandType);
        return operandType;
    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r =  GestionRegistre.obtainFreeRegister();

        if( !(getOperand() instanceof Identifier)){
            int entier = this.getOperand().getEvalBooleanExpr();
            compiler.addInstruction(new LOAD(entier, r));
        }
        else{
            DAddr op = ((Identifier)getOperand()).getExpDefinition().getOperand();
            compiler.addInstruction(new LOAD(op, r));
        }


        Label E = new Label(thenIns.toString().replace('@', '_'));
        Label fin = new Label(thenIns.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new CMP(0, r));
        compiler.addInstruction(new BEQ(E));
        elseIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        thenIns.codeGenListInst(compiler);
        compiler.addLabel(fin);
    }

    @Override
    protected String getOperatorName() {
        return "!";
    }
}
