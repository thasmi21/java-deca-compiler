package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;

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
        //si on doit conserver dans la pile
        GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
        compiler.addInstruction(new ADD(Register.R0, register));
        GestionRegistre.IncCompteurTempUsage();
        //System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (this.getLeftOperand() instanceof ArrayLiteral && this.getRightOperand() instanceof ArrayLiteral) {
            Array tableauDroit = ((ArrayLiteral) this.getRightOperand()).getArray();
            Array tableauGauche = ((ArrayLiteral) this.getLeftOperand()).getArray();
            int tailleTableauDroit = tableauDroit.getSize();
            int tailleTableauGauche = tableauGauche.getSize();
            if (tailleTableauDroit != tailleTableauGauche) {
                throw new RuntimeException("Les tableaux ne sont pas de la même taille !");
            }
            // Boucle pour additionner chaque élément des tableaux
            for (int i = 0; i < tailleTableauDroit; i++) {
                if (r1 != null) {
                    tableauDroit.getCoefficients(i).evalueExpr(compiler, r1);
                    tableauGauche.getCoefficients(i).evalueExpr(compiler, register);
                    compiler.addInstruction(new ADD(r1, register));
                } else {
                    GestionRegistre.pushETpop(compiler, tableauDroit.getCoefficients(i), tableauGauche.getCoefficients(i), register);
                    compiler.addInstruction(new ADD(Register.R0, register));
                    GestionRegistre.IncCompteurTempUsage();
                }
                if (tableauDroit.getCoefficients(0).getType() instanceof IntType) {
                    compiler.addInstruction(new WINT());
                } else if (tableauDroit.getCoefficients(0).getType() instanceof FloatType) {
                    compiler.addInstruction(new WFLOAT());
                }
            }
            if (r1 != null) {
                r1.setInUse(false);
            }// il faut verifier ici si R1 a été obtenu et le rendre si oui
        }else {
            if (r1 != null) {
                getLeftOperand().evalueExpr(compiler, r1);
                getRightOperand().evalueExpr(compiler, register);
                compiler.addInstruction(new ADD(r1, register));
            } else {
                GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
                compiler.addInstruction(new ADD(Register.R0, register));
                GestionRegistre.IncCompteurTempUsage();
            }

            if (this.getType() instanceof IntType) {
                compiler.addInstruction(new WINT());
            } else if (this.getType() instanceof FloatType) {
                compiler.addInstruction(new WFLOAT());
            }

            if (r1 != null) {
                r1.setInUse(false);
            }// il faut verifier ici si R1 a été obtenu et le rendre si oui
        }
    }
}
