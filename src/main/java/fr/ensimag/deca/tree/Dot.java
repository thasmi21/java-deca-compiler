package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;

public class Dot extends AbstractLValue {

    private AbstractExpr select_expr;
    private AbstractIdentifier identifier;
    private ListExpr args;

    private boolean dotField = false;

    public Dot(AbstractExpr select_expr, AbstractIdentifier identifier) {
        this.select_expr = select_expr;
        this.identifier = identifier;
        this.args = null;
        this.dotField=true;
    }
    public AbstractExpr getSelectExpr(){
        return this.select_expr;
    }
    public AbstractIdentifier getIdentifier(){
        return this.identifier;
    }
    public boolean getDotField(){
        return this.dotField;
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler){
        if(dotField){
            DAddr addrParent = ((AbstractIdentifier)select_expr).getExpDefinition().getOperand();
            int indice = identifier.getFieldDefinition().getIndex();
            GPRegister register = GestionRegistre.obtainFreeRegister();
            boolean rWasNul = false;
            if(register == null){
                rWasNul = true;
                register = Register.getR(2);
                compiler.addInstruction(new PUSH(register));
            }
            compiler.addInstruction(new LOAD(addrParent, register));
            GestionRegistre.verifieNullReference(compiler, register);
            compiler.addInstruction(new LOAD(new RegisterOffset(indice, register), register));
            compiler.addInstruction(new LOAD(register, Register.R1));
            compiler.addInstruction(new WINT());
            if(rWasNul){
                compiler.addInstruction(new POP(register));
            }else{
                register.setInUse(false);
            }
        }else{
            GPRegister r = Register.getR(1);
            compiler.addInstructionEnd(new PUSH(r));
            this.evalueExpr(compiler, r);
            compiler.addInstruction(new WINT());
            compiler.addInstructionEnd(new POP(r));
        }
    }

