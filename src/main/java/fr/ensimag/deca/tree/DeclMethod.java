package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class DeclMethod extends AbstractBody {


    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private ListParams params;
    final private ListDeclVar declVar;
    final private ListInst inst;

    public DeclMethod(AbstractIdentifier type, AbstractIdentifier varName, ListParams params, ListDeclVar declVar, ListInst inst) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(params);
        Validate.notNull(declVar);
        Validate.notNull(inst);
        this.type = type;
        this.varName = varName;
        this.params = params;
        this.declVar = declVar;
        this.inst = inst;
    }



    @Override
    protected void verifyBody(DecacCompiler compiler,
                              EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
    }


    @Override
    public void codeGenBody(DecacCompiler compiler){
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        varName.decompile(s);
        params.decompile(s);
        declVar.decompile(s);
        inst.decompile(s);
        s.println(";");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        params.iter(f);
        declVar.iter(f);
        inst.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        declVar.prettyPrint(s, prefix, false);
        inst.prettyPrint(s, prefix, true);
    }


}
