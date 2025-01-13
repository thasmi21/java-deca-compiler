package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.ensimag.ima.pseudocode.Register;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl06
 * @date 01/01/2025
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }
    
    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    public int getArret(){
        return arret;
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private List<File> sourceFiles = new ArrayList<File>();
    private int arret=0;


    
    public void parseArgs(String[] args) throws CLIException {
        // A FAIRE : parcourir args pour positionner les options correctement.
        ArrayList<String> chemins=new ArrayList<>();
        if(args.length == 0) {
            return;
        }
        if(args[0].equals("-b")) {
            if(args.length > 1) {
                throw new CLIException("deca -b n'a pas d'argument!!");
            }
            else {
                printBanner = true;
                return;
            }
        }
        int v=0;
        int p=0;
        int r=0;
        int P=0;
        int n=0;
        int w=0;
        int d=0;
        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-v")) {
                if(v==0 && p==0) {
                    v=1;
                    arret=2;
                }
                else{
                    throw new CLIException("l'option -v n'est utilisable qu'une seule fois ou sans l'option -p");
                }
            } else if (args[i].equals("-p") ) {
                if(v==0 && p==0) {
                    p=1;
                    arret=1;
                }
                else{
                    throw new CLIException("l'option -p n'est utilisable qu'une seule fois ou sans l'option -v");
                }
            }
            else if(args[i].equals("-r") ) {
                if(r==0){
                    if(args.length>i+1) {
                        int digit = Integer.parseInt(args[i+1]);
                        if(4<=digit && digit<=16){
                            r=1;
                            for(int j=15;j>=digit;j--){
                                Register.getR(j).setInUse(true);
                            }
                        }
                        else {
                            throw new CLIException("l'option -r de deca nécéssite un entier en argument compris entre 4 et 16");
                        }
                    }
                    else {
                        throw new CLIException("l'option -r de deca nécéssite un entier en argument");
                    }

                }
                else{
                    throw new CLIException("l'option -r n'est utilisable qu'une seule fois");
                }
            }
            else if(args[i].equals("-P")) {
                if(P==0) {
                    P=1;
                    parallel = true;
                }
                else{
                    throw new CLIException("l'option -r n'est utilisable qu'une seule fois");
                }
            }
            else if(args[i].equals("-n")) {
                if(n==0) {
                    n=1;
                }
                else {
                    throw new CLIException("l'option -n n'est utilisable qu'une seule fois");
                }
            }
            else if(args[i].equals("-w")) {
                if(w==0){
                    w=1;
                }
                else{
                    throw new CLIException("l'option -w n'est utilisable qu'une seule fois");
                }
            }
            else if(args[i].equals("-d")){
                if(d==0){
                    d=1;
                    debug=INFO;
                } else if (d==1) {
                    d=2;
                    debug=DEBUG;
                }
                else if (d==2) {
                    d+=1;
                    debug=TRACE;
                }
                else {
                    throw new CLIException("Il faut au maximum 3 -d");
                }
            }
            else {
                chemins.add(args[i]);
            }
        }
        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        if(w==1){
            logger.setLevel(Level.WARN);
        }
        switch (getDebug()) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        default:
            logger.setLevel(Level.ALL); break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());



        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }

        //placement du fichier source
        //on va de manière incremental sans gèrer toutes les options pour l'intant
        //String chemin = args[0];
        for(String chemin : chemins){
            File fichier = new File(chemin);
            if(fichier.exists() && fichier.isFile()){
                sourceFiles.add(fichier);
            }else{
                throw new CLIException("Entrer un nom de fichier correct!");
            }
        }
    }

    protected void displayUsage() {
        System.out.println("decac [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]\n" +
                ". -b (banner) : affiche une bannière indiquant le nom de l'équipe\n" +
                ". -p (parse) : arrête decac après l'étape de construction de\n" +
                "l'arbre, et affiche la décompilation de ce dernier\n" +
                "(i.e. s'il n'y a qu'un fichier source à\n" +
                "compiler, la sortie doit être un programme\n" +
                "deca syntaxiquement correct)\n" +
                ". -v (verification) : arrête decac après l'étape de vérifications\n" +
                "(ne produit aucune sortie en l'absence d'erreur)\n" +
                ". -n (no check) : supprime les tests à l'exécution spécifiés dans\n" +
                "les points 11.1 et 11.3 de la sémantique de Deca.\n" +
                ". -r X (registers) : limite les registres banalisés disponibles à\n" +
                "R0 ... R{X-1}, avec 4 <= X <= 16\n" +
                ". -d (debug) : active les traces de debug. Répéter\n" +
                "l'option plusieurs fois pour avoir plus de\n" +
                "traces.\n" +
                ". -P (parallel) : s'il y a plusieurs fichiers sources,\n" +
                "lance la compilation des fichiers en\n" +
                "parallèle (pour accélérer la compilation)");
    }
}
