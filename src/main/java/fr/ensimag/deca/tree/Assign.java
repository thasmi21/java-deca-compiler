package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;

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
            throw new ContextualError("Les deux opérandes ne sont pas du même type : "+ typeLeft+" "+ typeRight, getLocation());
        }
    }

    /*@Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register) {
        // On commence par récupérer le registre dans lequel
        // stocker la valeur, qui est l'opérand gauche
        DAddr regAdresse = ((AbstractIdentifier)getLeftOperand()).getExpDefinition().getOperand();
        // On prend ensuite un registre libre
        GPRegister reg = GestionRegistre.obtainFreeRegister();
        // On y stock la valeur contenue dans l'opérand droite
        getRightOperand().evalueExpr(compiler, reg);
        // On stock la valeur mise dans le registre choisi a l'adresse de l'operand gauche
        compiler.addInstruction(new STORE(reg, regAdresse));
        reg.setInUse(false);
    }*/

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        //gerer le cas ou ce serait un entier egalement
        DAddr addresse = ((AbstractIdentifier)getLeftOperand()).getExpDefinition().getOperand();
        GPRegister r2 = Register.R1; // On prend le registre R1 car on va print ensuite
        getRightOperand().evalueExpr(compiler, r2 );
        compiler.addInstruction(new STORE(r2, addresse));
        //compiler.addInstruction(new WINT());
        getLeftOperand().codeGenPrint(compiler);
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        // On prend un registre libre
        //compiler.addInstruction(new STORE(Register.getR(3), getLeftOperand().getMemoryLocation()));
        // On commence par récupérer le registre dans lequel
        // stocker la valeur, qui est l'opérand gauche
        AbstractIdentifier ident;
        if(getLeftOperand() instanceof Dot){
            if(((Dot)getLeftOperand()).getDotField()){
                ident = ((Dot) getLeftOperand()).getIdentifier();
            }
            else{
                throw new IllegalArgumentException("Vous ne pouvez pas faire un assign avec cet element");
            }

        }else{
            ident = (AbstractIdentifier)getLeftOperand();
        }
        DAddr regAdresse = ident.getExpDefinition().getOperand();
        // On prend ensuite un registre libre
        GPRegister reg = GestionRegistre.obtainFreeRegister();
        if(ident.getDefinition().isField()){

            GPRegister r = GestionRegistre.obtainFreeRegister();
            boolean rWasNul = false;
            if(r == null){
                compiler.addInstruction(new PUSH(Register.getR(5)));
                r=Register.getR(5);
                rWasNul = true;
            }

            DAddr addrParent = ((AbstractIdentifier)((Dot)getLeftOperand()).getSelectExpr()).getExpDefinition().getOperand();
            compiler.addInstruction(new LOAD(addrParent, r));
            GestionRegistre.verifieNullReference(compiler, r);
            getRightOperand().evalueExpr(compiler, reg);
            compiler.addInstruction(new STORE(reg, new RegisterOffset((ident.getFieldDefinition()).getIndex(),r)));
            if(rWasNul){
                compiler.addInstruction(new POP(Register.getR(5)));
            }
            return;
        }
        if(reg != null){
            // On y stock la valeur contenue dans l'opérand droite
            getRightOperand().evalueExpr(compiler, reg);
            // On stock la valeur mise dans le registre choisi a l'adresse de l'operand gauche
            compiler.addInstruction(new STORE(reg, regAdresse));
            reg.setInUse(false);
        }
        else {
            reg = Register.getR(2);
            compiler.addInstruction(new PUSH(reg));
            getRightOperand().evalueExprMethod(compiler, reg);
            compiler.addInstruction(new STORE(reg, regAdresse));
            compiler.addInstruction(new POP(reg));
        }

    }

    @Override
    public void codeGenInstMethod(DecacCompiler compiler) {
        //AbstractLValue left = getLeftOperand();
        AbstractIdentifier ident;
        if(getLeftOperand() instanceof Dot){
            if(((Dot)getLeftOperand()).getDotField()){
                ident = ((Dot) getLeftOperand()).getIdentifier();
            }
            else{
                throw new IllegalArgumentException("Vous ne pouvez pas faire un assign avec cet element");
            }

        }else{
            ident = (AbstractIdentifier)getLeftOperand();
        }
        GPRegister reg = GestionRegistre.obtainFreeRegister();
        DAddr regAdresse = ident.getExpDefinition().getOperand();
        if(ident.getDefinition().isField()){

            GPRegister r = GestionRegistre.obtainFreeRegister();
            boolean rWasNul = false;
            if(r == null){
                compiler.addInstructionEnd(new PUSH(Register.getR(5)));
                r=Register.getR(5);
                rWasNul = true;
            }
            getRightOperand().evalueExprMethod(compiler, reg);
            DAddr addrParent = new RegisterOffset(-2, Register.LB);
            compiler.addInstructionEnd(new LOAD(addrParent, r));
            compiler.addInstructionEnd(new STORE(reg, new RegisterOffset((ident.getFieldDefinition()).getIndex(),r)));
            if(rWasNul){
                compiler.addInstructionEnd(new POP(Register.getR(5)));
            }
            return;
        }

        if(reg != null){
            getRightOperand().evalueExprMethod(compiler, reg);
            compiler.addInstructionEnd(new STORE(reg, regAdresse));
            reg.setInUse(false);
        }
        else{
            reg = Register.getR(2);
            compiler.addInstructionEnd(new PUSH(reg));
            getRightOperand().evalueExprMethod(compiler, reg);
            compiler.addInstructionEnd(new STORE(reg, regAdresse));
            compiler.addInstructionEnd(new POP(reg));
        }


    }




}
