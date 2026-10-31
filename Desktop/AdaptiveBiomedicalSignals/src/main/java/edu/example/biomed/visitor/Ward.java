package edu.example.biomed.visitor;

import java.util.List;

public class Ward implements Element {
    private final String name;
    private final List<Patient> patients;

    public Ward(String name, List<Patient> patients) {
        this.name = name;
        this.patients = List.copyOf(patients);
    }

    public String name() { return name; }
    public List<Patient> patients() { return patients; }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitWard(this);
    }
}
