package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.DeclVar;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import org.apache.commons.lang.Validate;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Array extends AbstractExpr{
    private int size;
    private AbstractExpr[] coefficients;


    public Array(ArrayList<Integer> sizes, List<AbstractExpr> coefficients) {
        Validate.notNull(sizes);
        this.size = sizes.get(0);
        if (sizes.size() != 2){
            sizes.remove(0);
            if(size <= 0 ){
                throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
            }
            this.coefficients = new AbstractExpr[size];
            for (int i = 0; i < size; i++) {
                this.coefficients[i] = new Array(sizes, null);
            }
        }
        else {
            this.coefficients = new AbstractExpr[size];
            for (int i = 0; i < size; i++) {
                this.coefficients[i] = new Array(sizes.get(1), null);
            }
        }

        if(coefficients != null && coefficients.size() != 0){
            this.coefficients = coefficients.toArray(new AbstractExpr[size]);
        }
    }

    public AbstractExpr getCoefficients(int index) {
        return coefficients[index];
    }

    public AbstractExpr[] getCoefficients() {
        return coefficients;
    }

    public Array(int size, List<AbstractExpr> coefficients) {
        if(size ==0 ){
            this.size = 0;
            this.coefficients = new AbstractExpr[0];
        }
        if (size < 0) {
            throw new IllegalArgumentException("La taille du tableau doit être strictement positive !");
        }
        this.size = size;
        if(coefficients != null){
            this.coefficients = coefficients.toArray(new AbstractExpr[size]);
        }
        else {
            this.coefficients = new AbstractExpr[size];
            for(int i = 0; i < size; i++){
                this.coefficients[i] = null;
            }
        }
    }

    public int getSize() {
        return size;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {

        // 1) On récupère le type global du tableau (ex: "float[]")
        String name = getType().getName().getName();
        Type arrayType = compiler.getEnvironmentType()
                .defOfType(compiler.createSymbol(name))
                .getType();

        // 2) Vérification que c'est bien un tableau,
        //    et qu'il a la dimension attendue (le code "getBaseType" est hypothétique)
        if (arrayType.sameType(getBaseType(compiler, arrayType))) {
            throw new ContextualError(
                    "ArrayLiteral : l'array n'est pas de la bonne dimension !",
                    this.getLocation()
            );
        }

        // 3) Déterminer le type des coefficients = "typeChild"
        //    Par ex, si name = "float[]", alors nameChild = "float"
        String nameChild = name.substring(0, name.length() - 2);
        Type typeChild = compiler.getEnvironmentType()
                .defOfType(compiler.createSymbol(nameChild))
                .getType();

        // 4) Vérifier chaque coefficient
        for (int i = 0; i < coefficients.length; i++) {
            AbstractExpr coeff = coefficients[i];

            // S'il y a un coefficient (pas null)
            if (coeff != null) {
                // a) Vérifier son type
                Type coeffType = coeff.verifyExpr(compiler, localEnv, currentClass);

                // b) Si c'est un sous-tableau, on laisse faire la récursivité
                //    (Array ou ArrayLiteral => déjà géré par verifyExpr)
                //    Sinon, on compare au typeChild
                if (!(coeff instanceof Array)) {
                    // si le type ne matche pas
                    if (!typeChild.sameType(coeffType)) {

                        // c) Cas autorisé : typeChild = float, coeffType = int => on cast via ConvFloat
                        if (typeChild.isInt() && coeffType.isFloat()) {
                            // On crée le nœud ConvFloat
                            ConvFloat conv = new ConvFloat(coeff);
                            conv.setLocation(coeff.getLocation());
                            conv.setType(compiler.environmentType.FLOAT);

                            // On remplace l'ancien coefficient dans le tableau
                            coefficients[i] = conv;

                            // On re-vérifie pour mettre à jour le type
                            Type newType = conv.verifyExpr(compiler, localEnv, currentClass);

                            // Contrôle : on attend float désormais
                            if (!typeChild.sameType(newType)) {
                                throw new ContextualError(
                                        "Impossible de convertir l'élément en float",
                                        conv.getLocation()
                                );
                            }
                        }
                        else {
                            // d) Sinon, on lève une erreur
                            throw new ContextualError(
                                    "Le coefficient n'est pas du type souhaité. "
                                            + "Type voulu : " + typeChild + ", reçu : " + coeffType,
                                    coeff.getLocation()
                            );
                        }
                    }
                }
            }
        }

        // 5) On retourne le type global (ex: "float[]")
        return arrayType;
    }

        public static String removeIntegers(String input) {
        return input.replaceAll("\\d", ""); // Remplace tous les chiffres par une chaîne vide
    }

    // méthode qui récupère le type de base, par ex(int[][][] -> int)
    public Type getBaseType(DecacCompiler compiler,Type type) throws ContextualError {
        String typeName = type.getName().getName();
        String baseTypeName = extractBaseType(typeName); // Enlève les `[]`
        SymbolTable.Symbol baseSymbol = compiler.createSymbol(baseTypeName);
        TypeDefinition baseTypeDef = compiler.getEnvironmentType().defOfType(baseSymbol);

        if (baseTypeDef == null) {
            throw new ContextualError("Le type de base '" + baseTypeName + "' n'est pas défini.", getLocation());
        }
        return baseTypeDef.getType();
    }


    public String extractBaseType(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }

    @Override
    public void decompile(IndentPrintStream s) {

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        for (AbstractExpr coeff : coefficients){
            if(coeff != null){
                coeff.prettyPrint(s,prefix,false);
            }
            else {
                System.out.println("null");
            }
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }

    public String toString(){
        System.out.println("[");
        for (AbstractExpr coefficient : coefficients) {
            System.out.print(coefficient+",");
        }
        return "]";
    }
}
