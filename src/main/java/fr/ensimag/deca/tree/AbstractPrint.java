package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Print statement (print, println, ...).
 *
 * @author gl06
 * @date 01/01/2025
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();
    
    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected Type verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        for (AbstractExpr expressions : arguments.getList()){
            Type typeExpr = expressions.verifyExpr(compiler, localEnv, currentClass);
            if(!(typeExpr.isFloat() || typeExpr.isInt() || typeExpr.isString() || typeExpr.isArray() )) {
                throw new ContextualError("A l'intérieur du print, il faut un int, float ou string ! Type reçu : "+ typeExpr, getLocation());
            }
        }
        return compiler.environmentType.VOID;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        if(getPrintHex()){
            for (AbstractExpr a : getArguments().getList()) {
                a.codeGenPrintX(compiler);
            }
        }
        else{
            for (AbstractExpr a : getArguments().getList()) {
                a.codeGenPrint(compiler);
            }
        }
    }
    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        for (AbstractExpr a : getArguments().getList()) {
            a.codeGenPrintMethod(compiler);
        }
    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("print" + getSuffix() +"(");
        int i = 0;
        for (AbstractExpr a : arguments.getList()) {
            i+=1;
            a.decompile(s);
            if(i!=arguments.getList().size()) {s.print(",");}
        }
        s.println(");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
