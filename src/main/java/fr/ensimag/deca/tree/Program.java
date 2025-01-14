package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.AbstractLine;
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
        classes.verifyListClass(compiler);
        classes.verifyListClassMembers(compiler);
        classes.verifyListClassBody(compiler);
        main.verifyMain(compiler);
        LOG.debug("verify program: end");
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // A FAIRE: compléter ce squelette très rudimentaire de code
        // Il faudrait une méthode qui calcule de combien on doit ADDSP
        // On a : ADDSP = nb_var_glob + pour chaque class ( @table_super_class + @Code_methods_Object.equals + @Code_method_de_la_class)
        int toAddStack = compiler.toADDSP(compiler);
        compiler.addInstruction(new ADDSP(7)); // On met 7 dans le cadre du prog one_class_no_method.deca
        // 3 varGlo + 2classObject + 2 ClassSimple
        compiler.addComment("--------------------------------------------------");
        compiler.addComment("       Construction des tables des methodes       ");
        compiler.addComment("--------------------------------------------------");
        classes.codeGenListClass(compiler);
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
        int val = GestionRegistre.getIndiceRegistreGB() + GestionRegistre.getCompteurTempUsage();
        compiler.addFirst(new TSTO(val));

        Line firstCom = new Line("start main program");
        compiler.addFirst(firstCom);

        for (AbstractLine l : compiler.getToExecuteEnd()) {
            compiler.add(l);
        }
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
