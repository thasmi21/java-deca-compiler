package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
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
    protected void verifyClassMembers(DecacCompiler compiler,
                              EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type t = this.type.verifyType(compiler);
        VariableDefinition varDef = new VariableDefinition(t, this.getLocation());
        try {
            localEnv.declare(this.varName.getName(), varDef);
        } catch(EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("La variable "+varName.getName()+" est déjà définie !",this.getLocation());
        }
        this.varName.setDefinition(varDef);
        this.varName.setType(t);
        params.verifyParams(compiler,new EnvironmentExp(null));
    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler,
                                   EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError{
        EnvironmentExp environmentExp=new EnvironmentExp(null);
        params.verifyParams(compiler,environmentExp);
        declVar.verifyListDeclVar(compiler,environmentExp,currentClass);
        inst.verifyListInst(compiler,environmentExp,currentClass,varName.getType());
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
