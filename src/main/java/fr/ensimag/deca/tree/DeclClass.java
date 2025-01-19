package fr.ensimag.deca.tree;



import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Objects;

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
    private ArrayList<Label> tabLabel;

    public void addTabLabel(Label label) {
        this.tabLabel.add(label);
    }

    public void setTabLabel(int index,Label label) {
        this.addTabLabel(label);
        this.tabLabel.set(index, label);
    }

    public SymbolTable.Symbol getSymbol() {
        return className.getName();
    }

    public DeclClass(AbstractIdentifier className, AbstractIdentifier extendsName, ListBody classBody) {
        Validate.notNull(className);
        Validate.notNull(classBody);
        this.className = className;
        this.extendsName = extendsName;
        this.classBody = classBody;

        this.tabLabel = new ArrayList<Label>();

//        Type t = this.className.getType();
//        if (t instanceof ClassType) {
//            ClassType classType = (ClassType) t;
//            ClassDefinition ex = classType.getDefinition();
//            this.tabLabels = new Label[ex.getNumberOfMethods()]; //On crée pour chaque class un tableau
//        }
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
        if(type instanceof ClassType) {
            ClassType classType = (ClassType)type;
            if(extendsName != null) {
                ClassDefinition classDefinition = new ClassDefinition(classType,getLocation(),extendsName.getClassDefinition());
                VariableDefinition varDef = new VariableDefinition(type, this.getLocation());
                className.setDefinition(classDefinition);
                className.setType(classType);
            }
            else {
                ClassDefinition classDefinition = new ClassDefinition(classType,getLocation(),compiler.environmentType.OBJET.getDefinition());
                VariableDefinition varDef = new VariableDefinition(type, this.getLocation());
                className.setDefinition(classDefinition);
                className.setType(classType);
            }
        }
        else{
            throw new ContextualError("ce n'est pas une class",getLocation());
        }
    }
    protected void initDefault(DeclField field, DecacCompiler compiler){
        Type type = field.getTypeIdentifier().getDefinition().getType();
        //System.out.println(type);
        if(type instanceof  BooleanType || type instanceof IntType){
            compiler.getToExecuteEnd().add(new Line(new LOAD(0, Register.R0)));
        } else if (type instanceof FloatType) {
            compiler.getToExecuteEnd().add(new Line(new LOAD(new ImmediateFloat(0.0F), Register.R0)));
        }
        else{

            compiler.getToExecuteEnd().add (new Line(new LOAD(new NullOperand(), Register.R0)));
        }

    }
    protected void initByUser(DeclField field, DecacCompiler compiler, int cpt){
        AbstractExpr initiali = ((Initialization)field.getInitialization()).getExpression();
        //on l'evalue dans r0
        //il faut gérer les evalue des classes pour voir comment gérer cela

        //il faut aussi trouver une solution pour gérer le cas ou l'appel de cette fonction à fait
        // des appels de Piles et ainsi les ajouter à d1

        /*
         * ici il faut que les calcul du chargement se mette à la queue aussi (à voir)*/
        GPRegister r1 = GestionRegistre.obtainFreeRegister();
        // que se passe t-il si r1 est null, on va utiliser R1
        //on ne peut pas directement utiliser R0, car c'est notre registre de secours
        // dans les calculs quand il n'ya plus de registre
        if(initiali instanceof New){
            GPRegister r = Register.getR(2);
            compiler.addInstructionEnd(new PUSH(r));

            New ident = (New)initiali;
            //si c'est une classe definition
            ClassDefinition def = ident.getClassDef();
            //System.out.println(def);

            int nbrTotFields = def.getNumberOfFields() + def.getSuperClass().getNumberOfFields();
            compiler.addInstructionEnd(new NEW(nbrTotFields+1, r));

            compiler.getToExecuteEnd().add(new Line(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1)));
            compiler.getToExecuteEnd().add(new Line(new STORE(r, new RegisterOffset(cpt, Register.R1))));

            compiler.addInstructionEnd(new BOV(new Label("tas_plein")));
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
        if(r1 != null){
            initiali.evalueExprEnd(compiler, r1);
            compiler.getToExecuteEnd().add(new Line(new LOAD(r1, Register.R0)));
            r1.setInUse(false);
        }
        else{
            // on sauvegarde r1, et on le recupera ensuite
            //on aurait pu prendre ici n'importe quel registre en fait sauf r0 et r1'
            GPRegister r = Register.getR(2);
            compiler.getToExecuteEnd().add(new Line(new PUSH(r)));
            initiali.evalueExprEnd(compiler, r);
            compiler.getToExecuteEnd().add(new Line(new LOAD(r, Register.R0)));
            // on remet la bonne valeur de R1
            compiler.getToExecuteEnd().add(new Line(new POP(r)));
        }

    }
    protected void codeGenInit(DecacCompiler compiler)  {
        String nom_label = "init."+this.getSymbol().getName();
        AbstractLine ligne_label = new Line(new Label(nom_label));
        compiler.getToExecuteEnd().add(ligne_label);
        int d1 = 0; //Pour l'instant pas de TSTO
        int dernierIndice = compiler.getToExecuteEnd().size();
        int nombreVarSuper;
        if(extendsName == null){
            nombreVarSuper = 0;
        }
        else{

            TypeDefinition t= compiler.getEnvironmentType().getEnvTypes().get(extendsName.getName());
            if(t instanceof ClassDefinition){
                ClassDefinition classDefinition = (ClassDefinition)t;
                nombreVarSuper = classDefinition.getNumberOfFields();
            }
            else{
                nombreVarSuper = 0;
            }
        }
        int cpt =  nombreVarSuper+1;

        //premier for pour initialiser à Null si y'en a pas ou bien qu'il y'a pas de extends
        for (AbstractBody element: classBody.getList()){
            if(element instanceof DeclField){
                DeclField field = (DeclField)element;
                //si il n'ya pas d'initialisation, on le fait à 0 null ou false selon le champ
                if(field.getInitialization() instanceof NoInitialization || extendsName != null){
                    initDefault(field, compiler);
                    compiler.getToExecuteEnd().add(new Line(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1)));
                    compiler.getToExecuteEnd().add(new Line(new STORE(Register.R0, new RegisterOffset(cpt, Register.R1))));
                }

                //il faut appeler la methode init du parent
                field.getVarName().getFieldDefinition().setIndex(cpt);
                cpt++;
            }
        }
        //appel de la methode init du parent
        if(this.extendsName!= null){
            compiler.getToExecuteEnd().add(new Line("Appel de l'initialisation de la classe herité "+extendsName.getName()));
            compiler.getToExecuteEnd().add(new Line(new PUSH(Register.R1)));
            Label initSuper = new Label("init."+extendsName.getName());
            compiler.getToExecuteEnd().add(new Line(new BSR(initSuper)));
            compiler.getToExecuteEnd().add(new Line(new SUBSP(1)));
        }

        compiler.getToExecuteEnd().add(new Line("initialisation explicite"));
        cpt = nombreVarSuper+1;
        for (AbstractBody element: classBody.getList()){
            if(element instanceof DeclField){
                DeclField field = (DeclField)element;
                //si il n'ya pas d'initialisation, on le fait à 0 null ou false selon le champ
                if(field.getInitialization() instanceof Initialization){
                    initByUser(field, compiler, cpt);
                    if(! (((Initialization)field.getInitialization()).getExpression() instanceof New)) {
                        compiler.getToExecuteEnd().add(new Line(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1)));
                        compiler.getToExecuteEnd().add(new Line(new STORE(Register.R0, new RegisterOffset(cpt, Register.R1))));
                    }
                    d1++;
                }
                field.getVarName().getFieldDefinition().setIndex(cpt);
                cpt++;
            }
        }

        compiler.getToExecuteEnd().add(new Line(new RTS()));

        if(d1 != 0){
            compiler.getToExecuteEnd().add(dernierIndice, new Line(new TSTO(d1)));
            compiler.getToExecuteEnd().add(dernierIndice+1, new Line(new BOV(new Label("pile_pleine"))));
        }


    }
    protected void codeGenMethodBody(DecacCompiler compiler) {
        for (AbstractBody method: classBody.getList()){
            if(method instanceof DeclMethod){
                GestionRegistre.setIndiceRegistreLB(0);
                DeclMethod method1 = (DeclMethod)method;
                String nom_label = "code."+this.getSymbol().getName()+"."+ method1.getVarName().getName();
                AbstractLine ligne_label = new Line(new Label(nom_label));
                compiler.getToExecuteEnd().add(ligne_label);
                int d1 = 0; //Pour l'instant pas de TSTO
                int dernierIndice = compiler.getToExecuteEnd().size();

                compiler.getToExecuteEnd().add(new Line("XXXXXXXXXXXXXX"));
                //init des varaibles avec leur index
                compiler.addInstructionEnd(new ADDSP(method1.getDeclVar().getList().size()));
                //les paramètres ont été gérer dans le verify
                for (AbstractDeclVar decl : method1.getDeclVar().getList()){
                    decl.codeGenDeclVarMethod(compiler);
                }
                String nom_label_fin = "fin."+this.getSymbol().getName()+"."+ method1.getVarName().getName();
                for (AbstractInst ins : method1.getInsts().getList()){
                    //RegisterOffset parentRegister = new RegisterOffset(-2, Register.LB);
                    if(ins instanceof Return){
                        ((Return)ins).codeGenInstReturn(compiler, nom_label_fin);
                    }
                    ins.codeGenInstMethod(compiler);
                }
                compiler.addLabelEnd(new Label("etq_fin.1"));
                compiler.addInstructionEnd(new WSTR("Erreur:Sortie de la methode "+this.getSymbol().getName()+"."+ method1.getVarName().getName()+"sans return"));
                compiler.addInstructionEnd(new WNL());
                compiler.addInstructionEnd(new ERROR());

                AbstractLine ligne_label_fin = new Line(new Label(nom_label_fin));
                compiler.getToExecuteEnd().add(ligne_label_fin);
                compiler.addInstructionEnd(new RTS());
                compiler.getToExecuteEnd().add(new Line("XXXXXXXXXXXXXXX"));
            }

        }

    }
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
        GestionRegistre.classFinCour = this.className.getName().getName();
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

    public void createTableauLabel(DecacCompiler compiler) {
        // Pour chaque classe à l'indice 0 elle doit avoir le code de la méthode
        // de Object.equals car chaque class hérite de Object
        this.setTabLabel(0,new Label("code.Object.equals"));
        // Ensuite quand on ajoutera les méthodes il faudra pour chaque classe
        // regarder si elle a une super class, si oui il faudra ajouter les méthodes
        // de la super class dans le tableau en faisant attention aux indices
        DeclClass declClass = this;
//        TypeDefinition t= compiler.getEnvironmentType().getEnvTypes().get(declClass.extendsName.getName());
//        ClassDefinition classDef = (ClassDefinition) t;
        if (declClass.extendsName != null){
            //ArrayList<Label> test = declClass.
            TypeDefinition t= compiler.getEnvironmentType().getEnvTypes().get(extendsName.getName());
            ClassDefinition classDef = (ClassDefinition) t;

            for (Label lab : classDef.getLabels()) {
                if (Objects.equals(lab.toString(), "code.Object.equals")) {
                    continue;
                }
                this.addTabLabel(lab);
            }
            ClassDefinition parent = classDef.getSuperClass(); // On récupère la classe parent de la classe actuelle
            //for (Label labPar : )
        }
        // Si ca vaut nul c'est que la super classe est Object
        // On va itérer sur les noms des méthodes de la classe courante
        TypeDefinition t = compiler.getEnvironmentType().getEnvTypes().get(className.getName());
        ClassDefinition currentClass = (ClassDefinition)t;
        for (SymbolTable.Symbol methodName : currentClass.getMembers().getLocalEnv().keySet()) {
            ExpDefinition expDefinition= currentClass.getMembers().getLocalEnv().get(methodName);
            if (expDefinition instanceof MethodDefinition) { // Si on est bien avec une méthode
                // On ajoute son label
                this.addTabLabel(new Label("code." + currentClass.getType().getName().getName() + "." + methodName.getName()));
            }
        }
    }

    @Override
    public void declareTableMethode(DecacCompiler compiler) {
        // On construit le tableau des étiquettes de méthodes de chaque classe
        this.createTableauLabel(compiler);

        TypeDefinition tip = compiler.getEnvironmentType().getEnvTypes().get(className.getName());
        ClassDefinition clas = (ClassDefinition) tip;
        for (SymbolTable.Symbol meth : clas.getMembers().getLocalEnv().keySet()){
            ExpDefinition expDefinition= clas.getMembers().getLocalEnv().get(meth);
            if (expDefinition instanceof MethodDefinition) { // Si on est bien avec une méthode
                MethodDefinition methodDefinition = (MethodDefinition) expDefinition;
                methodDefinition.setoffSet(this.tabLabel.indexOf(new Label("code."+ clas.getType().getName().getName() + "." + meth.getName())) + 1);
            }

        }

        compiler.setToAddStack(compiler.getToAddStack()+1+this.tabLabel.size());

        // Pour class on veut récupérer si elle en a ses super class
        // Si elle n'en a pas sa super class va être Object
        String debComment = "Construction de la table des méthode de ";
        // On cherche la classe mère de la classe que l'on traite, pour savoir où est initialiser sa table des méthode
        TypeDefinition nom = compiler.getEnvironmentType().getEnvTypes().get(((DeclClass) this).getSymbol());
        if (nom instanceof ClassDefinition) {
            ClassDefinition classDef = (ClassDefinition) nom;
            classDef.setLabels(this.tabLabel);
            // Il faut dire à quelle adresse dans la pile à étét déclaré la table des méthodes de la classe courante
            classDef.setAdrTableMethode(GestionRegistre.getIndiceRegistreGB() + 1);
            String endComment = String.valueOf(classDef.getType()); // J'espère que cela récupère le nom de la classe
            compiler.addComment(debComment + endComment);
            ClassDefinition parent = classDef.getSuperClass();
            // Ici on récupère Object si la classe actuelle n'hérite d'aucune classe
            if (Objects.equals(parent.getType().getName().getName(), "objet")) {
                // Si la super classe est la classe objet, on sait ou est définie sa table des méthode
                // C'est à 1(GB)
                compiler.addInstruction(new LEA(new RegisterOffset(1, Register.GB), Register.R0));
                // On charge l'adresse de la table des méthode de la classe
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1), Register.GB)));
            } else { // Si la classe hérite d'une autre clase que Object on doti récupérer l'adresse du début de la table des méthode de cette super classe
                // On récupère l'adrese de la table des méthodes de la super classe
                compiler.addInstruction(new LEA(new RegisterOffset(parent.getAdrTableMethode(), Register.GB), Register.R0));
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1), Register.GB)));
            }
        }
        // Maintenant on va itérer sur les méthodes de chaque classe
        for (Label methodLabel : this.tabLabel) {
            // On met chaque label au bon endroit dans la pile
            compiler.addInstruction(new LOAD(new LabelOperand(methodLabel), Register.R0));
            compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(GestionRegistre.obtainIndiceGPandIncrement(1),Register.GB)));
        }
    }
}
