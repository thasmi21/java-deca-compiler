package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

public abstract class AbstractParams extends Tree {

    /**
     * Implements non-terminal "decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv
     *   its "parentEnvironment" corresponds to the "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to
     *      the synthetized attribute
     * @param currentClass
     *          corresponds to the "class" attribute (null in the main bloc).
     */
    protected abstract void verifyParams(DecacCompiler compiler,
                                          EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Generate assembly code for the declaration.
     *
     * @param compiler
     */
    public abstract void codeGenParams(DecacCompiler compiler);
}
