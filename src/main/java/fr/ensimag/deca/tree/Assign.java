package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.GestionRegistre;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.WINT;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl06
 * @date 01/01/2025
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight;
        if(getRightOperand() instanceof ArrayLiteral && ((ArrayLiteral) getRightOperand()).implicitDeclaration()) {
            getRightOperand().setType(typeLeft);
            getRightOperand().verifyExpr(compiler, localEnv, currentClass);
            return typeLeft;
        }
        else {
            typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
            if (typeRight.sameType(typeLeft) || ((typeLeft.isFloat() && typeRight.isInt() || typeLeft.isInt()) && (typeRight.isFloat()))) {
                if (typeRight.sameType(typeLeft)) {
                    this.setType(typeLeft);
                    return typeLeft;
                }
                if (typeLeft.isFloat()) {
                    this.setType(typeLeft);
                    return typeLeft;
                }
                this.setType(typeRight);
                return typeRight;
            } else {
                throw new ContextualError("Les deux opérandes ne sont pas du même type : " + typeLeft + typeRight, getLocation());
            }
        }
    }

    /*@Override
    public void evalueExpr(DecacCompiler compiler, GPRegister register) {
        // On commence par récupérer le registre dans lequel
        // stocker la valeur, qui est l'opérand gauche
        DAddr regAdresse = ((AbstractIdentifier)getLeftOperand()).getExpDefinition().getOperand();
        // On prend ensuite un registre libre
        GPRegister reg = GestionRegistre.obtainFreeRegister();
        // On y stock la valeur contenue dans l'opérand droite
        getRightOperand().evalueExpr(compiler, reg);
        // On stock la valeur mise dans le registre choisi a l'adresse de l'operand gauche
        compiler.addInstruction(new STORE(reg, regAdresse));
        reg.setInUse(false);
    }*/

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        //gerer le cas ou ce serait un entier egalement
        DAddr addresse = ((AbstractIdentifier)getLeftOperand()).getExpDefinition().getOperand();
        GPRegister r2 = Register.R1; // On prend le registre R1 car on va print ensuite
        getRightOperand().evalueExpr(compiler, r2 );
        compiler.addInstruction(new STORE(r2, addresse));
        //compiler.addInstruction(new WINT());
        getLeftOperand().codeGenPrint(compiler);
    }
        @Override
        public void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                ClassDefinition currentClass, Type returnType) throws ContextualError {
            // Vérifie le type de l'affectation
            Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
            Type typeRight;
            if (getRightOperand() instanceof ArrayLiteral && ((ArrayLiteral) getRightOperand()).implicitDeclaration()) {
                getRightOperand().setType(typeLeft);
                getRightOperand().verifyExpr(compiler, localEnv, currentClass);
                this.setType(typeLeft);
            } else {
                typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
                // Vérifie la compatibilité des types
                if (typeRight.sameType(typeLeft) || (typeLeft.isFloat() && typeRight.isInt())) {
                    if (typeLeft.isFloat() && typeRight.isInt()) {
                        // Promotion implicite de int vers float
                        setRightOperand(new ConvFloat(getRightOperand()));
                    }
                    this.setType(typeLeft);
                } else {
                    throw new ContextualError(
                            "Erreur de type : impossible d'affecter une expression de type " +
                                    typeRight + " à une variable de type " + typeLeft,
                            getLocation()
                    );
                }
            }
        }


    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        // On prend un registre libre
        //compiler.addInstruction(new STORE(Register.getR(3), getLeftOperand().getMemoryLocation()));
        // On commence par récupérer le registre dans lequel
        // stocker la valeur, qui est l'opérand gauche
        DAddr regAdresse;
        // On prend ensuite un registre libre
        GPRegister reg = GestionRegistre.obtainFreeRegister();
        // On y stock la valeur contenue dans l'opérand droite
        getRightOperand().evalueExpr(compiler, reg);
        if(getLeftOperand() instanceof GetArray){
            DAddr idAdresse=((GetArray) getLeftOperand()).codeGenAddressOf(compiler);
            compiler.addInstruction(new STORE(reg, idAdresse));
        }
        else{
            regAdresse = ((AbstractIdentifier)getLeftOperand()).getExpDefinition().getOperand();
            compiler.addInstruction(new STORE(reg, regAdresse));
        }
        // On stock la valeur mise dans le registre choisi a l'adresse de l'operand gauche

        reg.setInUse(false);
    }




}
