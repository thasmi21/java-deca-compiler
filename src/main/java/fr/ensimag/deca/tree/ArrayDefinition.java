package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;

public class ArrayDefinition extends VariableDefinition {
    private Array array;

    public ArrayDefinition(Type type, Location location, Array array) {
        super(type, location);
        this.array = array;
    }

    public Array getArray() {
        return array;
    }
}
