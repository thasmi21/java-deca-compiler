package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;

/**
 * 
 * @author gl06
 * @date 01/01/2025
 */
public class ListInst extends TreeList<AbstractInst> {

    /**
     * Implements non-terminal "list_inst" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv corresponds to "env_exp" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     * @param returnType
     *          corresponds to "return" attribute (void in the main bloc).
     */    
    public Type verifyListInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {

            Type t = null;
            Type type;
            for (AbstractInst i : getList()) {
                type = i.verifyInst(compiler, localEnv, currentClass, returnType);
                if (!type.isVoid()) {
                    if (type.sameType(returnType) || type.isVoid()) {
                        t = type;
                    } else {
                        throw new ContextualError("Le type de retour n'est pas le bon", getLocation());
                    }
                }
            }
            return returnType;
    }

    public void codeGenListInst(DecacCompiler compiler) {
        for (AbstractInst i : getList()) {
            i.codeGenInst(compiler);
        }
    }
    public void codeGenListInstMethod(DecacCompiler compiler) {
        for (AbstractInst i : getList()) {
            i.codeGenInstMethod(compiler);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractInst i : getList()) {
            i.decompileInst(s);
            s.println();
        }
    }
}
