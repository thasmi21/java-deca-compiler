package fr.ensimag.ima.pseudocode;

/**
 * Operand representing a register indirection with offset, e.g. 42(R3).
 *
 * @author Ensimag
 * @date 01/01/2025
 */
public class RegisterOffset extends DAddr {
    public int getOffset() {
        return offset;
    }
    public Register getRegister() {
        return register;
    }
    private final int offset;
    private final Register register;
    public RegisterOffset(int offset, Register register) {
        super();
        this.offset = offset;
        this.register = register;
    }

    public RegisterOffset(DVal dval, Register register) {
        super();
        if(dval instanceof ImmediateInteger) {
            this.offset = ((ImmediateInteger) dval).getValue();
        }
        else {
            throw new IllegalArgumentException("dval is not an integer type");
        }
        this.register = register;
    }
    @Override
    public String toString() {
        return offset + "(" + register + ")";
    }
}
