package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

import java.io.PrintStream;

public class New extends AbstractExpr{

    AbstractIdentifier identifier;
    ClassDefinition classDef = null;

    public New(AbstractIdentifier identifier) {
        super();
        this.identifier = identifier;
    }
    public ClassDefinition getClassDef(){
        return classDef;
    }
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        EnvironmentType environmentType=compiler.getEnvironmentType();
        if(environmentType.getEnvTypes().containsKey(identifier.getName())) {
            TypeDefinition typeDefinition=environmentType.getEnvTypes().get(identifier.getName());
            if(typeDefinition instanceof ClassDefinition) {
                ClassDefinition classDefinition=(ClassDefinition)typeDefinition;
                this.classDef = classDefinition;
                Type type= classDefinition.getType();
                setType(type);
                return type;
            }
        }
        throw new ContextualError("le type donnée n'est pas une class",getLocation());
    }
    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }

    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        //ne fait rien
        compiler.addInstructionEnd(new WSTR("L'instruction n'est pas utilisé ! On préfère ne rien génerer"));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(" new");
        identifier.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        identifier.iterChildren(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        identifier.prettyPrintChildren(s, prefix);
    }
}
