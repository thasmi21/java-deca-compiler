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
                new RegisterOffset(
                        GestionRegistre.obtainIndiceGPandIncrement(1),
                        Register.GB
                )
        );

        if (initialization instanceof Initialization) {
            AbstractExpr expr = ((Initialization) initialization).getExpression();

            // Cas simple : pas un tableau
            if (!(expr.getType() instanceof ArrayType)) {
                GPRegister r = GestionRegistre.obtainFreeRegister();
                expr.evalueExpr(compiler, r);
                compiler.addInstruction(
                        new STORE(r, varName.getExpDefinition().getOperand())
                );
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
                    fillArrayRecursive(compiler, tabLiteral, rBase);

                    // Stocker l'adresse du tableau dans la variable
                    compiler.addInstruction(
                            new STORE(rBase, varName.getExpDefinition().getOperand())
                    );
                    rBase.setInUse(false);
                }
                else if(expr instanceof ArrayNoInit){
                    ArrayNoInit exprArray = (ArrayNoInit) expr;
                    List<AbstractExpr> indices = exprArray.getIndices();
                    int numDimensions = indices.size();
                    List<Integer> sizes = new ArrayList<>();


                }
                else {
                    throw new IllegalArgumentException(
                            "Structure inattendue lors de l'initialisation d'un tableau."
                    );
                }
            }
        }
    }

    /**
     * Remplit récursivement un bloc mémoire alloué en partant d'un Array ou ArrayLiteral.
     * @param element Peut être un ArrayLiteral, un Array, un Integer, ou null.
     */
    private void fillArrayRecursive(
            DecacCompiler compiler,
            Object element,
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
                Object subElement = array.getCoefficients(i);

                // Sous-élément = sous-tableau ?
                if (subElement instanceof Array
                        || subElement instanceof ArrayLiteral) {

                    // a) Calcul de la taille du sous-tableau :
                    int subSize = getArraySize(subElement);

                    // b) Allouer un bloc pour ce sous-tableau
                    GPRegister subReg = GestionRegistre.obtainFreeRegister();
                    compiler.addInstruction(new NEW(subSize, subReg));

                    // c) Stocker la référence dans baseReg[i]
                    compiler.addInstruction(
                            new STORE(subReg, new RegisterOffset(i, baseReg))
                    );

                    // d) Remplir récursivement
                    fillArrayRecursive(compiler, subElement, subReg);
                    subReg.setInUse(false);
                }
                // Sous-élément = null ? on stocke 0
                else if (subElement == null) {
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    compiler.addInstruction(new LOAD(new ImmediateInteger(0), r));
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    r.setInUse(false);
                }
                // Sous-élément = entier ?
                else if (subElement instanceof IntLiteral) {
                    int val = ((IntLiteral) subElement).getValue();
                    GPRegister r = GestionRegistre.obtainFreeRegister();
                    compiler.addInstruction(new LOAD(new ImmediateInteger(val), r));
                    compiler.addInstruction(
                            new STORE(r, new RegisterOffset(i, baseReg))
                    );
                    r.setInUse(false);
                }
                // Autre type (String, float, etc.) ?
                else {
                    throw new IllegalArgumentException(
                            "Structure inattendue dans le tableau : "
                                    + subElement.getClass().getName()
                    );
                }
            }
        }
        // 3) Si ce n'est pas un Array, on jette une exception
        else {
            throw new IllegalArgumentException(
                    "fillArrayRecursive ne peut remplir qu'un Array ou ArrayLiteral."
            );
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