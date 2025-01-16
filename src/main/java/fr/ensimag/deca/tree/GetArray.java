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
        List<IntLiteral> liste = new ArrayList<>();
        for (DecaParser.ExprContext coefficient : ListIndex) {
            if (coefficient.tree instanceof IntLiteral) {
                liste.add((IntLiteral) coefficient.tree);
            } else {
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
        if (defIdentifier == null) {
            throw new ContextualError(symbolIdentifier + "non déclaré", getLocation());
        }
        Type type = defIdentifier.getType();
        if (!(type instanceof ArrayType)) {
            throw new ContextualError(symbolIdentifier + "n'est pas un tableau !", getLocation());
        }
        EnvironmentType envType = compiler.getEnvironmentType();
        Type typeCourant = type;
        Symbol symboleTypeCoeff = null;
        for (int i = 1; i < listIndex.size() + 1; i++) {
            symboleTypeCoeff = compiler.createSymbol(type.getName().getName().substring(0, type.getName().getName().length() - 2 * i));
        }
        Type typeVoulu = envType.defOfType(symboleTypeCoeff).getType();
        this.setType(compiler.environmentType.INT);
        Definition varDef = new VariableDefinition(typeVoulu, getLocation());
        String name = symbolIdentifier.getName();
        for (int i = 0; i < listIndex.size(); i++) {
            name = name + "[" + listIndex.get(i).getValue() + "]";
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
        // 1) On se réserve un registre temporaire (rBase) pour tout calcul
        GPRegister rBase = GestionRegistre.obtainFreeRegister();

        // 2) Récupère l’opérande où se trouve l’adresse de x (tableau)
        DAddr xAddr = this.identifier.getExpDefinition().getOperand();

        // 3) Charger &x dans rBase
        compiler.addInstruction(new LOAD(xAddr, rBase));
        // => rBase = &x

        // 4) Parcours des dimensions (indices)
        int nbDims = listIndex.size();
        for (int dim = 0; dim < nbDims; dim++) {
            // a) Vérification si l’indice est un littéral int (ou on gère autrement)
            //    Ici, on suppose seulement des int littéraux pour l’exemple :
            if (!(listIndex.get(dim) instanceof IntLiteral)) {
                throw new UnsupportedOperationException(
                        "Exemple : indices non-littéraux non gérés ici"
                );
            }

            int offset = ((IntLiteral) listIndex.get(dim)).getValue();
            // => On veut faire LEA(rBase + offset) => rBase
            compiler.addInstruction(
                    new LEA(new RegisterOffset(offset, rBase), rBase)
            );

            // b) LOAD [rBase + 0] => rBase
            compiler.addInstruction(
                    new LOAD(new RegisterOffset(0, rBase), rBase)
            );
        }

        // À la fin, rBase contient la valeur x[..][..] (si le tableau est d'entiers)
        // ou un float, etc.

        // 5) On place le résultat dans R1
        //    (si votre convention d’impression requiert R1)
        compiler.addInstruction(new LOAD(rBase, register));

    }

    public DAddr codeGenAddressOf(DecacCompiler compiler) {
        // 1) On récupère l’opérande de base (adresse du tableau x)
        DAddr xAddr = this.identifier.getExpDefinition().getOperand();

        // 2) On alloue un registre de travail
        GPRegister rBase = GestionRegistre.obtainFreeRegister();
        // Charger &x dans rBase
        compiler.addInstruction(new LOAD(xAddr, rBase));
        // => rBase = adresse du bloc racine de x

        int nbDims = this.listIndex.size();
        for (int dim = 0; dim < nbDims; dim++) {
            // a) Récupérer l’indice
            AbstractExpr indexExpr = listIndex.get(dim);

            // Hypothèse : indices constants (IntLiteral).
            // Sinon, il faut un mécanisme plus élaboré pour additionner un pointeur & un entier.
            if (!(indexExpr instanceof IntLiteral)) {
                throw new UnsupportedOperationException(
                        "codeGenAddressOf ne gère ici que des indices constants."
                );
            }
            int offset = ((IntLiteral) indexExpr).getValue();

            // b) LEA ou addition permettant rBase = rBase + offset
            //    Ici on illustre un pseudo LEA si votre ISA ne tolère pas directement ADD (adresse+entier).
            compiler.addInstruction(
                    new LEA(new RegisterOffset(offset, rBase), rBase)
            );

            // c) Si on n’est pas au dernier niveau, on charge le sous-bloc
            boolean isLastDim = (dim == nbDims - 1);
            if (!isLastDim) {
                // => rBase = contenu de [rBase] (pointeur vers le sous-bloc)
                compiler.addInstruction(
                        new LOAD(new RegisterOffset(0, rBase), rBase)
                );
            }
            // Sinon, on ne fait rien : on garde l’adresse de la case finale.
        }

        // À ce stade, rBase pointe sur la *cellule* x[i1][i2]...[iN].
        // On renvoie un DAddr qui décrit "rBase + offset 0"
        DAddr finalAddr = new RegisterOffset(0, rBase);

        // L'appelant pourra par exemple faire LOAD(finalAddr, R1) pour lire la valeur.

        // NB: on NE libère pas encore rBase.setInUse(false) ici
        //     si l'appelant veut l'utiliser juste après.
        //     À voir selon votre gestion des registres.

        return finalAddr;
    }



    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        // 1) On se réserve un registre temporaire (rBase) pour tout calcul
        GPRegister rBase = GestionRegistre.obtainFreeRegister();

        // 2) Récupère l’opérande où se trouve l’adresse de x (tableau)
        DAddr xAddr = this.identifier.getExpDefinition().getOperand();

        // 3) Charger &x dans rBase
        compiler.addInstruction(new LOAD(xAddr, rBase));
        // => rBase = &x

        // 4) Parcours des dimensions (indices)
        int nbDims = listIndex.size();
        for (int dim = 0; dim < nbDims; dim++) {
            // a) Vérification si l’indice est un littéral int (ou on gère autrement)
            //    Ici, on suppose seulement des int littéraux pour l’exemple :
            if (!(listIndex.get(dim) instanceof IntLiteral)) {
                throw new UnsupportedOperationException(
                        "Exemple : indices non-littéraux non gérés ici"
                );
            }

            int offset = ((IntLiteral) listIndex.get(dim)).getValue();
            // => On veut faire LEA(rBase + offset) => rBase
            compiler.addInstruction(
                    new LEA(new RegisterOffset(offset, rBase), rBase)
            );

            // b) LOAD [rBase + 0] => rBase
            compiler.addInstruction(
                    new LOAD(new RegisterOffset(0, rBase), rBase)
            );
        }

        // À la fin, rBase contient la valeur x[..][..] (si le tableau est d'entiers)
        // ou un float, etc.

        // 5) On place le résultat dans R1
        //    (si votre convention d’impression requiert R1)
        compiler.addInstruction(new LOAD(rBase, Register.R1));

        // 6) Impression en fonction du type
        if (this.getType().isInt()) {
            // WINT lira R1
            compiler.addInstruction(new WINT());
        } else if (this.getType().isFloat()) {
            // WFLOAT lira R1
            compiler.addInstruction(new WFLOAT());
        }

        // 7) Libération du registre temporaire
        rBase.setInUse(false);
    }
}