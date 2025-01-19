package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * String literal
 *
 * @author gl06
 * @date 01/01/2025
 */
public class StringLiteral extends AbstractStringLiteral {

    @Override
    public String getValue() {
        return value;
    }

    private String value;

    public StringLiteral(String value) {
        Validate.notNull(value);
        this.value = value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type stringType = compiler.environmentType.STRING;
        this.setType(stringType);
        return stringType;
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new WSTR(new ImmediateString(value)));
    }
    @Override
    protected void codeGenPrintX(DecacCompiler compiler) {
        compiler.addInstruction(new WSTR(new ImmediateString(value)));
    }

    @Override
    protected void codeGenPrintMethod(DecacCompiler compiler) {
        compiler.addInstructionEnd(new WSTR(new ImmediateString(value)));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        String string = this.value;
        s.print("\"");
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == '\n') {
                s.print("\n");
            }
            else if (c == '\\') {
                s.print("\\\\");
            }
            else if (c == '"') {
                s.print("\\\"");
            }
            else if (c == '\r') {
                s.print("\\r");
            }
            else if (c == '\t') {
                s.print("\\t");
            }
            else {
                s.print(c);
            }
        }
        s.print("\"");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }
    
    @Override
    String prettyPrintNode() {
        return "StringLiteral (" + value + ")";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        return;
        //compiler.addInstruction(new HALT()); // On ne fait rien
        //throw new UnsupportedOperationException("Not finished yet.");
//        GPRegister reg = GestionRegistre.obtainFreeRegister();
//        compiler.addInstruction(new LOAD(new ImmediateString(value)),reg);
    }
}
