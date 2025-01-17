package fr.ensimag.deca;

import java.util.concurrent.Semaphore;

public class RunnableCompiler implements Runnable {

    private DecacCompiler compiler;
    private boolean n;
    private Semaphore semaphore;
    private boolean error=false;

    public void isError() {
        DecacMain.err=true;
    }

    public RunnableCompiler(DecacCompiler compiler, boolean n, Semaphore semaphore) {
        this.compiler = compiler;
        this.n = n;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        semaphore.acquireUninterruptibly();
        boolean err=compiler.compile();
        semaphore.release();
        if (n) {
            compiler.setN(true);
        }
        if(err){
            isError();
        }
    }
}
