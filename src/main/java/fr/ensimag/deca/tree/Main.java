package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);
    
    private ListDeclVar declVariables;
    private ListInst insts;
    public Main(ListDeclVar declVariables,
            ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        EnvironmentExp localEnv = new EnvironmentExp(null);
        LOG.debug("verify Main: start");
        declVariables.verifyListDeclVar(compiler,localEnv, null);
        insts.verifyListInst(compiler,localEnv,null,null);
        // A FAIRE: Appeler méthodes "verify*" de ListDeclVarSet et ListInst.
        // Vous avez le droit de changer le profil fourni pour ces méthodes
        // (mais ce n'est à priori pas nécessaire).
        LOG.debug("verify Main: end");
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) {
        // A FAIRE: traiter les déclarations de variables.

        declVariables.codeGenListDeclVar(compiler);
        compiler.addComment("Beginning of main instructions:");
        insts.codeGenListInst(compiler);

        // Il faudrait aussi que l'on puisse ajouter au debut du programme .ass l'instruction TSTO avec le bon #d calculé
        // Il doit y avoir une méthode qui permet d'ajouter une instruction en début de programme
        // Ajouter en début de programme :
        // TSTO #d ; taille maximale de la pile que l'on peut calculer qu'a la fin de la génération de code
        // BOV pile_pleine ; Instruction de branchement pour sauter à l'etiquette en cas de débordement

        // On doit gérer le cas ou on a une pile pleine et qu'il faut renvoyer une erreur
        compiler.addLabel(new Label("pile_pleine")); // On ajoute l'etiquette à laquelle il faut jump si il y a débordement
        compiler.addInstruction(new WSTR("Erreur: La pile est pleine")); // On écrit un message d'erreur
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR()); // Le programme s'arrête mais sur un message d'erreur
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }
 
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }
}
