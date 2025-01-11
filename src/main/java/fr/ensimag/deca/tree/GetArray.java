package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GetArray extends AbstractLValue {
    private AbstractIdentifier identifier;
    private List<IntLiteral> listIndex;

    public GetArray(AbstractIdentifier identifier, List<DecaParser.ExprContext> ListIndex) {
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

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Symbol symbolIdentifier = identifier.getName();
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
        return typeVoulu;
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        identifier.prettyPrint(s, prefix, true);
        //index.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }
}
