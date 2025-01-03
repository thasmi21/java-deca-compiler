package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.commons.lang.Validate;
import java.io.PrintStream;

public class Array extends AbstractExpr{
    private final int size;
    private AbstractExpr[] coefficients;
    private final AbstractIdentifier type;

    // pour des assignations du type tab = new int[]
    public Array(int size, AbstractIdentifier type) {
        //mettre le type comme ça ? Ou alors créer une classe ArrayType qui extends Type
        Validate.notNull(type);
        this.type = type;
        // size est-il utile ? je sais pas
        if(size <= 0 ){
            throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
        }
        this.size = size;
        this.coefficients = new AbstractExpr[size];
    }

    // pour des assignations du type tab = new int[] {1,2,3...}
    public Array(int size, AbstractIdentifier type, AbstractExpr[] coefficients){
        this.type = type;
        if(size <= 0 ){
            throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
        }
        this.size = size;
        if(coefficients.length!=size) {
            throw new IllegalArgumentException("La taille de l'array doit être égale au nombre de coefficients !");
        }
        this.coefficients = coefficients;
    }

    public void setCoefficients(int i, AbstractExpr coeff) {
        coefficients[i]=coeff;
    }

    public AbstractExpr getCoefficients(int i) {
        return coefficients[i];
    }

    // Je vérifie que tous les coefficients du tableau sont du bon type
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type typeArray= this.type.verifyType(compiler);
        if (!(typeArray instanceof Type)) {
            throw new ContextualError("L'expression n'est pas un tableau valide.", getLocation());
        }
        for (AbstractExpr coefficient : coefficients) {
            Type type = coefficient.verifyExpr(compiler, localEnv, currentClass);
            if (type.isSubTypeOf(typeArray)) {
                throw new ContextualError("Les coefficients ne sont pas tous du bon type. Type souhaité : " + typeArray + ". Type reçu : "+ type, getLocation());
            }
        }

        return typeArray;
    }

    @Override
    public void decompile(IndentPrintStream s) {
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {

    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }
}
