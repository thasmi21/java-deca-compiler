package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

public abstract class AbstractBody extends Tree{
    protected abstract void codeGenBody(DecacCompiler compiler);


    /**
     * Implements non-terminal "main" of [SyntaxeContextuelle] in pass 3
     */
    protected abstract void verifyClassMembers(DecacCompiler compiler,
                                       EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError;

    protected abstract void verifyClassBody(DecacCompiler compiler,
                                               EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError;
}


