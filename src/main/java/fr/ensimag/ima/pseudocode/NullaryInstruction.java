package fr.ensimag.ima.pseudocode;

import java.io.PrintStream;

/**
 * Instruction without operand.
 *
 * @author Ensimag
 * @date 01/01/2025
 */
public abstract class NullaryInstruction extends Instruction {
    @Override
    void displayOperands(PrintStream s) {
        // no operand
    }
}
