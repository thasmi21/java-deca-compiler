package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * @author gl06
 * @date 01/01/2025
 */
public class Initialization extends AbstractInitialization {

    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;

        //bonne positionnement de la Localisation de l'initialisation
        this.setLocation(expression.getLocation());
    }

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeExpr = expression.verifyExpr(compiler, localEnv, currentClass);
        if (!typeExpr.sameType(t)) {
            throw new ContextualError("Types incompatibles dans l'initialisation ! (attendu "+t+" ), (trouvé "+typeExpr+")",expression.getLocation());
        }
    }


    @Override
    public void decompile(IndentPrintStream s) {
        expression.decompile(s);
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }
}
