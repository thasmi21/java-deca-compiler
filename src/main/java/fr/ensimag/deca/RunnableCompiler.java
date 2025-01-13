package fr.ensimag.deca;

public class RunnableCompiler implements Runnable {

    private DecacCompiler compiler;

    RunnableCompiler(DecacCompiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public void run() {
        if(compiler.compile()){
            System.exit(1);
        }
        System.exit(0);
    }
}
