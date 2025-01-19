package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;

public class Return extends AbstractInst{

    private AbstractExpr expr;
    private String etiqFin;

    public String getEtiqFin() {
        return etiqFin;
    }

    public void setEtiqFin(String etiqFin) {
        this.etiqFin = etiqFin;
    }

    public Return(AbstractExpr expr) {
        this.expr = expr;
    }


    public Type verifyInst(DecacCompiler compiler,
                                    EnvironmentExp localEnv, ClassDefinition currentClass,Type type)
            throws ContextualError{
        this.setEtiqFin("fin."+GestionRegistre.classFinCour+"."+GestionRegistre.etiqFinCour);
        if(type==null){
            return expr.verifyExpr(compiler, localEnv, currentClass);
        }
        else{
            Type t =expr.verifyExpr(compiler,localEnv,currentClass);
            if (!t.sameType(type)) {
                throw new ContextualError("le return n'est pas du bon type",getLocation());
            }
            return t;
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {

    }
    public void codeGenInstReturn(DecacCompiler compiler, String nom_label_fin){
        GPRegister r = GestionRegistre.obtainFreeRegister();
        if(r != null){
            expr.evalueExprEnd(compiler, r);
            compiler.addInstructionEnd(new LOAD(r, Register.R0));
        }else{
            r=Register.getR(2);
            compiler.addInstructionEnd(new PUSH(r));
            expr.evalueExprEnd(compiler, r);
            compiler.addInstructionEnd(new LOAD(r, Register.R0));
            compiler.addInstructionEnd(new POP(r));
        }
        compiler.addInstructionEnd(new BRA(new Label(nom_label_fin)));
    }

    @Override
    protected void codeGenInstMethod(DecacCompiler compiler) {
        this.codeGenInstReturn(compiler, this.getEtiqFin());

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expr.decompile(s);
        s.println(";");
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    public void iterChildren(TreeFunction f) {
        expr.iter(f);
    }
}
