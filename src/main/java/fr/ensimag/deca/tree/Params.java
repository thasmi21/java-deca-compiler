package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class Params extends AbstractParams {


    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;

    public Params(AbstractIdentifier type, AbstractIdentifier varName) {
        Validate.notNull(type);
        Validate.notNull(varName);
        this.type = type;
        this.varName = varName;

    }

    @Override
    protected void verifyParams(DecacCompiler compiler,
                                 EnvironmentExp localEnv) throws ContextualError {
        Type t = this.type.verifyType(compiler);
        VariableDefinition varDef = new VariableDefinition(t, this.getLocation());
        if (t.isVoid()) {
            throw new ContextualError("La variable ne peut pas être de type void !", this.getLocation());
        }
        try {
            localEnv.declare(this.varName.getName(), varDef);
        } catch(EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("La variable "+varName.getName()+" est déjà définie !",this.getLocation());
        }
        this.varName.setDefinition(varDef);
        this.varName.setType(t);
    }

    @Override
    public void codeGenParams(DecacCompiler compiler){
        //throw new UnsupportedOperationException("Not Yet implemented by us");
        this.varName.getExpDefinition().setOperand(new RegisterOffset(1, Register.GB));
        //initialiser l'adresse

    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        varName.decompile(s);
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, true);
    }


    public Type getType() {
        return varName.getType();
    }
}