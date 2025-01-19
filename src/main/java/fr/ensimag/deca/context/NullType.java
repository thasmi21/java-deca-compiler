package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.DVal;

/**
 *
 * @author Ensimag
 * @date 01/01/2025
 */
public class NullType extends Type {

    public NullType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean sameType(Type otherType) {
        if (otherType instanceof NullType) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }


}
