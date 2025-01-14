package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GetArray extends Identifier {
    private AbstractIdentifier identifier;
    private List<IntLiteral> listIndex;

    public GetArray(AbstractIdentifier identifier, List<DecaParser.ExprContext> ListIndex) {
        super(identifier.getName());
        this.identifier = identifier;
        List<IntLiteral> liste= new ArrayList<>();
        for(DecaParser.ExprContext coefficient : ListIndex) {
            if(coefficient.tree instanceof IntLiteral) {
                liste.add((IntLiteral)coefficient.tree);
            }
            else{
                throw new IllegalArgumentException("Les indices doivent être des entiers !");
            }
        }
        this.listIndex = liste;
    }

    public List<IntLiteral> getListIndex() {
        return listIndex;
    }

    public AbstractIdentifier getIdentifier() {
        return identifier;
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Symbol symbolIdentifier = identifier.getName();
        identifier.verifyExpr(compiler, localEnv, currentClass);
        ExpDefinition defIdentifier = localEnv.get(symbolIdentifier);
        if(defIdentifier == null){
            throw new ContextualError(symbolIdentifier + "non déclaré", getLocation());
        }
        Type type = defIdentifier.getType();
        if(!(type instanceof ArrayType)) {
            throw new ContextualError(symbolIdentifier + "n'est pas un tableau !", getLocation());
        }
        EnvironmentType envType = compiler.getEnvironmentType();
        Type typeCourant = type;
        Symbol symboleTypeCoeff = null;
        for(int i = 1; i< listIndex.size()+1; i++) {
            symboleTypeCoeff = compiler.createSymbol(type.getName().getName().substring(0, type.getName().getName().length() - 2*i));
        }
        Type typeVoulu = envType.defOfType(symboleTypeCoeff).getType();
        this.setType(compiler.environmentType.INT);
        Definition varDef = new VariableDefinition(typeVoulu,getLocation());
        String name = symbolIdentifier.getName();
        for(int i = 0; i< listIndex.size(); i++) {
            name= name+"["+listIndex.get(i).getValue()+"]";
        }
        Symbol varSymbol = compiler.createSymbol(name);
        this.setName(varSymbol);
        this.setDefinition(varDef);
        return typeVoulu;
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        identifier.prettyPrint(s, prefix, true);
        listIndex.get(0).prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {

    }

    @Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register) {
        // Récupération de l'adresse de base de l'identifiant
        DAddr idAdresse = identifier.getExpDefinition().getOperand();
        RegisterOffset identifierOffset = (RegisterOffset) idAdresse;
        Register baseRegister = identifierOffset.getRegister();
        int baseOffset = identifierOffset.getOffset();
        // Vérification si l'indice est une constante ou une expression dynamique
        if (listIndex.get(0) instanceof IntLiteral) {
            int indice = listIndex.get(0).getValue();
            compiler.addInstruction(new LEA(new RegisterOffset(baseOffset + indice, baseRegister), register));
        } else {
            GPRegister tempRegister = GestionRegistre.obtainFreeRegister();
            listIndex.get(0).evalueExpr(compiler, tempRegister);
            compiler.addInstruction(new LEA(new RegisterOffset(baseOffset, baseRegister), register));
            compiler.addInstruction(new ADD(tempRegister, register));
            tempRegister.setInUse(false);
        }
    }


    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        int indice = listIndex.get(0).getValue();
        ArrayDefinition arrayDef = (ArrayDefinition) identifier.getExpDefinition();
        int size = arrayDef.getArray().getSize();
        if(!(0<=indice && indice < size)) {
            throw new RuntimeException("Index out of bounds");
        }
        DAddr idAdresse=  identifier.getExpDefinition().getOperand();
        RegisterOffset identifierOffset = (RegisterOffset) idAdresse;

        Register baseRegister = identifierOffset.getRegister();
        int baseOffset = identifierOffset.getOffset();
        RegisterOffset effectiveAdress = new RegisterOffset(baseOffset+indice,baseRegister);

        compiler.addInstruction(new LOAD(effectiveAdress,Register.R1));
        if (listIndex.get(0) instanceof IntLiteral) {
            compiler.addInstruction(new WINT());
        } else if (listIndex.get(0).getType().isFloat()) {
            compiler.addInstruction(new WFLOAT());
        }
        r1.setInUse(false);
    }
}
