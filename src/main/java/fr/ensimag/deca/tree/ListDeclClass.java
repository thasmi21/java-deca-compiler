package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.log4j.Logger;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClass: start");
        for (AbstractDeclClass c : getList()) {
            c.verifyClass(compiler);
        }
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClass c : getList()) {
            c.verifyClassMembers(compiler);
        }
    }
    
    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClass c : getList()) {
            c.verifyClassBody(compiler);
        }
        LOG.debug("verify listClass: end");
    }

    public void codeGenListDeclClass(DecacCompiler compiler) {
        //throw new UnsupportedOperationException("To be implemented");
        // On commence par construire la table des méthodes de Object quoi qu'il arrive
        compiler.addComment("Construction de la table des methodes de Object");
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1), Register.GB)));
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1), Register.GB)));

        // On doit écrire le code de la méthode de Object
        // Pour le moment on crée simplement l'étiquette qui met 0 dans R0 (elle ne fait rien)
        GPRegister reg1 = GestionRegistre.obtainFreeRegister();
        GPRegister reg2 = GestionRegistre.obtainFreeRegister();
        compiler.getToExecuteEnd().add(new Line(new Label("code.Object.equals")));
        compiler.getToExecuteEnd().add(new Line(new LOAD(new RegisterOffset(-3,Register.LB),reg1))); // On récupère la valeur du paramètre other de la méthode equals
        compiler.getToExecuteEnd().add(new Line(new LOAD(new RegisterOffset(-2,Register.LB),reg2))); // On récupère l'instance de this
        compiler.getToExecuteEnd().add(new Line(new CMP(reg1,reg2)));
        compiler.getToExecuteEnd().add(new Line(new BEQ(new Label("code.Object.equals.true"))));
        compiler.getToExecuteEnd().add(new Line(new LOAD(0,Register.R0))); // Si ce n'est pas égal on met 0 dans R0
        compiler.getToExecuteEnd().add(new Line(new RTS()));
        compiler.getToExecuteEnd().add(new Line(new Label("code.Object.equals.true")));
        compiler.getToExecuteEnd().add(new Line(new LOAD(1, Register.R0))); // Si c'est égal on met 1 dans R0
        compiler.getToExecuteEnd().add(new Line(new RTS())); // Pour le moment la méthode Object.equals ne fait rien
        reg1.setInUse(false);
        reg2.setInUse(false);

        for (AbstractDeclClass declClass : getList()) {
            // Ceci correspond à la passe 1
            declClass.declareTableMethode(compiler);
            //declClass.codeGenDeclClass(compiler);
        }
        //throw new UnsupportedOperationException("Not Finish yet");
    }

    public void codeGenPass2(DecacCompiler compiler){
        compiler.getToExecuteEnd().add(new Line("debut initialisation des Champs des classes"));
        for (AbstractDeclClass declClass : getList()) {
            //on prend chaque classe et on genere son init et son codeGen
            declClass.codeGenDeclClass(compiler);
        }
        compiler.getToExecuteEnd().add(new Line("fin initialisation des Champs et methodes des classes"));
    }
}
