package fr.ensimag.deca.codegen;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

/*
* Classe pour demander un régistre libre et le liberer après si besoin
* il aura egalement une fonction pour demander de l'espace pile
* avec un compteur à jour sur le prochain espace de pile libre
* */
public final class GestionRegistre {
    private GestionRegistre() {
        throw new IllegalStateException("Utility class: On ne peut pas l'instancier");
    }
    public static String classFinCour="";
    public static String etiqFinCour="";
    private static int indiceRegistreGB = 0;
    private static int indiceRegistreLB = 0;
    private static int indiceRegistreSP = 0;
    private static int compteurTempUsage = 0;

    public static int getCompteurTempUsage(){return compteurTempUsage;}
    public static int getIndiceRegistreGB(){return indiceRegistreGB;}
    private int getIndiceRegistreLB(){return indiceRegistreLB;}
    private int getIndiceRegistreSP(){return indiceRegistreSP;}

    public static void IncCompteurTempUsage(){compteurTempUsage +=1;}
    private void  setIndiceRegistreGB(int indice) {indiceRegistreGB = indice;}
    public static void setIndiceRegistreLB(int indice) {indiceRegistreLB = indice;}
    private void setIndiceRegistreSP(int indice) {indiceRegistreSP = indice;}

    public static int obtainIndiceGPandIncrement(int offset){
        indiceRegistreGB += offset;
        return indiceRegistreGB;
    }

    public static int obtainIndiceLBandIncrement(int offset){
        indiceRegistreLB += offset;
        return indiceRegistreLB;
    }

    /*
    * methode pour obtenir le prochain registre libre à partir de 2 ou une adresse pile si impossible
    * il faut donc que j'ajoute un champ enUtilisation à mes registre ou garder
    * un dictionnaire sur mes registre libre. je pense je vais ajouer un champs
    * */
    public static GPRegister obtainFreeRegister(){
        for(int i=2; i<=15; i++){
            if(!Register.getR(i).isInUse()){
                Register.getR(i).setInUse(true);
                return Register.getR(i);
            }
        }
        //indiceRegistreSP += 1;
        // il faut retourner ici nulle et on saura
        // qu'on doit utiliser une adresse pile
        return null;
    }


    /*
    * Ecris les instructions pour evaluer l'expression droite dans register,
    * mettre la valeur de register dans la pile,
    * evaluer expression gauche dans register,
    * retirer la valeur de la pile (POP) dans r0
    * evaluer l'expression */
    public static void pushETpop(DecacCompiler compiler, AbstractExpr rigthExpr, AbstractExpr leftExpr, GPRegister register){
        rigthExpr.evalueExpr(compiler, register );
        compiler.addInstruction(new PUSH(register));
        leftExpr.evalueExpr(compiler, register );
        compiler.addInstruction(new POP(Register.R0));
    }
    public static void pushETpopEnd(DecacCompiler compiler, AbstractExpr rigthExpr, AbstractExpr leftExpr, GPRegister register){
        rigthExpr.evalueExprEnd(compiler, register );
        compiler.addInstructionEnd(new PUSH(register));
        leftExpr.evalueExprEnd(compiler, register );
        compiler.addInstructionEnd(new POP(Register.R0));
    }
//    public static void pushETpopMethod(DecacCompiler compiler, AbstractExpr rigthExpr, AbstractExpr leftExpr, GPRegister register){
//        rigthExpr.evalueExprMethod(compiler, register );
//        compiler.addInstructionEnd(new PUSH(register));
//        leftExpr.evalueExprMethod(compiler, register );
//        compiler.addInstructionEnd(new POP(Register.R0));
//    }
    public static void verifieNullReference(DecacCompiler compiler, GPRegister register){
        compiler.addInstruction(new CMP(new NullOperand(), register));
        compiler.addInstruction(new BEQ(new Label("dereferencement.null")));
    }
    public static void verifieNullReferenceEnd(DecacCompiler compiler, GPRegister register){
        compiler.addInstructionEnd(new CMP(new NullOperand(), register));
        compiler.addInstructionEnd(new BEQ(new Label("dereferencement.null")));
    }

}
