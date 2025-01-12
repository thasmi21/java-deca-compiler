package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl06
 * @date 01/01/2025
 */
public class DeclClass extends AbstractDeclClass {

    final private AbstractIdentifier className;
    final private AbstractIdentifier extendsName;
    final private ListBody classBody;

    public DeclClass(AbstractIdentifier className, AbstractIdentifier extendsName, ListBody classBody) {
        Validate.notNull(className);
        Validate.notNull(classBody);
        this.className = className;
        this.extendsName = extendsName;
        this.classBody = classBody;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        className.decompile(s);
        if (extendsName != null) {
            extendsName.decompile(s);
        }
        if (classBody != null){
            classBody.decompile(s);
        }
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        EnvironmentType envType = compiler.getEnvironmentType();
        if(extendsName != null) {
            envType.putClass(className.getName(),extendsName.getName(),getLocation());
        }
        else {
            envType.putClass(className.getName(),getLocation());
        }
        EnvironmentExp environmentExp=compiler.getEnvironmentType().getEnvironmentMain();
        Type type = envType.getEnvTypes().get(className.getName()).getType();
        VariableDefinition varDef = new VariableDefinition(type, this.getLocation());
        try {
            environmentExp.declare(className.getName(), varDef);
        } catch(EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("La variable "+className.getName()+" est déjà définie !",this.getLocation());
        }
        className.setDefinition(varDef);
        className.setType(type);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        EnvironmentExp environmentExp=((ClassDefinition)(compiler.getEnvironmentType().getEnvTypes().get(className.getName()))).getMembers();
        TypeDefinition typeDefinition= compiler.getEnvironmentType().getEnvTypes().get(className.getName());
        if(typeDefinition != null && typeDefinition.isClass()) {
            ClassDefinition classDefinition = (ClassDefinition)typeDefinition;
            classBody.verifyClassMembers(compiler,environmentExp,classDefinition);
        }
        else{
            throw new ContextualError(className.getName().getName()+"n'est pas une classe",getLocation());
        }
    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        EnvironmentExp environmentExp=((ClassDefinition)(compiler.getEnvironmentType().getEnvTypes().get(className.getName()))).getMembers();
        TypeDefinition typeDefinition= compiler.getEnvironmentType().getEnvTypes().get(className.getName());
        if(typeDefinition != null && typeDefinition.isClass()) {
            ClassDefinition classDefinition = (ClassDefinition)typeDefinition;
            classBody.verifyClassBody(compiler,environmentExp,classDefinition);
        }
        else{
            throw new ContextualError(className.getName().getName()+"n'est pas une classe",getLocation());
        }
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s,prefix,false);
        if (extendsName != null) {
            extendsName.prettyPrint(s,prefix+" extends ",false);
        }
        if (classBody != null) {
            classBody.prettyPrint(s,prefix,true);
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iterChildren(f);
        if (extendsName != null) {
            extendsName.iterChildren(f);
        }
        if (classBody != null){
            classBody.iterChildren(f);
        }
    }

}
