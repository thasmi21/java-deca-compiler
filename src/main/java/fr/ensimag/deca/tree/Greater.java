package fr.ensimag.deca.tree;


/**
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Greater extends AbstractOpIneq {

    public Greater(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return ">";
    }

}
