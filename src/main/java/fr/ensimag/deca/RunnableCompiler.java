package fr.ensimag.deca;

public class RunnableCompiler implements Runnable {

    private DecacCompiler compiler;
    private boolean n;

    RunnableCompiler(DecacCompiler compiler, boolean n) {
        this.compiler = compiler;
        this.n = n;
    }

    @Override
    public void run() {
        boolean err=compiler.compile();
        if (n) {
            compiler.setN(true);
        }
        if(err){
            System.exit(1);
        }
        System.exit(0);
    }
}
