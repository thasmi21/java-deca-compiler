package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.STORE;
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

    public AbstractIdentifier getType() {
        return this.type;
    }


    public AbstractInitialization getInitialization() {
        return this.initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
                                 EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        EnvironmentType env = compiler.getEnvironmentType();
        //Vérification du type de base
        Type identType = this.getType().verifyType(compiler);
        // Vérification du type vide
        if (identType.isVoid()) {
            throw new ContextualError("La variable ne peut pas être de type void !", this.getLocation());
        }
        EnvironmentType envType = compiler.getEnvironmentType();

        //Dimension de la variable + du type
        int dimVar = countArrayDimensions(this.varName.getName().getName());
        int dimType = countArrayDimensions(this.type.getName().getName());
        int dimTotal = dimVar + dimType;

        // Construction du nouveau type (en prenant en compte la variable)
        Type baseType = getBaseType(compiler, identType);
        Type arrayType = identType;
        String arrayTypeName = identType.getName().getName();
        for (int i = dimType; i < dimTotal; i++) {
            arrayTypeName += "[]";
            SymbolTable.Symbol arraySymbol = compiler.createSymbol(arrayTypeName);
            arrayType = new ArrayType(arraySymbol,baseType,i+1);
            // Ajouter le type tableau dans l'environnement global s'il n'existe pas déjà
            if (env.defOfType(arraySymbol) == null) {
                compiler.getEnvironmentType().setTypeDefinition(arraySymbol, new TypeDefinition(arrayType, getLocation()));
            }
        }
        if (!(initialization instanceof Initialization && ((Initialization) initialization).getExpression() instanceof ArrayLiteral && ((ArrayLiteral) ((Initialization) initialization).getExpression()).implicitDeclaration())){
            initialization.verifyInitialization(compiler, arrayType, localEnv, currentClass);
        }
        else {
            ((Initialization) initialization).getExpression().setType(arrayType);
            initialization.verifyInitialization(compiler, arrayType, localEnv, currentClass);
        }
        String varNameWoBrackets = withoutBrackets(varName.getName().getName());
        if(dimTotal>0) {
            ArrayDefinition arrayDef;
            if (initialization instanceof Initialization) {
                arrayDef = new ArrayDefinition(arrayType, this.getLocation(), ((Initialization) initialization));
            }
            else{
                arrayDef = new ArrayDefinition(arrayType, this.getLocation(), null);
            }
            try {
                localEnv.declare(this.varName.getName(), arrayDef);
            } catch (EnvironmentExp.DoubleDefException e) {
                throw new ContextualError("La variable " + varName.getName() + " est déjà définie !", this.getLocation());
            }
            this.varName.setName(compiler.createSymbol(varNameWoBrackets));
            this.varName.setDefinition(arrayDef);
            this.varName.setType(arrayType);
        }else {
            VariableDefinition varDef = new VariableDefinition(arrayType, this.getLocation());
            try {
                localEnv.declare(this.varName.getName(), varDef);
            } catch(EnvironmentExp.DoubleDefException e) {
                throw new ContextualError("La variable "+varName.getName()+" est déjà définie !",this.getLocation());
            }
            this.varName.setName(compiler.createSymbol(varNameWoBrackets));
            this.varName.setDefinition(varDef);
            this.varName.setType(arrayType);
        }
    }

    @Override
    public void codeGenDeclVar(DecacCompiler compiler){
        // 1) On se crée un registre de travail
        GPRegister r = GestionRegistre.obtainFreeRegister();
        boolean rWasNull = false;
        if (r == null) {
            rWasNull = true;
            r = Register.getR(2);
            compiler.addInstruction(new PUSH(r));
        }
        // 2) Associer la variable (this.varName) à un emplacement mémoire (offset) dans GB
        //    (chaque variable occupant 1 mot)
        this.varName.getExpDefinition().setOperand(new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1), Register.GB));

        // 3) Si on a une initialisation :
        if (initialization instanceof Initialization) {
            if(((Initialization) initialization).getExpression() instanceof New){
                New ident = (New)((Initialization) initialization).getExpression();
                //si c'est une classe definition
                ClassDefinition def = ident.getClassDef();
                //System.out.println(def);

                int nbrTotFields = def.getNumberOfFields() + def.getSuperClass().getNumberOfFields();
                compiler.addInstruction(new NEW(nbrTotFields+1, r));
                compiler.addInstruction(new STORE(r, varName.getExpDefinition().getOperand()));
                if (!compiler.getN()){
                    compiler.addInstruction(new BOV(new Label("tas_plein")));
                }
                compiler.addInstruction(new LEA(new RegisterOffset(def.getAdrTableMethode(), Register.GB), Register.R0));
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0, r)));
                compiler.addInstruction(new PUSH(r));
                compiler.addInstruction(new BSR(new Label("init."+def.getType().getName())));
                compiler.addInstruction(new POP(r));
                //compiler.addComment("Ok Ok OKOKKKKKKKKKKKKK");
                if(rWasNull){
                    compiler.addInstruction(new POP(r));
                }
                else{
                    r.setInUse(false);
                }

                return ;
            }
            AbstractExpr expr = ((Initialization) initialization).getExpression();

            if (!(expr.getType() instanceof ArrayType)) {
                r = GestionRegistre.obtainFreeRegister();
                rWasNull = false;
                if(r==null){
                    rWasNull = true;
                    r = Register.getR(2);
                    compiler.addInstruction(new PUSH(r));
                }
                expr.evalueExpr(compiler, r);
                compiler.addInstruction(new STORE(r, varName.getExpDefinition().getOperand()));
                if(rWasNull){
                    compiler.addInstruction(new POP(r));
                }
                else{
                    r.setInUse(false);
                }
            }

            // 3.b) Si c’est un tableau => on appelle allocateSubArray
            else {
                // Récupérer l’expression : ArrayLiteral, ArrayNoInit, etc.
                if (expr instanceof ArrayLiteral) {
                    ArrayLiteral tabLiteral = (ArrayLiteral) expr;

                    // (1) Obtenir un registre pour stocker l’adresse de base du tableau
                    GPRegister rBase = GestionRegistre.obtainFreeRegister();
                    boolean rbaseWasNull = false;
                    if (rBase == null) {
                        rbaseWasNull = true;
                        // Choix arbitraire de R(5), dépend de vos conventions
                        rBase = Register.getR(5);
                        compiler.addInstruction(new PUSH(rBase));
                    }

                    // (2) Calculer la taille du tableau “racine”.
                    //     Dans votre 'remplissageRecursif', vous traitez un Array ou ArrayLiteral.
                    //     Ici, tabLiteral.getArray() est l’objet 'Array' interne, d’où on peut tirer la taille.
                    int size = tabLiteral.getArray().getSize();

                    // (3) Allouer le bloc de 'size' mots
                    compiler.addInstruction(new NEW(size, rBase));
                    // (Facultatif) Gérer un éventuel BOV si vous gérez l’overflow du tas :
                    // if (!compiler.getN()) {
                    //    compiler.addInstruction(new BOV(new Label("tas_plein")));
                    // }

                    // (4) Appel récursif pour remplir le bloc :
                    //     `remplissageRecursif(compiler, tabLiteral, rBase);`
                    //     Cette méthode ira parcourir l’Array (ou ArrayLiteral) et remplir chaque sous-élément.
                    remplissageRecursif(compiler, tabLiteral, rBase);

                    // (5) Stocker l’adresse du tableau dans la variable (GB offset)
                    //     varName.getExpDefinition().getOperand() pointe vers l’emplacement de la variable.
                    compiler.addInstruction(new STORE(rBase, varName.getExpDefinition().getOperand()));

                    // (6) Libération / restauration du registre
                    if (rbaseWasNull) {
                        // si on avait “poussé” le registre (car il n’y en avait plus de libre),
                        // on le POP maintenant
                        compiler.addInstruction(new POP(rBase));
                    } else {
                        rBase.setInUse(false);
                    }
                }

                // Cas : c'est un ArrayNoInit
                else if (expr instanceof ArrayNoInit) {
                    ArrayNoInit exprArray = (ArrayNoInit) expr;
                    List<AbstractExpr> dimensions = exprArray.getIndices();

                    // Obtenir un registre libre pour stocker l'adresse du tableau
                    GPRegister rBase = GestionRegistre.obtainFreeRegister();
                    if (rBase == null) {
                        // Si aucun registre libre n'est disponible, on peut par ex. prendre R(2) et le pousser
                        rBase = Register.getR(2);
                        compiler.addInstruction(new PUSH(rBase));
                    }

                    // Appel de la méthode qui alloue tout le tableau (simple ou multidimensionnel)
                    allocateSubArray(compiler, dimensions, rBase);

                    // Stocker l'adresse résultante dans la variable
                    // (varName.getExpDefinition().getOperand() pointe vers la case mémoire de la variable)
                    compiler.addInstruction(
                            new STORE(rBase, varName.getExpDefinition().getOperand())
                    );

                    // Libérer le registre (si vous avez fait un PUSH, POPpez-le, sinon setInUse(false))
                    rBase.setInUse(false);

                    return;
                }
                else {
                    // Autres cas (ex: expr evalueExpr d'un autre type renvoyant un array)
                    GPRegister rVal = GestionRegistre.obtainFreeRegister();
                    boolean rValWasNull = false;
                    if (rVal == null) {
                        rValWasNull = true;
                        rVal = Register.getR(4);
                        compiler.addInstruction(new PUSH(rVal));
                    }

                    expr.evalueExpr(compiler, rVal);
                    // Si c'est pas littéral => on charge la valeur
                    // (Ici, si c'est un tableau, c'est peut-être déjà une adresse, alors on n'a pas besoin
                    //  de LOAD offset(0, rVal) mais tout dépend de la logique globale.
                    //  On suppose qu'un 'new array' renverra l'adresse direct ?)

                    compiler.addInstruction(new STORE(rVal, varName.getExpDefinition().getOperand()));
                    if (rValWasNull) {
                        compiler.addInstruction(new POP(rVal));
                    } else {
                        rVal.setInUse(false);
                    }
                }
            }
        }

        // Libération du registre initial (r)
        if (rWasNull) {
            compiler.addInstruction(new POP(r));
        } else {
            r.setInUse(false);
        }
    }

    /**
     * Alloue récursivement un tableau (potentiellement multidimensionnel)
     * et initialise chaque élément à 0 par défaut.
     *
     * @param compiler     Le compilateur gérant la génération de code.
     * @param dimensions   La liste des dimensions (chacune est une AbstractExpr).
     * @param rBase        Le registre dans lequel l'adresse du tableau est placée.
     */
    public static void allocateSubArray(DecacCompiler compiler,
                                        List<AbstractExpr> dimensions,
                                        GPRegister rBase) {
        // 1) Vérifier s'il reste des dimensions à traiter
        if (dimensions.isEmpty()) {
            return;
        }

        // 2) Récupérer la première dimension : constante ou dynamique ?
        AbstractExpr firstDimExpr = dimensions.get(0);
        boolean isFirstDimLiteral = (firstDimExpr instanceof IntLiteral);
        int firstDimensionSize = 0;
        GPRegister rSize = null;  // Pour les dimensions dynamiques

        // 3) Allouer le tableau principal pour la première dimension
        if (isFirstDimLiteral) {
            // a) Dimension connue à la compilation
            firstDimensionSize = ((IntLiteral) firstDimExpr).getValue();
            compiler.addInstruction(new NEW(firstDimensionSize, rBase));
            // (Facultatif : gérer un BOV si vous gérez le débordement de tas)
        } else {
            // b) Dimension dynamique : on évalue l'expression
            rSize = GestionRegistre.obtainFreeRegister();
            firstDimExpr.evalueExpr(compiler, rSize);
            compiler.addInstruction(new NEW(rSize, rBase));
            // (Facultatif : BOV pour le débordement)
        }

        // 4) Vérifier s'il reste d'autres dimensions (tableau multidimensionnel)
        if (dimensions.size() > 1) {
            // => Il faut allouer des sous-tableaux dans chaque case du tableau
            List<AbstractExpr> subDimensions = dimensions.subList(1, dimensions.size());

            if (isFirstDimLiteral) {
                // a) Boucle "classique" en Java (pour dimension constante)
                for (int i = 0; i < firstDimensionSize; i++) {
                    GPRegister rSub = GestionRegistre.obtainFreeRegister();
                    // Appel récursif pour allouer le sous-tableau
                    allocateSubArray(compiler, subDimensions, rSub);

                    // Stocker l'adresse du sous-tableau dans la case i
                    compiler.addInstruction(new STORE(rSub, new RegisterOffset(i, rBase)));
                    rSub.setInUse(false);
                }
            } else {
                // b) Dimension dynamique => générer une boucle assembleur
                Label loopStart = new Label("alloc_subarray_start");
                Label loopEnd   = new Label("alloc_subarray_end");
                GPRegister rLimit = rSize;  // La taille du tableau
                GPRegister rCounter = GestionRegistre.obtainFreeRegister();

                // Initialiser le compteur à 0
                compiler.addInstruction(new LOAD(new ImmediateInteger(0), rCounter));
                compiler.addLabel(loopStart);

                // Comparer rCounter < rLimit
                compiler.addInstruction(new CMP(rLimit, rCounter));
                compiler.addInstruction(new BGE(loopEnd)); // sort de la boucle si >=

                // Allouer le sous-tableau
                GPRegister rSub = GestionRegistre.obtainFreeRegister();
                allocateSubArray(compiler, subDimensions, rSub);

                // Stocker le sous-tableau dans le tableau à l'indice rCounter
                // (accès indirect si votre back-end le gère : new RegisterOffset(0, rBase, rCounter))
                compiler.addInstruction(new STORE(rSub, new RegisterIndirect(rBase, rCounter)));
                rSub.setInUse(false);

                // Incrémenter rCounter
                compiler.addInstruction(new ADD(new ImmediateInteger(1), rCounter));
                // Retour en début de boucle
                compiler.addInstruction(new BRA(loopStart));

                // Fin de boucle
                compiler.addLabel(loopEnd);
                rCounter.setInUse(false);
            }
        } else {
            // 5) S'il n'y a plus qu'une dimension, on initialise les éléments à 0
            GPRegister rZero = GestionRegistre.obtainFreeRegister();
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), rZero));

            if (isFirstDimLiteral) {
                // a) Taille constante => simple boucle "for"
                for (int offset = 0; offset < firstDimensionSize; offset++) {
                    compiler.addInstruction(new STORE(rZero, new RegisterOffset(offset, rBase)));
                }
            } else {
                // b) Taille dynamique => boucle assembleur
                Label initLoopStart = new Label("init_loop_start");
                Label initLoopEnd   = new Label("init_loop_end");
                GPRegister rLimit   = rSize;
                GPRegister rCounter = GestionRegistre.obtainFreeRegister();

                compiler.addInstruction(new LOAD(new ImmediateInteger(0), rCounter));
                compiler.addLabel(initLoopStart);

                // Condition : rCounter < rLimit ?
                compiler.addInstruction(new CMP(rLimit, rCounter));
                compiler.addInstruction(new BGE(initLoopEnd));

                // Initialiser la case [rCounter] à 0
                compiler.addInstruction(new STORE(rZero, new RegisterIndirect(rBase, rCounter)));

                // Incrémenter le compteur
                compiler.addInstruction(new ADD(new ImmediateInteger(1), rCounter));
                compiler.addInstruction(new BRA(initLoopStart));

                // Fin de boucle
                compiler.addLabel(initLoopEnd);
                rCounter.setInUse(false);
            }
            rZero.setInUse(false);
        }

        // 6) Libérer le registre ayant servi pour la taille dynamique s'il existe
        if (rSize != null) {
            rSize.setInUse(false);
        }
    }




    /**
     * Remplit récursivement un bloc mémoire alloué en partant d'un Array ou ArrayLiteral.
     * @param element Peut être un ArrayLiteral, un Array, un Integer, ou null.
     */
    private void remplissageRecursif(
            DecacCompiler compiler,
            AbstractExpr element,
            GPRegister baseReg
    ) {
        // 1) Si on reçoit un ArrayLiteral, on récupère le Array réel :
        if (element instanceof ArrayLiteral) {
            element = ((ArrayLiteral) element).getArray();
        }
        // 2) Si c'est un Array, on parcourt ses sous-éléments :
        if (element instanceof Array) {
            Array array = (Array) element;
            for (int i = 0; i < array.getSize(); i++) {
                AbstractExpr coeffCourant = array.getCoefficients(i);
                // Sous-élément = sous-tableau ?
                if (coeffCourant instanceof Array
                        || coeffCourant instanceof ArrayLiteral) {
                    // a) Calcul de la taille du sous-tableau :
                    int subSize = getArraySize(coeffCourant);
                    // b) Allouer un bloc pour ce sous-tableau
                    GPRegister subReg = GestionRegistre.obtainFreeRegister();
                    boolean subRegWasNul = false;
                    if (subReg == null) {
                        subRegWasNul = true;
                        subReg = Register.getR(8);
                        compiler.addInstruction(new PUSH(subReg));
                    }
                    compiler.addInstruction(new NEW(subSize, subReg));
                    // c) Stocker la référence dans baseReg[i]
                    compiler.addInstruction(
                            new STORE(subReg, new RegisterOffset(i, baseReg))
                    );

                    // d) Remplir récursivement
                    remplissageRecursif(compiler, coeffCourant, subReg);
                    if(subRegWasNul){
                        compiler.addInstruction(new POP(subReg));
                    }else{
                        subReg.setInUse(false);
                    }
                }
                else if (coeffCourant == null) {
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    boolean rWasNull = false;
                    if(r==null){
                        rWasNull = true;
                        r = Register.getR(4);
                        compiler.addInstruction(new PUSH(r));
                    }
                    // Initialisation suivant le type
                    if(((Array) element).getType().isInt() || ((Array) element).getType().isBoolean() ) {
                        compiler.addInstruction(new LOAD(new ImmediateInteger(0), r));
                    }
                    else if(((Array) element).getType().isFloat()){
                        compiler.addInstruction(new LOAD(new ImmediateFloat(0.0F), r));
                    }
                    else {
                        compiler.addInstruction(new LOAD(new NullOperand(), r));
                    }
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    if(rWasNull){
                        compiler.addInstruction(new POP(r));
                    }else{
                        r.setInUse(false);
                    }
                }
                else if (coeffCourant instanceof IntLiteral) {
                    int val = ((IntLiteral) coeffCourant).getValue();
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    boolean rWasNull = false;
                    if(r==null){
                        rWasNull = true;
                        r = Register.getR(4);
                        compiler.addInstruction(new PUSH(r));
                    }
                    compiler.addInstruction(new LOAD(new ImmediateInteger(val), r));
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    if(rWasNull){
                        compiler.addInstruction(new POP(r));
                    }else{
                        r.setInUse(false);
                    }
                }
                else if(coeffCourant instanceof FloatLiteral) {
                    float val = ((FloatLiteral) coeffCourant).getValue();
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    boolean rWasNull = false;
                    if(r==null){
                        rWasNull = true;
                        r = Register.getR(4);
                        compiler.addInstruction(new PUSH(r));
                    }
                    compiler.addInstruction(new LOAD(new ImmediateFloat(val), r));
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    if(rWasNull){
                        compiler.addInstruction(new POP(r));
                    }else{
                        r.setInUse(false);
                    }
                }
                else if(coeffCourant instanceof BooleanLiteral) {
                    boolean val = ((BooleanLiteral) coeffCourant).getValue();
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    boolean rWasNull = false;
                    if(r==null){
                        rWasNull = true;
                        r = Register.getR(4);
                        compiler.addInstruction(new PUSH(r));
                    }
                    if(val){
                        compiler.addInstruction(new LOAD(new ImmediateInteger(1), r));
                    }
                    else{
                        compiler.addInstruction(new LOAD(new ImmediateInteger(0), r));

                    }
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    if(rWasNull){
                        compiler.addInstruction(new POP(r));
                    }else{
                        r.setInUse(false);
                    }
                }
                else {
                    throw new IllegalArgumentException("Pas encore implémenté les objets de classe");
                }
            }
        }
        else {
            throw new IllegalArgumentException("La méthode ne peut remplir qu'un Array ou ArrayLiteral.");
        }
    }
    private int getArraySize(Object element) {
        if (element instanceof ArrayLiteral) {
            return ((ArrayLiteral) element).getArray().getSize();
        } else if (element instanceof Array) {
            return ((Array) element).getSize();
        } else {
            // Si jamais on passe ici sur un scalaire,
            // on peut estimer qu'on a besoin d'une case (1).
            // Mais en principe, on ne devrait pas allouer un bloc
            // pour un scalaire. Donc, on pourrait lever une exception.
            return 1;
        }
    }

    // méthode qui récupère le type de base, par ex(int[][][] -> int)
    public static Type getBaseType(DecacCompiler compiler,Type type) throws ContextualError {
        String typeName = type.getName().getName();
        String baseTypeName = extractBaseType(typeName); // Enlève les `[]`
        SymbolTable.Symbol baseSymbol = compiler.createSymbol(baseTypeName);
        TypeDefinition baseTypeDef = compiler.getEnvironmentType().defOfType(baseSymbol);
        return baseTypeDef.getType();
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

    public static int countArrayDimensions(String fullName) {
        int count = 0;
        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i) == '[') {
                count++;
            }
        }
        return count; // Chaque "[" correspond à une dimension
    }

        public static String extractBaseType(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }

    public String withoutBrackets(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
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
            if(((Initialization) initialization).getExpression() instanceof New){
                New ident = (New)((Initialization) initialization).getExpression();
                //si c'est une classe definition
                ClassDefinition def = ident.getClassDef();
                //System.out.println(def);

                int nbrTotFields = def.getNumberOfFields() + def.getSuperClass().getNumberOfFields();
                compiler.addInstructionEnd(new NEW(nbrTotFields+1, r));
                compiler.addInstructionEnd(new STORE(r, varName.getExpDefinition().getOperand()));
                if (!compiler.getN()){
                    compiler.addInstructionEnd(new BOV(new Label("tas_plein")));
                }
                compiler.addInstructionEnd(new LEA(new RegisterOffset(def.getAdrTableMethode(), Register.GB), Register.R0));
                compiler.addInstructionEnd(new STORE(Register.R0, new RegisterOffset(0, r)));
                compiler.addInstructionEnd(new PUSH(r));
                compiler.addInstructionEnd(new BSR(new Label("init."+def.getType().getName())));
                compiler.addInstructionEnd(new POP(r));
                compiler.addInstructionEnd(new POP(r));
                //compiler.addComment("Ok Ok OKOKKKKKKKKKKKKK");
                //r.setInUse(false);
                return ;
            }
            //compiler.addInstructionEnd(new PUSH(r));
            ((Initialization) initialization).getExpression().evalueExprEnd(compiler, r);
            compiler.addInstructionEnd(new STORE(r, varName.getExpDefinition().getOperand()));
            compiler.addInstructionEnd(new POP(r));
        }
    }
}