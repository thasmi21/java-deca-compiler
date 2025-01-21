package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.NEW;
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
    public void codeGenDeclVar(DecacCompiler compiler) {
        // Initialisation de l'adresse de la variable dans la pile
        this.varName.getExpDefinition().setOperand(
                new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1), Register.GB));
        if (initialization instanceof Initialization) {
            AbstractExpr expr = ((Initialization) initialization).getExpression();

            // Cas simple : pas un tableau
            if (!(expr.getType() instanceof ArrayType)) {
                GPRegister r = GestionRegistre.obtainFreeRegister();
                expr.evalueExpr(compiler, r);
                compiler.addInstruction(new STORE(r, varName.getExpDefinition().getOperand()));
                r.setInUse(false);
            }
            // Cas tableau
            else {
                // Hypothèse : si l'expression est un ArrayLiteral, on gère le tableau multidimensionnel
                if (expr instanceof ArrayLiteral) {
                    ArrayLiteral tabLiteral = (ArrayLiteral) expr;
                    // Allouer le bloc principal (au niveau "racine")
                    int size = tabLiteral.getArray().getSize();
                    GPRegister rBase = GestionRegistre.obtainFreeRegister();
                    compiler.addInstruction(new NEW(size, rBase));
                    // Appel récursif pour remplir
                    remplissageRecursif(compiler, tabLiteral, rBase);
                    // Stocker l'adresse du tableau dans la variable
                    compiler.addInstruction(new STORE(rBase, varName.getExpDefinition().getOperand()));
                    rBase.setInUse(false);
                }
                else if(expr instanceof ArrayNoInit){
                    ArrayNoInit exprArray = (ArrayNoInit) expr;
                    List<AbstractExpr> dimensions = exprArray.getIndices();
                    GPRegister rTotalSize = GestionRegistre.obtainFreeRegister();

                    int taillePremiereDim = 1;
                    if (dimensions.get(0) instanceof IntLiteral) {
                        taillePremiereDim *= ((IntLiteral) dimensions.get(0)).getValue();
                    }
                    else {
                        throw new UnsupportedOperationException("Pas encore implémenté");
                    }
                    GPRegister rBase = GestionRegistre.obtainFreeRegister();
                    compiler.addInstruction(new NEW(taillePremiereDim, rBase));

                    if(dimensions.size() > 1){
                        List<AbstractExpr> sousDimension = dimensions.subList(1, dimensions.size());
                        for (int i = 0; i < taillePremiereDim; i++) {
                            // Allouer chaque sous-tableau
                            GPRegister rSubArray = GestionRegistre.obtainFreeRegister();
                            allocateSubArray(compiler, sousDimension, rSubArray);
                            // Stocker l'adresse du sous-tableau dans le tableau principal
                            compiler.addInstruction(new STORE(rSubArray, new RegisterOffset(i, rBase)));
                            // Libérer le registre temporaire pour le sous-tableau
                            rSubArray.setInUse(false);
                        }
                    }
                    else {
                        GPRegister rTemp = GestionRegistre.obtainFreeRegister();
                        compiler.addInstruction(new LOAD(new ImmediateInteger(0), rTemp));
                        for (int offset = 0; offset < taillePremiereDim; offset++) {
                            compiler.addInstruction(new STORE(rTemp, new RegisterOffset(offset, rBase)));
                        }
                        rTemp.setInUse(false);
                    }
                    // Initialiser chaque case du tableau à une valeur par défaut (exemple : 0 pour les int)
                    // Stocker l'adresse de base du tableau dans la variable
                    compiler.addInstruction(new STORE(rBase, varName.getExpDefinition().getOperand()));

                    // Libérer le registre utilisé pour la base
                    rBase.setInUse(false);
                }
                else {
                    throw new IllegalArgumentException(
                            "Structure inattendue lors de l'initialisation d'un tableau."
                    );
                }
            }
        }
    }

    private void allocateSubArray(DecacCompiler compiler, List<AbstractExpr> dimensions, GPRegister rBase) {
        // Calculer la taille de la première dimension
        int firstDimensionSize;
        if (dimensions.get(0) instanceof IntLiteral) {
            firstDimensionSize = ((IntLiteral) dimensions.get(0)).getValue();
        } else {
            throw new UnsupportedOperationException(
                    "Les dimensions dynamiques pour ArrayNoInit ne sont pas encore supportées."
            );
        }

        // Allouer la mémoire pour la première dimension
        compiler.addInstruction(new NEW(firstDimensionSize, rBase));

        // Si d'autres dimensions restent, allouer les sous-tableaux récursivement
        if (dimensions.size() > 1) {
            List<AbstractExpr> subDimensions = dimensions.subList(1, dimensions.size());

            for (int i = 0; i < firstDimensionSize; i++) {
                GPRegister rSubArray = GestionRegistre.obtainFreeRegister();
                allocateSubArray(compiler, subDimensions, rSubArray);

                compiler.addInstruction(new STORE(rSubArray, new RegisterOffset(i, rBase)));

                rSubArray.setInUse(false);
            }
        } else {
            // Si c'est un tableau unidimensionnel, initialiser à zéro
            GPRegister rTemp = GestionRegistre.obtainFreeRegister();
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), rTemp));
            for (int offset = 0; offset < firstDimensionSize; offset++) {
                compiler.addInstruction(new STORE(rTemp, new RegisterOffset(offset, rBase)));
            }
            rTemp.setInUse(false);
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
                    compiler.addInstruction(new NEW(subSize, subReg));
                    // c) Stocker la référence dans baseReg[i]
                    compiler.addInstruction(
                            new STORE(subReg, new RegisterOffset(i, baseReg))
                    );

                    // d) Remplir récursivement
                    remplissageRecursif(compiler, coeffCourant, subReg);
                    subReg.setInUse(false);
                }
                else if (coeffCourant == null) {
                    GPRegister r = GestionRegistre.obtainFreeRegister();
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
                    r.setInUse(false);
                }
                else if (coeffCourant instanceof IntLiteral) {
                    int val = ((IntLiteral) coeffCourant).getValue();
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    compiler.addInstruction(new LOAD(new ImmediateInteger(val), r));
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    r.setInUse(false);
                }
                else if(coeffCourant instanceof FloatLiteral) {
                    float val = ((FloatLiteral) coeffCourant).getValue();
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    compiler.addInstruction(new LOAD(new ImmediateFloat(val), r));
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    r.setInUse(false);
                }
                else if(coeffCourant instanceof BooleanLiteral) {
                    boolean val = ((BooleanLiteral) coeffCourant).getValue();
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    if(val){
                        compiler.addInstruction(new LOAD(new ImmediateInteger(1), r));
                    }
                    else{
                        compiler.addInstruction(new LOAD(new ImmediateInteger(0), r));

                    }
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    r.setInUse(false);
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

    /**
     * Calcule la taille (nombre d'éléments) nécessaire pour un bloc correspondant
     * à 'element', qui peut être un Array ou ArrayLiteral.
     * Dans cet exemple, on suppose qu'on n'a qu'un "niveau" de taille à allouer.
     */
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
}