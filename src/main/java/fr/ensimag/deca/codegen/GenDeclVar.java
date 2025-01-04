package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tree.AbstractDeclVar;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.IntLiteral;
import fr.ensimag.deca.tree.ListDeclVar;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public final class GenDeclVar {
    private GenDeclVar(){
        throw new UnsupportedOperationException("Cette classe ne doit pas être instanciée!");

    }
    /*
    * methode chargé de generer le code pour les déclaration de
    * variables; elles va appeler les fonctions de la hierarchie nexessaire à cela
    * */
    public static void genDeclVar(ListDeclVar declVars, DecacCompiler compiler){
            //on ajoute d'abord l'instruction pour augmenter la pile
            // on fera le TSTO après
            compiler.addInstruction(new ADDSP(declVars.size()));
            for(AbstractDeclVar declVar: declVars.getList()){
                declVar.codeGenDeclVar(compiler);
            }
    }

    /*
    * A deplacer  peut-être dans une autre classe après
    * il prend une expression, l'evalue dans un registre et retourne
    * ce registre
    * il fera des allocations de pile si nexessaire*/

    public static Register evalueExpr(DecacCompiler compiler, AbstractExpr expr){
        //il faut recuperer un registre libre
        if(expr instanceof IntLiteral){
            compiler.addInstruction(new LOAD(((IntLiteral)expr).getValue(), Register.getR(3)));       }
        return Register.getR(3);
    }

}
