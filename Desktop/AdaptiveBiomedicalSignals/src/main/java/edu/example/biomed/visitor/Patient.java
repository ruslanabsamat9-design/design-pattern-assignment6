package edu.example.biomed.visitor;

import java.util.List;

public class Patient implements Element {
    private final String id;
    private final List<Element> devices;

    public Patient(String id, List<Element> devices) {
        this.id = id;
        this.devices = List.copyOf(devices);
    }

    public String id() { return id; }
    public List<Element> devices() { return devices; }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPatient(this);
    }
}
