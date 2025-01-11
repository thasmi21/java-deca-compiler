package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;

public class ListParams extends TreeList<AbstractParams> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractParams i : getList()) {
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
     */
    void verifyParams(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError {
        for(AbstractParams param : this.getList()){
            param.verifyParams(compiler, localEnv);
        }
    }

    void codeGenListDeclVar(DecacCompiler compiler){
        //on ajoute d'abord l'instruction pour augmenter la pile
        // on fera le TSTO après
        compiler.addInstruction(new ADDSP(this.getList().size()));
        for(AbstractParams param: this.getList()){
            param.codeGenParams(compiler);
        }
    }

}
