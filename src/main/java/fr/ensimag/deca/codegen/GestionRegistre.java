package fr.ensimag.deca.codegen;


import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/*
* Classe pour demander un régistre libre et le liberer après si besoin
* il aura egalement une fonction pour demander de l'espace pile
* avec un compteur à jour sur le prochain espace de pile libre
* */
public final class GestionRegistre {
    private GestionRegistre() {
        throw new IllegalStateException("Utility class: On ne peut pas l'instancier");
    }
    private static int indiceRegistreGB = 0;
    private static int indiceRegistreLB = 0;
    private static int indiceRegistreSP = 0;
    private int getIndiceRegistreGB(){return indiceRegistreGB;}
    private int getIndiceRegistreLB(){return indiceRegistreLB;}
    private int getIndiceRegistreSP(){return indiceRegistreSP;}

    private void setIndiceRegistreGB(int indice) {indiceRegistreGB = indice;}
    private void setIndiceRegistreLB(int indice) {indiceRegistreLB = indice;}
    private void setIndiceRegistreSP(int indice) {indiceRegistreSP = indice;}

    public static int obtainIndiceGPandIncrement(int offset){
        indiceRegistreGB += offset;
        return indiceRegistreGB;
    }

    /*
    * methode pour obtenir le prochain registre libre à partir de 2 ou une adresse pile si impossible
    * il faut donc que j'ajoute un champ enUtilisation à mes registre ou garder
    * un dictionnaire sur mes registre libre. je pense je vais ajouer un champs
    * */
    public static GPRegister obtainFreeRegister(){
        for(int i=2; i<=16; i++){
            if(!Register.getR(i).isInUse()){
                Register.getR(i).setInUse(true);
                return Register.getR(i);
            }
        }
        indiceRegistreSP += 1;
        // il faut retourner ici nulle et on saura
        // qu'on doit utiliser une adresse pile
        return null;
    }



}
