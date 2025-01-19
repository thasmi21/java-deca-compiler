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
    public ParamDefinition getParamDefinition() {
        try {
            return (ParamDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Param identifier, you can't call getParamDefinition on it");
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
        EnvironmentExp env = localEnv;
        ExpDefinition def;
        while(env != null) {
            def= env.get(name);
            if (def != null) {
                this.setDefinition(def);
                this.setType(def.getType());
                return def.getType();
            }
            env=env.getParentEnvironment();
        }
        throw new ContextualError("Identifier '" + name.getName() + "' is not defined", getLocation());
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
    public void codeGenPrintX(DecacCompiler compiler){
        //on recupere sa valeur et on charge
        //Mais si c'est une classe par exemple il faut gérer cela.
        //Pour l'instant j'essaie jeuste de faire le cas ou c'est un int
        DAddr op = this.getExpDefinition().getOperand();
        if (this.getType() instanceof FloatType) {
            compiler.addInstruction(new LOAD(op, Register.getR(1)));
            compiler.addInstruction(new WFLOATX());
        }
        else if(this.getType() instanceof IntType){
            ConvFloat convFloat = new ConvFloat(this);
            convFloat.evalueExpr(compiler,Register.R1);
            compiler.addInstruction(new WFLOATX());
        }
        else {
            throw new RuntimeException("on ne peut print qu'un float en hexadecimal !");
        }
    }

    @Override
    public void codeGenPrintMethod(DecacCompiler compiler){
        // i lfaut que je gère le cas où c'est un paramètre ou
        //field ou une appelle de methode
        DAddr op = this.getExpDefinition().getOperand();
        if(getDefinition().isField()){
            int index = ((FieldDefinition)getDefinition()).getIndex();
            DAddr addrParent = new RegisterOffset(-2, Register.LB);
            compiler.addInstructionEnd(new LOAD(addrParent, Register.getR(1)));
            op = new RegisterOffset(index, Register.getR(1));
            //compiler.addInstructionEnd(new LOAD(new RegisterOffset(index, Register.getR(1)), Register.getR(1)));
            //compiler.addInstructionEnd(new WINT());
        }
        else if(getDefinition().isParam()){
            int index = getParamDefinition().getIndex();
            DAddr addrParam = new RegisterOffset(index, Register.LB);
            op = addrParam;
            //compiler.addInstructionEnd(new LOAD(addrParam, Register.getR(1)));
            //compiler.addInstructionEnd(new WINT());

        }

        if (this.getType() instanceof IntType){
            compiler.addInstructionEnd(new LOAD(op, Register.getR(1)));
            compiler.addInstructionEnd(new WINT());
        }
        else if(this.getType() instanceof BooleanType){
            GPRegister r1 = GestionRegistre.obtainFreeRegister();
            compiler.addInstructionEnd(new LOAD(op, r1));
            Label E = new Label(this.toString().replace('@', '_'));
            Label fin = new Label(this.toString().replace('@', '_')+"_fin");
            compiler.addInstructionEnd(new CMP(0, r1));
            compiler.addInstructionEnd(new BNE(E));
            compiler.addInstructionEnd(new WSTR("false"));
            compiler.addInstructionEnd(new BRA(fin));
            compiler.addLabelEnd(E);
            compiler.addInstructionEnd(new WSTR("true"));
            compiler.addLabelEnd(fin);
            r1.setInUse(false);
        } else if (this.getType() instanceof FloatType) {
            compiler.addInstructionEnd(new LOAD(op, Register.getR(1)));
            compiler.addInstructionEnd(new WFLOAT());
        }


    }


    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        DAddr op = this.getExpDefinition().getOperand();
        if(getDefinition().isField()){
            int index = ((FieldDefinition)getDefinition()).getIndex();
            DAddr addrParent = new RegisterOffset(-2, Register.LB);
            compiler.addInstructionEnd(new LOAD(addrParent, Register.getR(1)));
            op = new RegisterOffset(index, Register.getR(1));
            //compiler.addInstructionEnd(new LOAD(new RegisterOffset(index, Register.getR(1)), Register.getR(1)));
            //compiler.addInstructionEnd(new WINT());
        }
        else if(getDefinition().isParam()){
            int index = getParamDefinition().getIndex();
            DAddr addrParam = new RegisterOffset(index, Register.LB);
            op = addrParam;
            //compiler.addInstructionEnd(new LOAD(addrParam, Register.getR(1)));
            //compiler.addInstructionEnd(new WINT());

        }
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
        if(getDefinition().isField()){
            int index = ((FieldDefinition)getDefinition()).getIndex();
            DAddr addrParent = new RegisterOffset(-2, Register.LB);
            compiler.addInstructionEnd(new LOAD(addrParent, Register.getR(1)));
            op = new RegisterOffset(index, Register.getR(1));
            //compiler.addInstructionEnd(new LOAD(new RegisterOffset(index, Register.getR(1)), Register.getR(1)));
            //compiler.addInstructionEnd(new WINT());
        }
        else if(getDefinition().isParam()){
            int index = getParamDefinition().getIndex();
            DAddr addrParam = new RegisterOffset(index, Register.LB);
            op = addrParam;
            //compiler.addInstructionEnd(new LOAD(addrParam, Register.getR(1)));
            //compiler.addInstructionEnd(new WINT());

        }
        compiler.addInstruction(new LOAD(op, r));

        compiler.addInstruction(new CMP(0, r)); //si vrai
        compiler.addInstruction(new BNE(label));
        r.setInUse(false);
    }

    @Override
    public void genIfElseCodeMethod(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        DAddr op = this.getExpDefinition().getOperand();
        compiler.addInstructionEnd(new LOAD(op, r));


        Label E = new Label(thenIns.toString().replace('@', '_'));
        Label fin = new Label(thenIns.toString().replace('@', '_')+"_fin");
        compiler.addInstructionEnd(new CMP(0, r));
        compiler.addInstructionEnd(new BNE(E));
        elseIns.codeGenListInstMethod(compiler);
        compiler.addInstructionEnd(new BRA(fin));
        compiler.addLabelEnd(E);
        thenIns.codeGenListInstMethod(compiler);
        compiler.addLabelEnd(fin);
        r.setInUse(false);
    }
    @Override
    public void genWhileCodeMethod(DecacCompiler compiler, Label label){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        DAddr op = this.getExpDefinition().getOperand();
        compiler.addInstructionEnd(new LOAD(op, r));

        compiler.addInstructionEnd(new CMP(0, r)); //si vrai
        compiler.addInstructionEnd(new BNE(label));
        r.setInUse(false);
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register ) {
        DAddr op = this.getExpDefinition().getOperand();
        compiler.addInstruction(new LOAD(op, register));
    }
    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register ) {
        if(getDefinition().isField()){
            int index = ((FieldDefinition)getDefinition()).getIndex();
            DAddr addrParent = new RegisterOffset(-2, Register.LB);
            compiler.addInstructionEnd(new LOAD(addrParent, register));
            compiler.addInstructionEnd(new LOAD(new RegisterOffset(index, register), register));
            return;
        }
        else if(getDefinition().isParam()){
            int index = getParamDefinition().getIndex();
            DAddr addrParam = new RegisterOffset(index, Register.LB);
            compiler.addInstructionEnd(new LOAD(addrParam, register));
            return;
        }
        DAddr op = this.getExpDefinition().getOperand();
        compiler.addInstructionEnd(new LOAD(op, register));
    }

    /*public void evalueExprField(DecacCompiler compiler, GPRegister register, DVal addrParent){
        compiler.getToExecuteEnd().add(new Line(new LOAD(addrParent, register)));
        compiler.getToExecuteEnd().add(new Line(new LOAD(new RegisterOffset(indiceField, register), register)));
    }*/

//    @Override
//    public void evalueExprMethod(DecacCompiler compiler, GPRegister register) {
//
//        if(getDefinition().isField()){
//            int index = ((FieldDefinition)getDefinition()).getIndex();
//            DAddr addrParent = new RegisterOffset(-2, Register.LB);
//            compiler.addInstructionEnd(new LOAD(addrParent, register));
//            compiler.addInstructionEnd(new LOAD(new RegisterOffset(index, register), register));
//            return;
//        }
//        else if(getDefinition().isParam()){
//            int index = getParamDefinition().getIndex();
//            DAddr addrParam = new RegisterOffset(index, Register.LB);
//            compiler.addInstructionEnd(new LOAD(addrParam, register));
//            return;
//        }
//        DAddr op = this.getExpDefinition().getOperand();
//        compiler.addInstructionEnd(new LOAD(op, register));
//    }

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
}
