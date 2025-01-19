package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import org.apache.commons.lang.Validate;

/**
 * Full if/else if/else statement.
 *
 * @author gl06
 * @date 01/01/2025
 */
public class IfThenElse extends AbstractInst {
    
    private final AbstractExpr condition; 
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public void setElseBranch(ListInst elseBranch) {
        this.elseBranch = elseBranch;
    }


    @Override
    protected Type verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        this.condition.verifyCondition(compiler, localEnv, currentClass);
        Type typeif=this.thenBranch.verifyListInst(compiler, localEnv, currentClass, null);
        Type typeelse=this.elseBranch.verifyListInst(compiler, localEnv, currentClass, null);
        if(typeif.sameType(returnType) && typeelse.sameType(returnType)) {
            return typeelse;
        }
        if(returnType==null || typeif.sameType(typeelse)) {
            return typeelse;
        }
        else if((typeelse.sameType(returnType) && typeif.isVoid())|| (typeif.sameType(returnType) && typeelse.isVoid()) || (typeelse.isVoid() && typeif.isVoid())) {
            return compiler.getEnvironmentType().VOID;
        }
        else{
            throw new ContextualError("le type renvoyer dans le if n'est pas le meme que dans le else",getLocation());
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
            //on recupere un registre pour y mettre la valeur de la condition

        condition.genIfElseCode(compiler, thenBranch, elseBranch);

    }
    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {

        condition.genIfElseCodeMethod(compiler, thenBranch, elseBranch);

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if(");
        condition.decompile(s);
        s.println("){");
        thenBranch.decompile(s);
        s.println("}");
        s.print("else{");
        elseBranch.decompile(s);
        s.println("}");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
}
