package backend.mips.assembly.pseudo;

import backend.mips.Register;

public class MarsLa extends MipsMars {
    private final Register rd;
    private final String label;

    public MarsLa(Register regDestination, String label) {
        this.rd = regDestination;
        this.label = label;
    }

    @Override
    public String toString() {
        // la $t1, label
        return "la " + rd + ", " + label;
    }
}
