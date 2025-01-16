package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "*";
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register){
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        //GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (r1 != null){
            getLeftOperand().evalueExpr(compiler, r1 );
            getRightOperand().evalueExpr(compiler, register );
            compiler.addInstruction(new MUL(r1, register));
            r1.setInUse(false); //on n'utilise plus r1
            return;
        }
        GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
        compiler.addInstruction(new MUL(Register.R0, register));
        //System.out.println("Pas de registre dispo"); // à retirer, il faudra prendre un espace pile alors
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        if (this.getLeftOperand().getType().isArray() && this.getRightOperand().getType().isInt() || this.getLeftOperand().getType().isInt() && this.getRightOperand().getType().isArray()) {
            int tailleTableau;
            ArrayDefinition arrayDef;
            Array tableau;
            if (this.getLeftOperand().getType().isArray()) {
                arrayDef = ((ArrayDefinition) ((Identifier) getLeftOperand()).getExpDefinition());
                tableau = ((ArrayLiteral) (arrayDef.getInitialization().getExpression())).getArray();
                tailleTableau = tableau.getSize();
                getRightOperand().evalueExpr(compiler, register);
            } else {
                arrayDef = ((ArrayDefinition) ((Identifier) getRightOperand()).getExpDefinition());
                tableau = ((ArrayLiteral) (arrayDef.getInitialization().getExpression())).getArray();
                tailleTableau = tableau.getSize();
                getLeftOperand().evalueExpr(compiler, register);
            }
            compiler.addInstruction(new WSTR("["));
            for (int i = 0; i < tailleTableau; i++) {
                if (r1 != null) {
                    tableau.getCoefficients(i).evalueExpr(compiler, r1);
                    compiler.addInstruction(new MUL(r1, register));
                } else {
//                    GestionRegistre.pushETpop(compiler, tableau.getCoefficients(i), getRightOperand(), register);
//                    compiler.addInstruction(new MUL(Register.R0, register));
//                    GestionRegistre.IncCompteurTempUsage();
                }
                if (tableau.getCoefficients(0).getType() instanceof IntType) {
                    compiler.addInstruction(new WINT());
                } else if (tableau.getCoefficients(0).getType() instanceof FloatType) {
                    compiler.addInstruction(new WFLOAT());
                }
                if (i != tailleTableau - 1) {
                    compiler.addInstruction(new WSTR(","));
                }
            }
            compiler.addInstruction(new WSTR("]"));
        } else {
            if (r1 != null) {
                getLeftOperand().evalueExpr(compiler, register);
                getRightOperand().evalueExpr(compiler, r1);
                compiler.addInstruction(new MUL(r1, register));
            } else {
                GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
                compiler.addInstruction(new MUL(Register.R0, register));
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
