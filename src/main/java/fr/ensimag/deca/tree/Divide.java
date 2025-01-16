package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
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
            getLeftOperand().evalueExpr(compiler, register );
            getRightOperand().evalueExpr(compiler, r1 );
        }
        else{
            GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
            GestionRegistre.IncCompteurTempUsage();
            r1 = Register.R0;
        }

        //on verifie juste l'operande gauche car verifiyExpr aura verifie que les operandes sont de même type
        if (getLeftOperand().getType() instanceof IntType ) {
            compiler.addInstruction(new QUO(r1, register));
        } else if (getLeftOperand().getType() instanceof FloatType) {
            compiler.addInstruction(new DIV(r1, register));
        }

        compiler.labelDivZero(compiler);
        if(r1 != null){
            r1.setInUse(false);
        }// il faut verifier ici si R1 a été obtenu et le rendre si oui
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        GPRegister register = Register.R1;
        GPRegister r2 = GestionRegistre.obtainFreeRegister();
        if (this.getLeftOperand().getType().isArray() && this.getRightOperand().getType().isInt() || this.getLeftOperand().getType().isInt() && this.getRightOperand().getType().isArray()) {
            int tailleTableau;
            ArrayDefinition arrayDef;
            Array tableau;
            int scalaire;
            if (this.getLeftOperand().getType().isArray()) {
                arrayDef = ((ArrayDefinition) ((Identifier) getLeftOperand()).getExpDefinition());
                tableau = ((ArrayLiteral) (arrayDef.getInitialization().getExpression())).getArray();
                tailleTableau = tableau.getSize();
                getRightOperand().evalueExpr(compiler, r2);
            } else {
                arrayDef = ((ArrayDefinition) ((Identifier) getRightOperand()).getExpDefinition());
                tableau = ((ArrayLiteral) (arrayDef.getInitialization().getExpression())).getArray();
                tailleTableau = tableau.getSize();
                getLeftOperand().evalueExpr(compiler, r2);
            }
            compiler.addInstruction(new WSTR("["));
            for (int i = 0; i < tailleTableau; i++) {
                GPRegister r1 = GestionRegistre.obtainFreeRegister();
                if (r1 != null) {
                    tableau.getCoefficients(i).evalueExpr(compiler, r1);
                } else {
//                    GestionRegistre.pushETpop(compiler, tableau.getCoefficients(i), getRightOperand(), register);
//                    GestionRegistre.IncCompteurTempUsage();
                }
//                if ( instanceof IntType) {
//                    compiler.addInstruction(new QUO(r1, register));
//                    compiler.labelDivZero(compiler);
//                    compiler.addInstruction(new WINT());
//                } else if (this.getType() instanceof FloatType) {
//                    compiler.addInstruction(new DIV(r1, register));
//                    compiler.labelDivZero(compiler);
//                    compiler.addInstruction(new WFLOAT());
//                }
                compiler.addInstruction(new QUO(r1, register));
                compiler.labelDivZero(compiler);
                compiler.addInstruction(new WINT());

                if (i != tailleTableau - 1) {
                    compiler.addInstruction(new WSTR(","));
                }
            }
            compiler.addInstruction(new WSTR("]"));
        } else {
            GPRegister r1 = GestionRegistre.obtainFreeRegister();
            if (r1 != null) {
                getLeftOperand().evalueExpr(compiler, register);
                getRightOperand().evalueExpr(compiler, r1);

            } else {
                GestionRegistre.pushETpop(compiler, getRightOperand(), getLeftOperand(), register);
                GestionRegistre.IncCompteurTempUsage();
                r1 = Register.R0;

            }

            if (this.getType() instanceof IntType) {

                compiler.addInstruction(new QUO(r1, register));
                compiler.labelDivZero(compiler);
                compiler.addInstruction(new WINT());
            } else if (this.getType() instanceof FloatType) {
                compiler.addInstruction(new DIV(r1, register));
                compiler.labelDivZero(compiler);
                compiler.addInstruction(new WFLOAT());
            }

            if (r1 != null) {
                r1.setInUse(false);
            }// il faut verifier ici si R1 a été obtenu et le rendre si oui
        }
    }
}
