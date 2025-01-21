package fr.ensimag.ima.pseudocode;


// Classe créée pour gérer l'adressage indirect
public class RegisterIndirect extends DAddr {
    private final Register base;
    private final Register index;

    public RegisterIndirect(Register base, Register index) {
        super();
        this.base = base;
        this.index = index;
    }

    public Register getBase() {
        return base;
    }

    public Register getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "0(" + base + ", " + index + ")";
    }
}
