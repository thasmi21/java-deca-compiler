package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl06
 * @date 01/01/2025
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        this.setType(compiler.environmentType.FLOAT);
        return compiler.environmentType.FLOAT;
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            // On commence par évaluer l'expression que l'on veut convertir en float
            getOperand().evalueExpr(compiler,r1);
            compiler.addInstruction(new FLOAT(r1,register));
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        getOperand().evalueExpr(compiler, register );
        compiler.addInstruction(new LOAD(register, Register.R0));
        compiler.addInstruction(new FLOAT(Register.R0,register));
        //System.out.println("Pas de registre dispo conv"); // à retirer, il faudra prendre un espace pile alors
    }


    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            // On commence par évaluer l'expression que l'on veut convertir en float
            getOperand().evalueExprEnd(compiler,r1);
            compiler.addInstructionEnd(new FLOAT(r1,register));
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        getOperand().evalueExprEnd(compiler, register );
        compiler.addInstructionEnd(new LOAD(register, Register.R0));
        compiler.addInstructionEnd(new FLOAT(Register.R0,register));
        //System.out.println("Pas de registre dispo conv"); // à retirer, il faudra prendre un espace pile alors
    }


    @Override
    public void codeGenPrint(DecacCompiler compiler){
        // Si on a déjà un Float on va simplement l'afficher
        // Si on a un int on va d'abord le convertir puis l'afficher
        // On cherche un registre libre
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            // On evalue l'exression dans l'operand
            getOperand().evalueExpr(compiler,r1);
            // On ajoute l'instruction assembleur qui permet de convertir un entier en float
            compiler.addInstruction(new FLOAT(r1, Register.R1));
            r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstruction(new WFLOAT());
        }
        //Normalement on ne devrait jamais se retrouver dans un cas ou on affiche ceci
        //sauf si on fait un cast;

    }
    @Override
    public void codeGenPrintMethod(DecacCompiler compiler){
        // Si on a déjà un Float on va simplement l'afficher
        // Si on a un int on va d'abord le convertir puis l'afficher
        // On cherche un registre libre
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            // On evalue l'exression dans l'operand
            getOperand().evalueExprEnd(compiler,r1);
            // On ajoute l'instruction assembleur qui permet de convertir un entier en float
            compiler.addInstructionEnd(new FLOAT(r1, Register.R1));
            r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
            compiler.addInstructionEnd(new WFLOAT());
        }
        //Normalement on ne devrait jamais se retrouver dans un cas ou on affiche ceci
        //sauf si on fait un cast;

    }
    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }

    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

}
