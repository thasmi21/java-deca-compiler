package fr.ensimag.deca.context;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.*;

/**
 * Initialisation d'une variable non ajoutée à env_exp
 *
 * @author gl06
 * @date 06/01/2025
 */

public class exp_ID {
   public static void main(String[] args) {
      EnvironmentExp Env_Test = new EnvironmentExp(null);

      DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
      Identifier id = new Identifier(compiler.createSymbol("int"));
      Identifier var = new Identifier(compiler.createSymbol("x"));
      NoInitialization ini = new NoInitialization();
      DeclVar x_test = new DeclVar(id, var, ini);

      // Doit etre refuse.
      if(Env_Test.get(var.getName()) == null){
         System.out.println("Succès");
      }
      else{
         System.out.println("echec");
      }
   }
}
