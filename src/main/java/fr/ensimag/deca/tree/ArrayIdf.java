package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;


public class ArrayIdf extends Identifier {
    private int dimensions;
    private ArrayList<Integer> taille;
    private AbstractExpr array;

    public ArrayIdf(Symbol name) {
        super(name);
        taille = new ArrayList<Integer>();
        dimensions = 0;
        boolean entreCrochet = false;
        String nbr="";
        for (char caractere : name.getName().toCharArray()) {
            if (caractere == ']') {
                entreCrochet = false;
                taille.add(parseInt(nbr));
                nbr = "";
            }
            if (entreCrochet) {
                nbr += caractere;
            }
            if (caractere == '[') {
                entreCrochet = true;
                dimensions += 1;
            }
        }
    }

    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        EnvironmentType env = compiler.getEnvironmentType();

        String fullName = getName().getName();
        String baseName = WithoutBrackets(fullName);
        ArrayList<Type> T=new ArrayList<>();
        // Vérifie que le type de base (sans crochets) est défini
        Symbol baseSymbol = compiler.createSymbol(baseName);
        TypeDefinition baseTypeDef = env.defOfType(baseSymbol);
        if (baseTypeDef == null) {
            throw new ContextualError("Base type '" + baseName + "' is not defined", getLocation());
        }

        // Récupération du type de base
        Type currentType = baseTypeDef.getType();

        // Construction progressive des types tableaux
        for (int i = 0; i < dimensions; i++) {
            Symbol arraySymbol = compiler.createSymbol(removeIntegers(fullName));
            T.add(currentType);
            currentType = new ArrayType(arraySymbol, currentType, i + 1);
        }
        if(taille.size()==1) {
            this.array = new Array(taille.get(0), baseTypeDef.getType());
        }
        else{
            this.array = new Array(taille,T);
        }
        // Associez le dernier type tableau à l'identifiant
        setDefinition(new TypeDefinition(currentType, getLocation()));
        setType(currentType);
        return currentType;
    }

    public String WithoutBrackets(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }

    @Override
    String prettyPrintNode() {
        return "IdfArray" + super.prettyPrintNode();
    }

    public static String extractBaseType(String fullName) {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return fullName; // Pas de crochets, le type est directement le type de base
        }
        return fullName.substring(0, bracketIndex);
    }

    private int countArrayDimensions(String fullName) {
        int count = 0;
        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i) == '[') {
                count++;
            }
        }
        return count; // Chaque "[" correspond à une dimension
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        //System.out.println("hola");
        Type t = verifyType(compiler);
        //System.out.println(getType());
        String nameString= getName().getName();
        String nameStringWithoutInt = removeIntegers(nameString);
        ExpDefinition def = localEnv.get(compiler.createSymbol(nameStringWithoutInt));
        if (def != null) {
            return def.getType();
        }
        // 🔹 2️⃣ Vérifier si c'est un type connu dans `env_types`
        TypeDefinition typeDef = compiler.getEnvironmentType().defOfType(compiler.createSymbol(nameStringWithoutInt));
        if (typeDef != null) {
            ExpDefinition newDef = new VariableDefinition(typeDef.getType(), getLocation());
            // Ajoute la définition du type dans `localEnv`
            try {
                localEnv.declare(getName(), newDef);
            } catch(EnvironmentExp.DoubleDefException e) {
                throw new ContextualError("La variable "+getName()+" est déjà définie !",this.getLocation());
            }
            return typeDef.getType();
        }

        // 🔹 3️⃣ Si l'identifiant n'est trouvé ni dans `localEnv` ni dans `env_types`, lever une erreur
        throw new ContextualError("Identifier '" + getName().getName() + "' is not defined", getLocation());
    }

    public static String removeIntegers(String input) {
        return input.replaceAll("\\d", ""); // Remplace tous les chiffres par une chaîne vide
    }
}
