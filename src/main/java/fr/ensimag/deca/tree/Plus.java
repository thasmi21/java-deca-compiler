package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.*;
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
    protected void codeGenInst(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (r1 != null) {
            getLeftOperand().evalueExpr(compiler, r1);
            getRightOperand().evalueExpr(compiler, register);
            compiler.addInstruction(new ADD(r1, register));
        } else {
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            compiler.addInstruction(new ADD(Register.R0, register));
            GestionRegistre.IncCompteurTempUsage();
        }

        if (r1 != null) {
            r1.setInUse(false);
        }
    }



    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (this.getLeftOperand().getType() instanceof ArrayType && this.getRightOperand().getType() instanceof ArrayType) {
            ArrayDefinition arrayDefDroit = ((ArrayDefinition) ((Identifier)getLeftOperand()).getExpDefinition());
            ArrayDefinition arrayDefGauche = ((ArrayDefinition) ((Identifier)getRightOperand()).getExpDefinition());
            Array tableauDroit = ((ArrayLiteral)(arrayDefDroit.getInitialization().getExpression())).getArray();
            Array tableauGauche = ((ArrayLiteral)(arrayDefGauche.getInitialization().getExpression())).getArray();
            int tailleTableauDroit = tableauDroit.getSize();
            // Boucle pour additionner chaque élément des tableaux
            compiler.addInstruction(new WSTR("["));
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
                if(i!= tailleTableauDroit-1){
                    compiler.addInstruction(new WSTR(","));
                }
            }
            compiler.addInstruction(new WSTR("]"));
            if (r1 != null) {
                r1.setInUse(false);
            }// il faut verifier ici si R1 a été obtenu et le rendre si oui
        }
        else {
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

    @Override
    public void doOpArray(DecacCompiler compiler){
        Identifier identGauche = (Identifier) getLeftOperand();
        Identifier identDroit = (Identifier) getRightOperand();
        ArrayDefinition arrayDefGauche = (ArrayDefinition) identGauche.getExpDefinition();
        ArrayDefinition arrayDefDroit = (ArrayDefinition) identDroit.getExpDefinition();
        Array tableauGauche = ((ArrayLiteral)arrayDefGauche.getInitialization().getExpression()).getArray();
        Array tableauDroit = ((ArrayLiteral)arrayDefDroit.getInitialization().getExpression()).getArray();

        int size = ((ArrayLiteral)arrayDefGauche.getInitialization().getExpression()).getArray().getSize();
        int indiceGP = GestionRegistre.obtainIndiceGPandIncrement(size-1);
        for(int i=0 ; i < size ; i++) {
            GPRegister r = GestionRegistre.obtainFreeRegister();
            evalueExpr(compiler, r);
            int coeffDroit = ((IntLiteral)tableauDroit.getCoefficients(i)).getValue();
            int coeffGauche = ((IntLiteral)tableauGauche.getCoefficients(i)).getValue();
            compiler.addInstruction(new LOAD(new ImmediateInteger(coeffGauche+coeffDroit), r));
            compiler.addInstruction(new STORE(r, new RegisterOffset(indiceGP-size+i+1, Register.GB)));
            r.setInUse(false);
        }
    }
}
