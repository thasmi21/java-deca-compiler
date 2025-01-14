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

    public void codeGenListClass(DecacCompiler compiler) {
        //throw new UnsupportedOperationException("To be implemented");
        // On commence par construire la table des méthodes de Object quoi qu'il arrive
        compiler.addComment("Construction de la table des methodes de Object");
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(1, Register.GB)));
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(2, Register.GB)));

        for (AbstractDeclClass c : getList()) {
            // Ceci correspond à la passe 1
            declareTableMethode(compiler,c);
        }
        //throw new UnsupportedOperationException("Not Finish yet");
    }

    public void declareTableMethode(DecacCompiler compiler, AbstractDeclClass c) {
        // Pour class on veut récupérer si elle en a ses super class
        // Si elle n'en a pas sa super class va être Object
        String debComment = "Construction de la table des méthode de ";

        // Maintenant on va itérer sur les méthodes de chaque classe
        if (c instanceof DeclClass) {
            DeclClass declClass = (DeclClass) c;
            TypeDefinition nom = compiler.getEnvironmentType().getEnvTypes().get(((DeclClass) c).getSymbol());
            if(nom instanceof ClassDefinition){
                ClassDefinition classDef = (ClassDefinition) nom;
                String endComment = String.valueOf(classDef.getType()); // J'espère que cela récupère le nom de la classe
                compiler.addComment(debComment + endComment);
                ClassDefinition parent = classDef.getSuperClass();
                if (parent == null) { // Si la classe n'hérite que de Object
                    RegisterOffset regOff =  new RegisterOffset(1, Register.GB);
                    // Si on rentre dans cette boucle c'est que la class c n'hérite que de Object
                    compiler.addInstruction(new LEA(regOff,Register.R0));
                    compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(3, Register.GB)));
                    compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
                    compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(4, Register.GB)));
                }
            }
        }
    }


}
