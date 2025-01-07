package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, r1 );
            getRightOperand().evalueExpr(compiler, register );
            if (getLeftOperand().getType() instanceof IntType && getRightOperand().getType() instanceof IntType) {
                compiler.addInstruction(new QUO(r1, register));
            } else if (getLeftOperand().getType() instanceof FloatType && getRightOperand().getType() instanceof FloatType) {
                compiler.addInstruction(new DIV(r1, register));
            }
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        if (this.getType() instanceof IntType) {
            GPRegister r1 = GestionRegistre.obtainFreeRegister();
            GPRegister r2 = Register.R1;
            getLeftOperand().evalueExpr(compiler, r2 );
            getRightOperand().evalueExpr(compiler, r1 );
            compiler.addInstruction(new QUO(r1, r2));
            r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstruction(new WINT());
        }else if (this.getType() instanceof FloatType) {
            // Il faudra gérer l'option qui dit qu'on peut afficher un float en hexadecimal
            GPRegister r1 = GestionRegistre.obtainFreeRegister();
            GPRegister r2 = Register.R1;
            getLeftOperand().evalueExpr(compiler, r2 );
            getRightOperand().evalueExpr(compiler, r1 );
            compiler.addInstruction(new DIV(r1, r2));
            r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstruction(new WFLOAT());
        }
    }
}
