package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        String chemin=null;
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
            if(args[i].equals("-v") && v==0 && p==0) {
                v=1;
                arret=2;
            } else if (args[i].equals("-p") && p==0 && v==0) {
                p=1;
                arret=1;
            }
            else if(args[i].equals("-r") && r==0) {
                if(args.length>i+1) {
                    int digit = Integer.parseInt(args[i+1]);
                    if(4<=digit && digit<=16){
                        r=1;
                        //A faire
                    }
                    else {
                        throw new CLIException("l'option -r de deca nécéssite un entier en argument compris entre 4 et 16");
                    }
                }
                else {
                    throw new CLIException("l'option -r de deca nécéssite un entier en argument");
                }
            }
            else if(args[i].equals("-P") && P==0) {
                P=1;
                parallel = true;
                //compilation en parallele à faire plus tard
            }
            else if(args[i].equals("-n")&& n==0) {
                n=1;
                //A faire
            }
            else if(args[i].equals("-w") && w==0) {
                w=1;
                // A faire
            }
            else if(args[i].equals("-d")){
                if(d==0){
                    d=1;
                    debug|=INFO;
                } else if (d==1) {
                    d=2;
                    debug|=DEBUG;
                }
                else {
                    debug|=TRACE;
                }
            }
            else if(args.length!=i+1){
                throw new CLIException("Le fichier doit être mis à la fin ou alors l'option n'est pas reconnue : " + args[i] + " Mettez la commande deca pour avoir toutes les options possibles.");
            }
            else {
                chemin = args[i];
            }
        }
        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
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
        File fichier = new File(chemin);
        if(fichier.exists() && fichier.isFile()){
            sourceFiles.add(fichier);
        }else{
            throw new CLIException("Entrer un nom de fichier correct!");
        }


    }

    protected void displayUsage() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
