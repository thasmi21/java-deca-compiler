package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.codegen.MemoryOperand;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
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
        Type t = type.verifyType(compiler);
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
        GPRegister r = GestionRegistre.obtainFreeRegister();
            //throw new UnsupportedOperationException("Not Yet implemented by us");
        this.varName.getExpDefinition().setOperand(new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1), Register.GB));
        //initialiser l'adresse
        if(initialization instanceof Initialization){
            if(((Initialization) initialization).getExpression() instanceof New){
                New ident = (New)((Initialization) initialization).getExpression();
                //si c'est une classe definition
                ClassDefinition def = ident.getClassDef();
                System.out.println(def);

                int nbrTotFields = def.getNumberOfFields() + def.getSuperClass().getNumberOfFields();
                compiler.addInstruction(new NEW(nbrTotFields+1, r));
                compiler.addInstruction(new STORE(r, varName.getExpDefinition().getOperand()));
                compiler.addInstruction(new BOV(new Label("tas_plein")));
                compiler.addInstruction(new LEA(new RegisterOffset(def.getAdrTableMethode(), Register.GB), Register.R0));
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0, r)));
                compiler.addInstruction(new PUSH(r));
                compiler.addInstruction(new BSR(new Label("init."+def.getType().getName())));
                compiler.addInstruction(new POP(r));
                compiler.addComment("Ok Ok OKOKKKKKKKKKKKKK");
                r.setInUse(false);
                return ;
            }

            ((Initialization) initialization).getExpression().evalueExpr(compiler, r);
            compiler.addInstruction(new STORE(r, varName.getExpDefinition().getOperand()));
            r.setInUse(false); //il faut verifier après que r ne sera jamais nul
        }
    }

    @Override
    public void codeGenDeclVarMethod(DecacCompiler compiler){
        //throw new UnsupportedOperationException("Not Yet implemented by us");
        this.varName.getExpDefinition().setOperand(new RegisterOffset(GestionRegistre.obtainIndiceLBandIncrement(1), Register.LB));
        //initialiser l'adresse
        if(initialization instanceof Initialization){
            //prendre un registre libre;
            //evaluer l'expression et charger dans le registre
            //puis affecter
            GPRegister r = Register.getR(2);
            compiler.addInstructionEnd(new PUSH(r));
            ((Initialization) initialization).getExpression().evalueExprMethod(compiler, r);
            compiler.addInstructionEnd(new STORE(r, varName.getExpDefinition().getOperand()));
            compiler.addInstructionEnd(new POP(r));
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
