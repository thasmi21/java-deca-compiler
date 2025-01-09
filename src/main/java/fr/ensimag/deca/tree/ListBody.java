package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;

public class ListBody extends TreeList<AbstractBody>{
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractBody i : getList()) {
            i.decompile(s);
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv
     *   its "parentEnvironment" corresponds to "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to
     *      the "env_exp_r" attribute
     * @param currentClass
     *          corresponds to "class" attribute (null in the main bloc).
     */
    void verifyListDeclField(DecacCompiler compiler, EnvironmentExp localEnv,
                             ClassDefinition currentClass) throws ContextualError {
        for(AbstractBody body : this.getList()){
            body.verifyBody(compiler, localEnv, currentClass);
        }
    }

    void codeGenListDeclField(DecacCompiler compiler){
        //on ajoute d'abord l'instruction pour augmenter la pile
        // on fera le TSTO après
        compiler.addInstruction(new ADDSP(this.getList().size()));
        for(AbstractBody body: this.getList()){
            body.codeGenBody(compiler);
        }
    }
}
