package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class ASM extends AbstractBody {


    final private String string;
    final private Location location;


    public ASM(String string, Location location) {
        Validate.notNull(string);
        Validate.notNull(location);
        this.string = string;
        this.location = location;
    }


    @Override
    protected void verifyClassBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {

    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler,
                              EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
    }


    @Override
    public void codeGenBody(DecacCompiler compiler){
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected
    void iterChildren(TreeFunction f) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
    }


}

