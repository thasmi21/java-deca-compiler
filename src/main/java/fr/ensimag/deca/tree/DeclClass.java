package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
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

    public SymbolTable.Symbol getSymbol() {
        return className.getName();
    }

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
        EnvironmentExp environmentExp=compiler.environmentType.getEnvironmentMain();
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
    protected void codeGenDeclClass(DecacCompiler compiler) {
        // Pour chaque class, on doit définir sa table des méthodes dans la passe 1
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

    @Override
    public void declareTableMethode(DecacCompiler compiler) {
        // Pour class on veut récupérer si elle en a ses super class
        // Si elle n'en a pas sa super class va être Object
        String debComment = "Construction de la table des méthode de ";

        // Maintenant on va itérer sur les méthodes de chaque classe
        DeclClass declClass = (DeclClass) this;
        TypeDefinition nom = compiler.getEnvironmentType().getEnvTypes().get(((DeclClass) this).getSymbol());
        if(nom instanceof ClassDefinition){
            ClassDefinition classDef = (ClassDefinition) nom;
            String endComment = String.valueOf(classDef.getType()); // J'espère que cela récupère le nom de la classe
            compiler.addComment(debComment + endComment);
            ClassDefinition parent = classDef.getSuperClass();
            if (parent == null) { // Si la classe n'hérite que de Object
                RegisterOffset regOff =  new RegisterOffset(1, Register.GB);
                // Si on rentre dans cette boucle c'est que la class c n'hérite que de Object
                compiler.addInstruction(new LEA(regOff,Register.R0));
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(3, Register.GB)));
                compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(4, Register.GB)));
            }
        }
    }
}
