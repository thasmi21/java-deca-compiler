package fr.ensimag.deca.tree;



import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl06
 * @date 01/01/2025
 */
public class DeclClass extends AbstractDeclClass {

    final private AbstractIdentifier className;
    final private AbstractIdentifier extendsName;
    final private ListBody classBody;

    public SymbolTable.Symbol getSymbol() {
        return className.getName();
    }

    public DeclClass(AbstractIdentifier className, AbstractIdentifier extendsName, ListBody classBody) {
        Validate.notNull(className);
        Validate.notNull(classBody);
        this.className = className;
        this.extendsName = extendsName;
        this.classBody = classBody;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        className.decompile(s);
        if (extendsName != null) {
            extendsName.decompile(s);
        }
        if (classBody != null){
            classBody.decompile(s);
        }
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        EnvironmentType envType = compiler.getEnvironmentType();
        if(extendsName != null) {
            envType.putClass(className.getName(),extendsName.getName(),getLocation());
        }
        else {
            envType.putClass(className.getName(),getLocation());
        }
        EnvironmentExp environmentExp=compiler.environmentType.getEnvironmentMain();
        Type type = envType.getEnvTypes().get(className.getName()).getType();
        VariableDefinition varDef = new VariableDefinition(type, this.getLocation());
        try {
            environmentExp.declare(className.getName(), varDef);
        } catch(EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("La variable "+className.getName()+" est déjà définie !",this.getLocation());
        }
        className.setDefinition(varDef);
        className.setType(type);
    }
    protected void codeGenInit(DecacCompiler compiler)  {
        String nom_label = "init."+this.getSymbol().getName();
        AbstractLine ligne_label = new Line(new Label(nom_label));
        compiler.getToExecuteEnd().add(ligne_label);
        int d1 = 1; //Pour l'instant pas de TSTO
        int dernierIndice = compiler.getToExecuteEnd().size();
        int cpt = 1;

        for (AbstractBody element: classBody.getList()){
            if(element instanceof DeclField){
                DeclField field = (DeclField)element;
                AbstractIdentifier identType = ((DeclField)element).getTypeIdentifier();
                Type type = identType.getType();
                //si il n'ya pas d'initialisation, on le fait à 0 null ou false selon le champ
                if(field.getInitialization() instanceof NoInitialization){
                    if(type instanceof  BooleanType || type instanceof IntType){
                        compiler.getToExecuteEnd().add(new Line(new LOAD(0, Register.R0)));
                    } else if (type instanceof FloatType) {
                        compiler.getToExecuteEnd().add(new Line(new LOAD(new ImmediateFloat(0.0F), Register.R0)));
                    }
                    else{

                        compiler.getToExecuteEnd().add (new Line(new LOAD(new NullOperand(), Register.R0)));
                    }
                }
                //sinon, On charge l'initialisation en question
                else{
                    AbstractExpr initiali = ((Initialization)field.getInitialization()).getExpression();
                    //on l'evalue dans r0
                    //il faut gérer les evalue des classes pour voir comment gérer cela

                    //il faut aussi trouver une solution pour gérer le cas ou l'appel de cette fonction à fait
                    // des appels de Piles et ainsi les ajouter à d1

                    /*
                    * ici il faut que les calcul du chargement se mette à la queue aussi (à voir)*/
                    initiali.evalueExprEnd(compiler, Register.R1);
                    compiler.getToExecuteEnd().add(new Line(new LOAD(Register.R1, Register.R0)));

                }


                compiler.getToExecuteEnd().add(new Line(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1)));
                compiler.getToExecuteEnd().add(new Line(new STORE(Register.R0, new RegisterOffset(cpt, Register.R1))));


                //il faut appeler la methode init du parent

                cpt++;
            }
        }

        compiler.getToExecuteEnd().add(new Line(new RTS()));

        if(d1 != 0){
            compiler.getToExecuteEnd().add(dernierIndice, new Line(new TSTO(d1)));
            compiler.getToExecuteEnd().add(dernierIndice+1, new Line(new BOV(new Label("pile_pleine"))));
        }


    }
    protected void codeGenMethodBody(DecacCompiler compiler) {}
    @Override
    protected void codeGenDeclClass(DecacCompiler compiler) {
        // Pour chaque class, on doit définir sa table des méthodes dans la passe 1
        this.codeGenInit(compiler);
        this.codeGenMethodBody(compiler);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        EnvironmentExp environmentExp=((ClassDefinition)(compiler.getEnvironmentType().getEnvTypes().get(className.getName()))).getMembers();
        TypeDefinition typeDefinition= compiler.getEnvironmentType().getEnvTypes().get(className.getName());
        if(typeDefinition != null && typeDefinition.isClass()) {
            ClassDefinition classDefinition = (ClassDefinition)typeDefinition;
            classBody.verifyClassMembers(compiler,environmentExp,classDefinition);
        }
        else{
            throw new ContextualError(className.getName().getName()+"n'est pas une classe",getLocation());
        }
    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        EnvironmentExp environmentExp=((ClassDefinition)(compiler.getEnvironmentType().getEnvTypes().get(className.getName()))).getMembers();
        TypeDefinition typeDefinition= compiler.getEnvironmentType().getEnvTypes().get(className.getName());
        if(typeDefinition != null && typeDefinition.isClass()) {
            ClassDefinition classDefinition = (ClassDefinition)typeDefinition;
            classBody.verifyClassBody(compiler,environmentExp,classDefinition);
        }
        else{
            throw new ContextualError(className.getName().getName()+"n'est pas une classe",getLocation());
        }
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s,prefix,false);
        if (extendsName != null) {
            extendsName.prettyPrint(s,prefix+" extends ",false);
        }
        if (classBody != null) {
            classBody.prettyPrint(s,prefix,true);
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iterChildren(f);
        if (extendsName != null) {
            extendsName.iterChildren(f);
        }
        if (classBody != null){
            classBody.iterChildren(f);
        }
    }

    @Override
    public void declareTableMethode(DecacCompiler compiler) {
        // Pour class on veut récupérer si elle en a ses super class
        // Si elle n'en a pas sa super class va être Object
        String debComment = "Construction de la table des méthode de ";

        // Maintenant on va itérer sur les méthodes de chaque classe
        DeclClass declClass = (DeclClass) this;
        TypeDefinition nom = compiler.getEnvironmentType().getEnvTypes().get(((DeclClass) this).getSymbol());
        if(nom instanceof ClassDefinition){
            ClassDefinition classDef = (ClassDefinition) nom;
            String endComment = String.valueOf(classDef.getType()); // J'espère que cela récupère le nom de la classe
            compiler.addComment(debComment + endComment);
            ClassDefinition parent = classDef.getSuperClass();
            if (parent == null) { // Si la classe n'hérite que de Object
                RegisterOffset regOff =  new RegisterOffset(1, Register.GB);
                // Si on rentre dans cette boucle c'est que la class c n'hérite que de Object
                compiler.addInstruction(new LEA(regOff,Register.R0));
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(3, Register.GB)));
                compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(4, Register.GB)));
            }
        }
    }
}
