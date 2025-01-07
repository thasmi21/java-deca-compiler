package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WINT;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class Plus extends AbstractOpArith {
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
 

    @Override
    protected String getOperatorName() {
        return "+";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, r1 );
            getRightOperand().evalueExpr(compiler, register );
            compiler.addInstruction(new ADD(r1, register));
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
            getLeftOperand().evalueExpr(compiler, r1 );
            getRightOperand().evalueExpr(compiler, r2 );
            compiler.addInstruction(new ADD(r1, r2));
            r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstruction(new WINT());
        } else if (this.getType() instanceof FloatType) {
            GPRegister r1 = GestionRegistre.obtainFreeRegister();
            GPRegister r2 = Register.R1;
            getLeftOperand().evalueExpr(compiler, r1 );
            getRightOperand().evalueExpr(compiler, r2 );
            compiler.addInstruction(new ADD(r1, r2));
            r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstruction(new WFLOAT());
        }

    }
}
