package fr.ensimag.deca.codegen;

import fr.ensimag.ima.pseudocode.Operand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

public class MemoryOperand extends Operand {
    private RegisterOffset offset;

    public MemoryOperand(int offset) {
        this.offset = new RegisterOffset(offset, Register.GB);
    }

    @Override
    public String toString() {
        return offset.toString();
    }
}
