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
        /*
        *  a revoir ici car on peut bien afficher !(a<=2) et pourtant "a<=2" n'est pas un boolean*/
        if (!operandType.isBoolean()) {
            throw new ContextualError("L'expression doit être un boolean pour le Not", getLocation());
        }
        setType(operandType);
        return operandType;
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register ) {
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        boolean r1EtaitNul = false;
        if(r1 == null){
            r1EtaitNul = true;
            r1 = register;
        }
        getOperand().evalueExpr(compiler, r1);
        Label E = new Label(this.toString().replace('@', '_'));
        Label fin = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new CMP(0, r1));
        compiler.addInstruction(new BEQ(E));
        compiler.addInstruction(new LOAD(0, register));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addInstruction(new LOAD(1, register));
        compiler.addLabel(fin);
        if(!r1EtaitNul) {
            r1.setInUse(false);
        }


    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r =  GestionRegistre.obtainFreeRegister();


        getOperand().evalueExpr(compiler, r);


        Label E = new Label(thenIns.toString().replace('@', '_')+"if");
        Label fin = new Label(thenIns.toString().replace('@', '_')+"fin_if");
        compiler.addInstruction(new CMP(0, r));
        compiler.addInstruction(new BEQ(E));
        elseIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        thenIns.codeGenListInst(compiler);
        compiler.addLabel(fin);
        r.setInUse(false);
    }

    @Override
    public void genWhileCode(DecacCompiler compiler, Label label){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        getOperand().evalueExpr(compiler, r);

        compiler.addInstruction(new CMP(0, r)); //si vrai
        compiler.addInstruction(new BEQ(label));
        r.setInUse(false);
    }

    @Override
    protected String getOperatorName() {
        return "!";
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        GPRegister r1 = GestionRegistre.obtainFreeRegister();

        getOperand().evalueExpr(compiler, r1);

        Label E = new Label(this.toString().replace('@', '_')+"pt");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_pt");
        compiler.addInstruction(new CMP(0, r1));
        compiler.addInstruction(new BNE(E));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addLabel(fin);
        r1.setInUse(false);
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();

        getOperand().evalueExpr(compiler, r1);

        Label E = new Label(this.toString().replace('@', '_')+"pt");
        Label fin = new Label(this.toString().replace('@', '_')+"fin_pt");
        compiler.addInstruction(new CMP(0, r1));
        compiler.addInstruction(new BNE(E));
        compiler.addInstruction(new WSTR("true"));
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        compiler.addInstruction(new WSTR("false"));
        compiler.addLabel(fin);
        r1.setInUse(false);
    }
}
