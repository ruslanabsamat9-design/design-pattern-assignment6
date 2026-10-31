package edu.example.biomed.visitor;

import java.util.List;

public class Hospital implements Element {
    private final String name;
    private final List<Ward> wards;

    public Hospital(String name, List<Ward> wards) {
        this.name = name;
        this.wards = List.copyOf(wards);
    }

    public String name() { return name; }
    public List<Ward> wards() { return wards; }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitHospital(this);
    }
}
