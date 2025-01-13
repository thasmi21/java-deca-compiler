package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.commons.lang.Validate;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class DeclVar extends AbstractDeclVar {

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type t = this.type.verifyType(compiler);
        VariableDefinition varDef = new VariableDefinition(t, this.getLocation());
        if (t.isVoid()) {
              throw new ContextualError("La variable ne peut pas être de type void !", this.getLocation());
        }
        initialization.verifyInitialization(compiler,t, localEnv, currentClass);
        try {
            localEnv.declare(this.varName.getName(), varDef);
        } catch(EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("La variable "+varName.getName()+" est déjà définie !",this.getLocation());
        }
        this.varName.setDefinition(varDef);
        this.varName.setType(t);
    }

    @Override
    public void codeGenDeclVar(DecacCompiler compiler){
            //throw new UnsupportedOperationException("Not Yet implemented by us");
        this.varName.getExpDefinition().setOperand(new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1), Register.GB));
        //initialiser l'adresse
        if(initialization instanceof Initialization){
            //prendre un registre libre;
            //evaluer l'expression et charger dans le registre
            //puis affecter
            GPRegister r = GestionRegistre.obtainFreeRegister();
            ((Initialization) initialization).getExpression().evalueExpr(compiler, r);
            //Register r = GenDeclVar.evalueExpre(compiler,((Initialization) initialization).getExpression());
            compiler.addInstruction(new STORE(r, varName.getExpDefinition().getOperand()));
            r.setInUse(false); //il faut verifier après que r ne sera jamais nul
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        varName.decompile(s);
        if (initialization instanceof Initialization) {
            s.print("= ");
        }
        initialization.decompile(s);
        s.println(";");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
}
