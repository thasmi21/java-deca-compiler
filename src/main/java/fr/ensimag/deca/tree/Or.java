package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){
        AbstractExpr transform = new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())));
        transform.genIfElseCode(compiler, thenIns, elseIns);
    }
    @Override
    public void genIfElseCodeMethod(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){
        AbstractExpr transform = new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())));
        transform.genIfElseCodeMethod(compiler, thenIns, elseIns);
    }
    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }
    @Override
    public void codeGenInst(DecacCompiler compiler) {
        AbstractExpr transform = new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())));
        transform.codeGenInst(compiler);
    }


    @Override
    public void codeGenPrint(DecacCompiler compiler){
        AbstractExpr transform = new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())));
        transform.codeGenPrint(compiler);
    }
    @Override
    public void genWhileCode(DecacCompiler compiler, Label label){
        AbstractExpr transform = new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())));
        transform.genWhileCode(compiler, label);
    }

    @Override
    public void genWhileCodeMethod(DecacCompiler compiler, Label label){
        AbstractExpr transform = new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())));
        transform.genWhileCodeMethod(compiler, label);
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        AbstractExpr transform = new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())));
        transform.evalueExpr(compiler, register);
    }

    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register){
        AbstractExpr transform = new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())));
        transform.evalueExprEnd(compiler, register);
    }
}
