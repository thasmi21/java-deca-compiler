package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;

public class ArrayType extends Type{
    private Type dataType;
    private int dimension;

    public ArrayType(SymbolTable.Symbol name) {
        super(name);
    }

    public Type getDataType() {
        return dataType;
    }
    public void setDataType(Type dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        return otherType instanceof ArrayType;
    }

}
