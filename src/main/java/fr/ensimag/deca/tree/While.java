package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.instructions.BRA;
import org.apache.commons.lang.Validate;

/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Label debut = new Label(this.toString().replace('@', '_'));
        Label cond = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstruction(new BRA(cond));
        compiler.addLabel(debut);
        getBody().codeGenListInst(compiler);
        compiler.addLabel(cond);
        condition.genWhileCode(compiler, debut);

    }
    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        Label debut = new Label(this.toString().replace('@', '_'));
        Label cond = new Label(this.toString().replace('@', '_')+"_fin");
        compiler.addInstructionEnd(new BRA(cond));
        compiler.addLabelEnd(debut);
        getBody().codeGenListInstMethod(compiler);
        compiler.addLabelEnd(cond);
        condition.genWhileCodeMethod(compiler, debut);

    }

    @Override
    protected Type verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        this.condition.verifyCondition(compiler,localEnv,currentClass);
        this.condition.verifyInst(compiler, localEnv, currentClass, compiler.environmentType.BOOLEAN);
        Type t=this.body.verifyListInst(compiler,localEnv,currentClass,null);
        return t;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

}
