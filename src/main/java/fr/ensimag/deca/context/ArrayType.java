package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractIdentifier;

public class ArrayType extends Type{
    private Type dataType;
    private final int dimensions;

    public ArrayType(Symbol fullSymbol, Type dataType, int dimensions) {
        super(fullSymbol);
        this.dataType = dataType;
        this.dimensions = dimensions;
    }

    public Type getDataType() {
        return dataType;
    }
    public void setDataType(Type dataType) {
        this.dataType = dataType;
    }

    public int getDimensions() {
        return dimensions;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {

        return otherType instanceof ArrayType && ((ArrayType) otherType).dataType == dataType && ((ArrayType) otherType).dimensions == dimensions;
    }

}
