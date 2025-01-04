package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.commons.lang.Validate;
import java.io.PrintStream;
import java.util.ArrayList;

import static java.lang.System.arraycopy;

public class Array extends AbstractExpr{
    private final int size;
    private AbstractExpr[] coefficients;
    private Type type;



    // pour des assignations du type tab = new int[]
    public Array(int size, Type type) {
        //mettre le type comme ça ? Ou alors créer une classe ArrayType qui extends Type
        Validate.notNull(type);
        this.type = type;
        // size est-il utile ? je sais pas
        if(size <= 0 ){
            throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
        }
        this.size = size;
        this.coefficients = new AbstractExpr[size];
        for(int i = 0; i < size; i++){
            this.coefficients[i] = null;
        }
    }

    public Array(ArrayList<Integer> sizes, ArrayList<Type> types) {
        Validate.notNull(types);
        Validate.notNull(sizes);
        Type type = types.get(types.size()-1);
        Validate.notNull(type);
        int size = sizes.get(0);
        if(sizes.size()!=2){
            sizes.remove(0);
            types.remove(types.size()-1);
            if(size <= 0 ){
                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
            }
            this.size = size;
            this.coefficients = new AbstractExpr[size];
            for(int i = 0; i < size; i++){
                this.coefficients[i]= new Array(sizes, types);
            }
        }
        else{
            if(size <= 0 ){
                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
            }
            this.size = size;
            this.coefficients = new AbstractExpr[size];
            for(int i = 0; i < size; i++){
                this.coefficients[i]= new Array(size, type);
            }
        }
    }

    // pour des assignations du type tab = new int[] {1,2,3...}
    public Array(int size, Type type, AbstractExpr[] coefficients){
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
        Type typeArray= this.type;
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
