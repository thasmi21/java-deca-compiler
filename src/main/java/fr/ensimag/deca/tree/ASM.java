package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class ASM extends AbstractInst {


    final private String string;
    final private Location location;


    public ASM(String string, Location location) {
        Validate.notNull(string);
        Validate.notNull(location);
        this.string = string;
        this.location = location;
    }

    @Override
    protected Type verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType) throws ContextualError {
        return returnType;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("asm (");
        s.print(string);
        s.println(");");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        s.println(prefix+string);
    }


}

