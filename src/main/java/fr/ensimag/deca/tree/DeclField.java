package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class DeclField extends AbstractDeclField {

    final private Visibility visibility;
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclField(Visibility visibility,AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        Validate.notNull(visibility);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
        this.visibility = visibility;
    }




    @Override
    protected void verifyClassMembers(DecacCompiler compiler,
                                   EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type t = type.verifyType(compiler);
        FieldDefinition fieldDefinition = new FieldDefinition(t,getLocation(),visibility,currentClass,currentClass.getNumberOfFields());
        currentClass.incNumberOfFields();
        if (t.isVoid()) {
            throw new ContextualError("La variable ne peut pas être de type void !", this.getLocation());
        }
        initialization.verifyInitialization(compiler,t, localEnv, currentClass);
        try {
            localEnv.declare(this.varName.getName(), fieldDefinition);
        } catch(EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("La variable "+varName.getName()+" est déjà définie !",this.getLocation());
        }
        this.varName.setDefinition(fieldDefinition);
        this.varName.setType(t);
    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler,
                                            EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError{

    }

    public AbstractInitialization getInitialization(){
        return this.initialization;
    }
    public AbstractIdentifier getTypeIdentifier() {
        return this.type;
    }
    public AbstractIdentifier getVarName() {
        return this.varName;
    }

    @Override
    public void codeGenBody(DecacCompiler compiler){
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if(visibility == Visibility.PROTECTED) {
            s.print("protected ");
        }
        type.decompile(s);
        varName.decompile(s);
        if (initialization instanceof Initialization) {
            s.print("= ");
        }
        initialization.decompile(s);
        s.println(";");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        s.println(prefix + visibility.toString());
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }


}


