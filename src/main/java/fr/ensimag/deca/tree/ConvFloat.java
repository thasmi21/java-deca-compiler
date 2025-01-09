package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.OPP;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WINT;

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
            // On appelle ensuite l'instruction assembleur qui permet de convertir un int en float
//            if (this.getType() instanceof FloatType) {
//                // Si on a déjà un float, pas besoin d'utiliser une instruction assembleur inutilement
//                // donc on ne fait rien
//                r1.setInUse(false); //on n'utilise plus r1
//                return;
//            } else if (this.getType() instanceof IntType) {
//                compiler.addInstruction(new FLOAT(r1,register));
//                r1.setInUse(false); //on n'utilise plus r1
//                return;
//            }
        }
        System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        // Si on a déjà un Float on va simplement l'afficher
        // Si on a un int on va d'abord le convertir puis l'afficher
        // On cherche un registre libre
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        // On evalue l'exression dans l'operand
        getOperand().evalueExpr(compiler,r1);
        // On ajoute l'instruction assembleur qui permet de convertir un entier en float
        compiler.addInstruction(new FLOAT(r1, Register.R1));
        r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
        compiler.addInstruction(new WFLOAT());
//        if (this.getType() instanceof IntType) {
//            // On cherche un registre libre
//            GPRegister r1 = GestionRegistre.obtainFreeRegister();
//            // On evalue l'exression dans l'operand
//            getOperand().evalueExpr(compiler,r1);
//            // On ajoute l'instruction assembleur qui permet de convertir un entier en float
//            compiler.addInstruction(new FLOAT(r1, Register.R1));
//            r1.setInUse(false); // il faut verifier ici si R1 est un registre ou une adresse
//            compiler.addInstruction(new WFLOAT());
//        }else if (this.getType() instanceof FloatType) {
//            // Ici on a déjà un float donc on a juste à l'afficher
//            // On recupere la valeur de notre float et on la met directement dans R1
//            getOperand().evalueExpr(compiler,Register.R1);
//            // Pas besoin de faire d'instruction assembleur
//            compiler.addInstruction(new WFLOAT());
//        }
    }

    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

}
