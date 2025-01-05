package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import static fr.ensimag.deca.tree.DeclVar.getBaseType;
import static fr.ensimag.deca.tree.DeclVar.extractBaseType;

/**
 *
 * @author Ensimag
 * @date 01/01/2025
 */
public class IntType extends Type {

    public IntType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isInt() {
        return true;
    }


    @Override
    public boolean sameType(Type otherType) {
        return otherType instanceof IntType;
    }
}
