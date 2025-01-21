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

public class GetArray extends AbstractLValue {
    private AbstractIdentifier identifier;
    private List<AbstractExpr> listIndex;

    public GetArray(AbstractIdentifier identifier, List<DecaParser.ExprContext> ListIndex) {
        //super(identifier.getName());
        this.identifier = identifier;
        List<AbstractExpr> liste = new ArrayList<>();
        for (DecaParser.ExprContext coefficient : ListIndex) {
            liste.add(coefficient.tree);
        }
        this.listIndex = liste;
    }

    public List<AbstractExpr> getListIndex() {
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
        if (defIdentifier == null) {
            throw new ContextualError(symbolIdentifier + "non déclaré", getLocation());
        }
        Type type = defIdentifier.getType();
        if (!(type instanceof ArrayType)) {
            throw new ContextualError(symbolIdentifier + "n'est pas un tableau !", getLocation());
        }
        EnvironmentType envType = compiler.getEnvironmentType();
        Symbol symboleTypeCoeff = compiler.createSymbol(type.getName().getName().substring(0, type.getName().getName().length() - (2 * (listIndex.size()))));
        Type typeVoulu = envType.defOfType(symboleTypeCoeff).getType();
        for (AbstractExpr indice : listIndex){
            indice.verifyExpr(compiler, localEnv, currentClass);
            if(!indice.getType().isInt()){
                throw new ContextualError( " Au moins un indice n'est pas un entier", getLocation());
            }
        }
        this.setType(typeVoulu);
        return typeVoulu;
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

//    @Override
//    protected String prettyPrintNode(){
//        return "GetArray";
//    }

//    @Override
//    protected void prettyPrintType(PrintStream s, String prefix) {
//        Type t = getType();
//        if (t != null) {
//            s.print(prefix);
//            s.print("type: ");
//            s.print(t.getName());
//            s.println();
//        }
//    }

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
        // 1) On se réserve un registre temporaire (rBase) pour tout calcul
        GPRegister rBase = GestionRegistre.obtainFreeRegister();
        DAddr xAddr = this.identifier.getExpDefinition().getOperand();
        compiler.addInstruction(new LOAD(xAddr, rBase));
        int nbDims = listIndex.size();
        for (int dim = 0; dim < nbDims; dim++) {
            // a) Vérification si l’indice est un IntLiteral (ou on gère autrement)
            if (!(listIndex.get(dim) instanceof IntLiteral)) {
                GPRegister rIndex = GestionRegistre.obtainFreeRegister();
                listIndex.get(dim).evalueExpr(compiler, rIndex);
                compiler.addInstruction(new LEA(new RegisterIndirect(rBase, rIndex), rBase));
                compiler.addInstruction(new LOAD(new RegisterOffset(0, rBase), rBase));
                rIndex.setInUse(false);
            }
            else{
                int offset = ((IntLiteral) listIndex.get(dim)).getValue();
                compiler.addInstruction(new LEA(new RegisterOffset(offset, rBase), rBase));
                compiler.addInstruction(new LOAD(new RegisterOffset(0, rBase), rBase));
            }
        }
        compiler.addInstruction(new LOAD(rBase, register));
    }

    public DAddr codeGenAddressOf(DecacCompiler compiler) {
        DAddr xAddr = this.identifier.getExpDefinition().getOperand();
        GPRegister rBase = GestionRegistre.obtainFreeRegister();
        compiler.addInstruction(new LOAD(xAddr, rBase));
        int nbDims = this.listIndex.size();
        for (int dim = 0; dim < nbDims; dim++) {
            // a) Récupérer l’indice
            AbstractExpr indexExpr = listIndex.get(dim);
            if (!(indexExpr instanceof IntLiteral)) {
                GPRegister rIndex = GestionRegistre.obtainFreeRegister();
                listIndex.get(dim).evalueExpr(compiler, rIndex);
                compiler.addInstruction(new LEA(new RegisterIndirect(rBase, rIndex), rBase));
                //compiler.addInstruction(new LOAD(new RegisterOffset(0, rBase), rBase));
                rIndex.setInUse(false);
            }
            else {
                int offset = ((IntLiteral) indexExpr).getValue();
                compiler.addInstruction(new LEA(new RegisterOffset(offset, rBase), rBase));
            }
            boolean isLastDim = (dim == nbDims - 1);
            if (!isLastDim) {
                //rBase = contenu de [rBase] (pointeur vers le sous-bloc)
                compiler.addInstruction(new LOAD(new RegisterOffset(0, rBase), rBase));
            }
        }
        DAddr finalAddr = new RegisterOffset(0, rBase);
        return finalAddr;
    }



    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        GPRegister rBase = GestionRegistre.obtainFreeRegister();
        // 2) Récupère l’adresse du tableau
        DAddr xAddr = this.identifier.getExpDefinition().getOperand();
        // 3) Charger &x dans rBase
        compiler.addInstruction(new LOAD(xAddr, rBase));
        // 4) Parcours des indices
        int nbDims = listIndex.size();
        for (int dim = 0; dim < nbDims; dim++) {
            // a) Vérification si l’indice est un littéral int (ou on gère autrement)
            if (!(listIndex.get(dim) instanceof IntLiteral)) {
                // ça fonctionne
                GPRegister rIndex = GestionRegistre.obtainFreeRegister();
                listIndex.get(dim).evalueExpr(compiler, rIndex);
                compiler.addInstruction(new LEA(new RegisterIndirect(rBase, rIndex), rBase));
                compiler.addInstruction(new LOAD(new RegisterOffset(0, rBase), rBase));
                rIndex.setInUse(false);
            }
            else {
                // ça fonctionne
                int offset = ((IntLiteral) listIndex.get(dim)).getValue();
                compiler.addInstruction(new LEA(new RegisterOffset(offset, rBase), rBase));
                compiler.addInstruction(new LOAD(new RegisterOffset(0, rBase), rBase));
            }
        }
        compiler.addInstruction(new LOAD(rBase, Register.R1));
        if (this.getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (this.getType().isFloat()) {
            compiler.addInstruction(new WFLOAT());
        }
        else if(this.getType().isBoolean()){
            // a compléter
        }
        else{
            throw new UnsupportedOperationException("On ne peut print qu'un entier ou un float ou un boolean");
        }
        rBase.setInUse(false);
    }
}