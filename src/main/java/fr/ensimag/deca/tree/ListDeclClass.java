package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
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
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(1, Register.GB)));
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.objet.equals")), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(2, Register.GB)));

        for (AbstractDeclClass declClass : getList()) {
            // Ceci correspond à la passe 1
            declClass.declareTableMethode(compiler);
            //declClass.codeGenDeclClass(compiler);
        }
        //throw new UnsupportedOperationException("Not Finish yet");
    }

    public void toAddStack(DecacCompiler compiler) {
        int result = 0;
        int nbVarGlo = compiler.getNbVarGlo();
        result = result + nbVarGlo;
        for (AbstractDeclClass declClass : getList()) {
            result = result + 2;
        }
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
