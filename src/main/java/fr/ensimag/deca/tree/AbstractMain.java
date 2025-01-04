package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Main block of a Deca program.
 *
 * @author gl06
 * @date 01/01/2025
 */
public abstract class AbstractMain extends Tree {

    protected abstract void codeGenMain(DecacCompiler compiler);


    /**
     * Implements non-terminal "main" of [SyntaxeContextuelle] in pass 3 
     */
    protected abstract void verifyMain(DecacCompiler compiler) throws ContextualError, EnvironmentExp.DoubleDefException;
}
