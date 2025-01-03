package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl06
 * @date 01/01/2025
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {

    @Override
    public void decompile(IndentPrintStream s) {
        throw new UnsupportedOperationException("Not yet implemented");
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
    void verifyListDeclVar(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                for(AbstractDeclVar declVar : this.getList()){
                    declVar.verifyDeclVar(compiler, localEnv, currentClass);
        }
    }

    /**
     * On créer une méthode de génération de code
     */
    void codeGenListDeclVar(DecacCompiler compiler) {
        // On commence par faire de la place dans la pile,
        // autant qu'il y a de variables définies
        compiler.addInstruction(new ADDSP(getList().size()));

        // Pour chaque variable on va appeler une nouvelle méthode de genCode
        for (AbstractDeclVar declVar : this.getList()){
            declVar.codeGenDeclVar(compiler);
        }
    }


}
