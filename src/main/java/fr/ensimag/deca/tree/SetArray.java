package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WINT;

import java.io.PrintStream;

public class SetArray extends AbstractExpr {
    private GetArray getArray;
    private AbstractExpr expr;

    public SetArray(GetArray getArray, AbstractExpr expr) {
        this.getArray = getArray;
        this.expr = expr;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type typeVoulu = getArray.verifyExpr(compiler, localEnv, currentClass);
        Type typeExpr = expr.verifyExpr(compiler, localEnv, currentClass);
        if(!typeVoulu.sameType(typeExpr)) {
            throw new ContextualError("Types incompatibles lors de l'assignation ! Attendu "+ typeVoulu+". Type trouvé : "+typeExpr, getLocation());
        }
        return typeExpr;
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        getArray.prettyPrint(s, prefix, true);
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        expr.evalueExpr(compiler, r1);

        int indice = getArray.getListIndex().get(0).getValue();
        DAddr idAdresse=  getArray.getIdentifier().getExpDefinition().getOperand();
        RegisterOffset identifierOffset = (RegisterOffset) idAdresse;
        Register baseRegister = identifierOffset.getRegister();
        int baseOffset = identifierOffset.getOffset();
        RegisterOffset effectiveAdress = new RegisterOffset(baseOffset+indice,baseRegister);

        compiler.addInstruction(new STORE(r1,effectiveAdress));
    }
}
