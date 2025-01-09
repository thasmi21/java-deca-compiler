package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Deca Identifier
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Identifier extends AbstractIdentifier {
    
    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("Identifier " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a class definition.
     */
    @Override
    public ClassDefinition getClassDefinition() {
        try {
            return (ClassDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a class identifier, you can't call getClassDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a method definition.
     */
    @Override
    public MethodDefinition getMethodDefinition() {
        try {
            return (MethodDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a method identifier, you can't call getMethodDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public FieldDefinition getFieldDefinition() {
        try {
            return (FieldDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a field identifier, you can't call getFieldDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public VariableDefinition getVariableDefinition() {
        try {
            return (VariableDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a ExpDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public ExpDefinition getExpDefinition() {
        try {
            return (ExpDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Exp identifier, you can't call getExpDefinition on it");
        }
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Symbol getName() {
        return name;
    }

    private Symbol name;

    public Identifier(Symbol name) {
        Validate.notNull(name);
        this.name = name;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        ExpDefinition def = localEnv.get(name);
        if (def == null) {
            throw new ContextualError("Identifier '" + name.getName() + "' is not defined", getLocation());
        }
        this.setDefinition(def);
        this.setType(def.getType());
        return def.getType();
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        //on recupere sa valeur et on charge
        //Mais si c'est une classe par exemple il faut gérer cela.
        //Pour l'instant j'essaie jeuste de faire le cas ou c'est un int
        DAddr op = this.getExpDefinition().getOperand();
        if(this.getType() instanceof IntType){
            compiler.addInstruction(new LOAD(op, Register.getR(1)));
            compiler.addInstruction(new WINT());
        }
        else if(this.getType() instanceof BooleanType){
            GPRegister r1 = GestionRegistre.obtainFreeRegister();
            compiler.addInstruction(new LOAD(op, r1));
            Label E = new Label(this.toString().replace('@', '_'));
            Label fin = new Label(this.toString().replace('@', '_')+"_fin");
            compiler.addInstruction(new CMP(0, r1));
            compiler.addInstruction(new BNE(E));
            compiler.addInstruction(new WSTR("false"));
            compiler.addInstruction(new BRA(fin));
            compiler.addLabel(E);
            compiler.addInstruction(new WSTR("true"));
            compiler.addLabel(fin);
            r1.setInUse(false);
        } else if (this.getType() instanceof FloatType) {
            compiler.addInstruction(new LOAD(op, Register.getR(1)));
            compiler.addInstruction(new WFLOAT());
        }


    }

    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        DAddr op = this.getExpDefinition().getOperand();
        compiler.addInstruction(new LOAD(op, r));


        Label E = new Label(thenIns.toString().replace('@', '_'));
        Label fin = new Label(thenIns.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new CMP(0, r));
        compiler.addInstruction(new BNE(E));
        elseIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        thenIns.codeGenListInst(compiler);
        compiler.addLabel(fin);
        r.setInUse(false);
    }
    @Override
    public void genWhileCode(DecacCompiler compiler, Label label){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        DAddr op = this.getExpDefinition().getOperand();
        compiler.addInstruction(new LOAD(op, r));

        compiler.addInstruction(new CMP(0, r)); //si vrai
        compiler.addInstruction(new BNE(label));
        r.setInUse(false);
    }

    @Override
    protected void evalueExpr(DecacCompiler compiler, GPRegister register ) {
        DAddr op = this.getExpDefinition().getOperand();
        compiler.addInstruction(new LOAD(op, register));
    }



    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * @param compiler contains "env_types" attribute
     */
    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        EnvironmentType env = compiler.getEnvironmentType();
        TypeDefinition typeDef = env.defOfType(name);
        if (typeDef == null) {
            throw new ContextualError("Type '" + name.getName() + "' is not defined", getLocation());
        }
        setDefinition(typeDef);
        return typeDef.getType();
    }
    
    
    private Definition definition;


    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(name.toString() + " ");
    }

    @Override
    String prettyPrintNode() {
        return "Identifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
    }
}