    public Dot(AbstractExpr select_expr,AbstractIdentifier identifier,ListExpr args) {
        this.select_expr = select_expr;
        this.identifier = identifier;
        this.args = args;
    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register ){
        DAddr addrParent = ((AbstractIdentifier)select_expr).getExpDefinition().getOperand();
        if(dotField){
            //DAddr addrParent = ((AbstractIdentifier)select_expr).getExpDefinition().getOperand();
            compiler.addInstruction(new LOAD(addrParent, register));
            GestionRegistre.verifieNullReference(compiler, register);
            compiler.addInstruction(new LOAD(new RegisterOffset((identifier.getFieldDefinition()).getIndex(),register), register));
        }
        //il s'agit donc d'un appel de methode
        else {
            //on evalue les paramètres et on les empiles
            int nbrParam = args.getList().size();
            int cpt = -1;
            compiler.addInstruction(new ADDSP(nbrParam));
            compiler.addInstruction(new LOAD(addrParent, register));
            compiler.addInstruction(new STORE(register, new RegisterOffset(0, Register.SP)));
            for(AbstractExpr expr: args.getList()){
                expr.evalueExpr(compiler, register);
                compiler.addInstruction(new STORE(register, new RegisterOffset(cpt, Register.SP)));
                cpt--;
            }
            compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), register));
            GestionRegistre.verifieNullReference(compiler, register);
            compiler.addInstruction(new LOAD(new RegisterOffset(0, register), register));
            //appel de la methode dont -il s'agit
            //identifier.getMethodDefinition().getIndex() /*
            // il faudra qu'on puisse recuperer après le bon index*/
            compiler.addInstruction(new BSR(new RegisterOffset(identifier.getMethodDefinition().getOffSet(), register)));
            //fin appel de la methode
            compiler.addInstruction(new SUBSP(nbrParam));
            compiler.addInstruction(new LOAD(Register.R0, register));
            //System.out.println("Il y'a "+ args.getList().size()+" paramètres");
        }
    }

    @Override
    public void evalueExprEnd(DecacCompiler compiler, GPRegister register ){
        DAddr addrParent;
        if(dotField) {
            if (select_expr instanceof AbstractIdentifier) {
                addrParent = ((AbstractIdentifier)select_expr).getExpDefinition().getOperand();
                compiler.addInstructionEnd(new LOAD(addrParent, register));
                GestionRegistre.verifieNullReference(compiler, register);
                compiler.addInstructionEnd(new LOAD(new RegisterOffset((identifier.getFieldDefinition()).getIndex(), register), register));

            } else if (select_expr instanceof This) {

                compiler.addInstructionEnd(new LOAD(new RegisterOffset(-2, Register.LB), register));
                GestionRegistre.verifieNullReferenceEnd(compiler, register);
                compiler.addInstructionEnd(new LOAD(new RegisterOffset((identifier.getFieldDefinition()).getIndex(),register), register));

            }
        }
        //il s'agit donc d'un appel de methode
        else {
            if(select_expr instanceof Identifier) {
                addrParent = ((AbstractIdentifier) select_expr).getExpDefinition().getOperand();
            }else if(select_expr instanceof This){
                addrParent = new RegisterOffset(-2, Register.LB);
         }else{
                throw new IllegalArgumentException("Vous ne pouvez pas faire cette Opération");
            }
            //on evalue les paramètres et on les empiles
            int nbrParam = args.getList().size();
            int cpt = -1;
            compiler.addInstructionEnd(new ADDSP(nbrParam));
            compiler.addInstructionEnd(new LOAD(addrParent, register));
            compiler.addInstructionEnd(new STORE(register, new RegisterOffset(0, Register.SP)));
            for(AbstractExpr expr: args.getList()){
                expr.evalueExprEnd(compiler, register);
                compiler.addInstructionEnd(new STORE(register, new RegisterOffset(cpt, Register.SP)));
                cpt--;
            }
            compiler.addInstructionEnd(new LOAD(new RegisterOffset(0, Register.SP), register));
            if(!(select_expr instanceof This)){
                GestionRegistre.verifieNullReferenceEnd(compiler, register);
            }
            compiler.addInstructionEnd(new LOAD(new RegisterOffset(0, register), register));
            //appel de la methode dont -il s'agit
            //identifier.getMethodDefinition().getIndex() /*
            // il faudra qu'on puisse recuperer après le bon index*/
            compiler.addInstructionEnd(new BSR(new RegisterOffset(identifier.getMethodDefinition().getOffSet(), register)));
            //fin appel de la methode
            compiler.addInstructionEnd(new SUBSP(nbrParam));
            compiler.addInstructionEnd(new LOAD(Register.R0, register));
            //System.out.println("Il y'a "+ args.getList().size()+" paramètres");


        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        if(args == null){
            return;
        }
        DAddr addrParent;
        GPRegister register = Register.getR(2);
        compiler.addInstruction(new PUSH(register));
        addrParent = ((AbstractIdentifier)select_expr).getExpDefinition().getOperand();
        //on evalue les paramètres et on les empiles
        int nbrParam = args.getList().size();
        int cpt = -1;
        compiler.addInstruction(new ADDSP(nbrParam));
        compiler.addInstruction(new LOAD(addrParent, register));
        compiler.addInstruction(new STORE(register, new RegisterOffset(0, Register.SP)));
        for(AbstractExpr expr: args.getList()){
            expr.evalueExpr(compiler, register);
            compiler.addInstruction(new STORE(register, new RegisterOffset(cpt, Register.SP)));
            cpt--;
        }
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), register));
        GestionRegistre.verifieNullReference(compiler, register);
        compiler.addInstruction(new LOAD(new RegisterOffset(0, register), register));
        //appel de la methode dont -il s'agit
        //identifier.getMethodDefinition().getIndex() /*
        // il faudra qu'on puisse recuperer après le bon index*/
        compiler.addInstruction(new BSR(new RegisterOffset(identifier.getMethodDefinition().getOffSet(), register)));
        //fin appel de la methode
        compiler.addInstruction(new SUBSP(nbrParam));
        //compiler.addInstructionEnd(new LOAD(Register.R0, register));
        //System.out.println("Il y'a "+ args.getList().size()+" paramètres");
        compiler.addInstruction(new POP(register));
    }

    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        if(args == null){
            return;
        }
        DAddr addrParent;
        GPRegister register = Register.getR(2);
        compiler.addInstructionEnd(new PUSH(register));
        if(select_expr instanceof Identifier) {
            addrParent = ((AbstractIdentifier) select_expr).getExpDefinition().getOperand();
        }else if(select_expr instanceof This){
            addrParent = new RegisterOffset(-2, Register.LB);
        }else{
            throw new IllegalArgumentException("Vous ne pouvez pas faire cette Opération");
        }
        //on evalue les paramètres et on les empiles
        int nbrParam = args.getList().size();
        int cpt = -1;
        compiler.addInstructionEnd(new ADDSP(nbrParam));
        compiler.addInstructionEnd(new LOAD(addrParent, register));
        compiler.addInstructionEnd(new STORE(register, new RegisterOffset(0, Register.SP)));
        for(AbstractExpr expr: args.getList()){
            expr.evalueExprEnd(compiler, register);
            compiler.addInstructionEnd(new STORE(register, new RegisterOffset(cpt, Register.SP)));
            cpt--;
        }
        compiler.addInstructionEnd(new LOAD(new RegisterOffset(0, Register.SP), register));
        GestionRegistre.verifieNullReferenceEnd(compiler, register);
        compiler.addInstructionEnd(new LOAD(new RegisterOffset(0, register), register));
        //appel de la methode dont -il s'agit
        //identifier.getMethodDefinition().getIndex() /*
        // il faudra qu'on puisse recuperer après le bon index*/
        compiler.addInstructionEnd(new BSR(new RegisterOffset(identifier.getMethodDefinition().getOffSet(), register)));
        //fin appel de la methode
        compiler.addInstructionEnd(new SUBSP(nbrParam));
        //compiler.addInstructionEnd(new LOAD(Register.R0, register));
        //System.out.println("Il y'a "+ args.getList().size()+" paramètres");
        compiler.addInstructionEnd(new POP(register));
    }


    @Override
    public void codeGenPrintMethod(DecacCompiler compiler){
        DAddr addrParent;
        if(dotField) {
            if (select_expr instanceof AbstractIdentifier) {
                addrParent = ((AbstractIdentifier)select_expr).getExpDefinition().getOperand();
                compiler.addInstructionEnd(new LOAD(addrParent, Register.R1));
                GestionRegistre.verifieNullReferenceEnd(compiler, Register.R1);
                compiler.addInstructionEnd(new LOAD(new RegisterOffset((identifier.getFieldDefinition()).getIndex(), Register.R1), Register.R1));

            } else if (select_expr instanceof This) {

                compiler.addInstructionEnd(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
                //GestionRegistre.verifieNullReference(compiler, register);
                compiler.addInstructionEnd(new LOAD(new RegisterOffset((identifier.getFieldDefinition()).getIndex(),Register.R1), Register.R1));

            }
        }
        //il s'agit donc d'un appel de methode
        else {
            if(select_expr instanceof Identifier) {
                addrParent = ((AbstractIdentifier) select_expr).getExpDefinition().getOperand();
            }else if(select_expr instanceof This){
                addrParent = new RegisterOffset(-2, Register.LB);
            }else{
                throw new IllegalArgumentException("Vous ne pouvez pas faire cette Opération");
            }
                //on evalue les paramètres et on les empiles
                int nbrParam = args.getList().size();
                int cpt = -1;
                compiler.addInstructionEnd(new ADDSP(nbrParam));
                compiler.addInstructionEnd(new LOAD(addrParent, Register.R1));
                compiler.addInstructionEnd(new STORE(Register.R1, new RegisterOffset(0, Register.SP)));
                for(AbstractExpr expr: args.getList()){
                    expr.evalueExprEnd(compiler, Register.R1);
                    compiler.addInstructionEnd(new STORE(Register.R1, new RegisterOffset(cpt, Register.SP)));
                    cpt--;
                }
                compiler.addInstructionEnd(new LOAD(new RegisterOffset(0, Register.SP), Register.R1));
                GestionRegistre.verifieNullReferenceEnd(compiler, Register.R1);
                compiler.addInstructionEnd(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));
                //appel de la methode dont -il s'agit
                //identifier.getMethodDefinition().getIndex() /*
                // il faudra qu'on puisse recuperer après le bon index*/
                compiler.addInstructionEnd(new BSR(new RegisterOffset(identifier.getMethodDefinition().getOffSet(), Register.R1)));
                //fin appel de la methode
                compiler.addInstructionEnd(new SUBSP(nbrParam));
                compiler.addInstructionEnd(new LOAD(Register.R0, Register.R1));



            //System.out.println("Il y'a "+ args.getList().size()+" paramètres");
        }
        compiler.addInstructionEnd(new WINT());
    }



    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        Type type=select_expr.verifyExpr(compiler, localEnv, currentClass);
        if(type.isClass()){
            ClassType classType = (ClassType)type;
            EnvironmentExp environmentExp= classType.getDefinition().getMembers();
            while(environmentExp!=null){
                ExpDefinition expDefinition = environmentExp.get(identifier.getName());
                if (expDefinition != null) {
                    if(expDefinition.isField()){
                        FieldDefinition fieldDefinition = (FieldDefinition)expDefinition;
                        if(currentClass==null){
                            if(fieldDefinition.getVisibility().equals(Visibility.PUBLIC) ){
                                Type type1 = identifier.verifyExpr(compiler,environmentExp,classType.getDefinition());
                                this.setType(type1);
                                return type1;
                            }
                            else{
                                throw new ContextualError(select_expr.prettyPrint() + "le fields demander est privé. on ne peut pas y acceder" + identifier.prettyPrint(),getLocation());
                            }
                        }
                        else{
                            if(fieldDefinition.getVisibility().equals(Visibility.PUBLIC) || currentClass.getType().isSubClassOf(classType) ||classType.sameType(currentClass.getType())){
                                Type type1 = identifier.verifyExpr(compiler,environmentExp,classType.getDefinition());
                                this.setType(type1);
                                return type1;
                            }
                            else{
                                throw new ContextualError(select_expr.prettyPrint() + "le fields demander est privé. on ne peut pas y acceder" + identifier.prettyPrint(),getLocation());
                            }
                        }
                    }
                    else if(expDefinition.isMethod()) {
                        MethodDefinition methodDefinition = (MethodDefinition)expDefinition;
                        int n=methodDefinition.getSignature().size();
                        if(n!=args.getList().size()){
                            throw new ContextualError(select_expr.prettyPrint() + "n'a pas le bon nombre d'argument " + identifier.prettyPrint(),getLocation());
                        }
                        Type type2;
                        int i=0;
                        for(AbstractExpr arg : args.getList()) {
                            type2=arg.verifyExpr(compiler, localEnv, classType.getDefinition());
                            if (type2.isClass()) {
                                ClassType classType2 = (ClassType)type2;
                                if(classType2.sameType((ClassType)methodDefinition.getSignature().paramNumber(i)) || classType2.isSubClassOf((ClassType)methodDefinition.getSignature().paramNumber(i))){
                                    i++;
                                }
                                else{
                                    throw new ContextualError(select_expr.prettyPrint() + "l'argument n'est pas du bon type" + identifier.prettyPrint(),getLocation());
                                }
                            }
                            else{
                                if(type2.sameType(methodDefinition.getSignature().paramNumber(i)) || type2.isSubTypeOf(methodDefinition.getSignature().paramNumber(i))){
                                    i++;
                                }
                                else{
                                    throw new ContextualError(select_expr.prettyPrint() + "l'argument n'est pas du bon type" + identifier.prettyPrint(),getLocation());
                                }
                            }
                        }
                        Type type1 = identifier.verifyExpr(compiler,environmentExp,classType.getDefinition());
                        this.setType(type1);
                        return type1;
                    }
                    else{
                        throw new ContextualError(select_expr.prettyPrint() + "n'est pas une méthode ou un field " + identifier.prettyPrint(),getLocation());
                    }
                }
                environmentExp=environmentExp.getParentEnvironment();
            }
            }
            throw new ContextualError(select_expr.prettyPrint() + "n'est pas une classe il ne peut donc avoir d'argument",getLocation());

    }


    @Override
    public void genIfElseCode(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r =  GestionRegistre.obtainFreeRegister();
        boolean rWasNul = false;
        if(r == null){

            compiler.addInstruction(new PUSH(Register.R1));
            r = Register.R1;
            rWasNul = true;
        }
        this.evalueExpr(compiler, r);
        Label E = new Label(thenIns.toString().replace('@', '_')+"if");
        Label fin = new Label(thenIns.toString().replace('@', '_')+"fin_if");
        compiler.addInstruction(new CMP(0, r));
        compiler.addInstruction(new BNE(E));
        elseIns.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(fin));
        compiler.addLabel(E);
        thenIns.codeGenListInst(compiler);
        compiler.addLabel(fin);
        if(!rWasNul){
            r.setInUse(false);
        }else{
            compiler.addInstruction(new POP(Register.R1));
        }

    }
    @Override
    public void genWhileCode(DecacCompiler compiler, Label label){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        boolean rWasNul = false;
        if(r == null){

            compiler.addInstruction(new PUSH(Register.R1));
            r = Register.R1;
            rWasNul = true;
        }
        this.evalueExpr(compiler, r);
        compiler.addInstruction(new CMP(0, r)); //si vrai
        compiler.addInstruction(new BNE(label));
        if(!rWasNul){
            r.setInUse(false);
        }else{
            compiler.addInstruction(new POP(Register.R1));
        }
    }

    @Override
    public void genIfElseCodeMethod(DecacCompiler compiler, ListInst thenIns, ListInst elseIns){

        GPRegister r =  GestionRegistre.obtainFreeRegister();
        boolean rWasNul = false;
        if(r == null){

            compiler.addInstructionEnd(new PUSH(Register.R1));
            r = Register.R1;
            rWasNul = true;
        }
        this.evalueExprEnd(compiler, r);
        Label E = new Label(thenIns.toString().replace('@', '_')+"if");
        Label fin = new Label(thenIns.toString().replace('@', '_')+"fin_if");
        compiler.addInstructionEnd(new CMP(0, r));
        compiler.addInstructionEnd(new BNE(E));
        elseIns.codeGenListInstMethod(compiler);
        compiler.addInstructionEnd(new BRA(fin));
        compiler.addLabelEnd(E);
        thenIns.codeGenListInstMethod(compiler);
        compiler.addLabelEnd(fin);
        if(!rWasNul){
            r.setInUse(false);
        }else{
            compiler.addInstructionEnd(new POP(Register.R1));
        }

    }
    @Override
    public void genWhileCodeMethod(DecacCompiler compiler, Label label){
        GPRegister r =  GestionRegistre.obtainFreeRegister();
        boolean rWasNul = false;
        if(r == null){

            compiler.addInstructionEnd(new PUSH(Register.R1));
            r = Register.R1;
            rWasNul = true;
        }
        this.evalueExprEnd(compiler, r);
        compiler.addInstructionEnd(new CMP(0, r)); //si vrai
        compiler.addInstructionEnd(new BNE(label));
        if(!rWasNul){
            r.setInUse(false);
        }else{
            compiler.addInstructionEnd(new POP(Register.R1));
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        select_expr.decompile(s);
        s.print(".");
        identifier.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        select_expr.iter(f);
        identifier.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        select_expr.prettyPrint(s,prefix,false);
        if(args != null) {
            identifier.prettyPrint(s,prefix,false);
            args.prettyPrint(s,prefix,true);
        }
        else{
            identifier.prettyPrint(s,prefix,true);
        }
    }



}

