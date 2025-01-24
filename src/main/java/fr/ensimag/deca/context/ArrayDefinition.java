package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Array;
import fr.ensimag.deca.tree.Initialization;
import fr.ensimag.deca.tree.Location;

public class ArrayDefinition extends VariableDefinition {
    private Initialization init;

    public ArrayDefinition(Type type, Location location, Initialization array) {
        super(type, location);
        this.init = array;
    }

    public Initialization getInitialization() {
        return init;
    }
}
