package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.instructions.*;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify program: start");
        //getClasses().verifyListClass(compiler);
        main.verifyMain(compiler);
        //decommenter la ligne au dessus par Victor
        //throw new UnsupportedOperationException("not yet implemented");
        LOG.debug("verify program: end");
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // A FAIRE: compléter ce squelette très rudimentaire de code
        compiler.addComment("Main program");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());
        // On doit gérer le cas ou on a une pile pleine et qu'il faut renvoyer une erreur
        compiler.addLabel(new Label("pile_pleine")); // On ajoute l'etiquette à laquelle il faut jump si il y a débordement
        compiler.addInstruction(new WSTR("Erreur: La pile est pleine")); // On écrit un message d'erreur
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR()); // Le programme s'arrête mais sur un message d'erreur

        // On doit ajouter l'instruction TSTO
        // Pour le moment on la fixe à une certaine constante mais il faudra être
        // capable de la calculer par la suite
        Label stackFull = new Label("pile_pleine");
        compiler.addFirst(new BOV(stackFull));
        compiler.addFirst(new TSTO(3));

        Line firstCom = new Line("start main program");
        compiler.addFirst(firstCom);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }
}
