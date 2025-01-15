package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;

public class Dot extends AbstractLValue {

    private AbstractExpr select_expr;
    private AbstractIdentifier identifier;
    private ListExpr args;

    public Dot(AbstractExpr select_expr, AbstractIdentifier identifier) {
        this.select_expr = select_expr;
        this.identifier = identifier;
        this.args = null;
    }

    public Dot(AbstractExpr select_expr,AbstractIdentifier identifier,ListExpr args) {
        this.select_expr = select_expr;
        this.identifier = identifier;
        this.args = args;
    }



    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        Type type=select_expr.verifyExpr(compiler, localEnv, currentClass);
        if(type.isClass()){
            ClassType classType = (ClassType)type;
            while(classType.getDefinition().getSuperClass()!=null){
                EnvironmentExp environmentExp= classType.getDefinition().getMembers();
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
                            if(type2.sameType(methodDefinition.getSignature().paramNumber(i))){
                                i++;
                            }
                            else{
                                throw new ContextualError(select_expr.prettyPrint() + "l'argument n'est pas du bon type" + identifier.prettyPrint(),getLocation());
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
                classType=classType.getDefinition().getSuperClass().getType();
            }
            }
            throw new ContextualError(select_expr.prettyPrint() + "n'est pas une classe il ne peut donc avoir d'argument",getLocation());

    }

    @Override
    public void decompile(IndentPrintStream s) {
        select_expr.decompile(s);
        s.print(".");
        identifier.decompile(s);
        s.println(";");
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

