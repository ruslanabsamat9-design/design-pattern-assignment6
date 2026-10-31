package edu.example.biomed.visitor;

import java.util.ArrayList;
import java.util.List;

public class LowQualityFilterVisitor implements Visitor<List<String>> {
    private final double threshold;
    private final List<String> lowQuality = new ArrayList<>();

    public LowQualityFilterVisitor(double threshold) {
        this.threshold = threshold;
    }

    @Override public List<String> visitHospital(Hospital h) {
        lowQuality.clear();
        h.wards().forEach(w -> w.accept(this));
        return List.copyOf(lowQuality);
    }
    @Override public List<String> visitWard(Ward w) {
        w.patients().forEach(p -> p.accept(this));
        return List.copyOf(lowQuality);
    }
    @Override public List<String> visitPatient(Patient p) {
        p.devices().forEach(d -> d.accept(this));
        return List.copyOf(lowQuality);
    }
    private void check(String name, double quality) {
        if (quality < threshold) lowQuality.add(name);
    }
    @Override public List<String> visitEcgChannel(EcgChannel c) { check(c.name(), c.quality()); return List.copyOf(lowQuality); }
    @Override public List<String> visitPpgChannel(PpgChannel c) { check(c.name(), c.quality()); return List.copyOf(lowQuality); }
    @Override public List<String> visitEegChannel(EegChannel c) { check(c.name(), c.quality()); return List.copyOf(lowQuality); }
    @Override public List<String> visitProcessingDevice(ProcessingDevice d) { check(d.name(), d.quality()); return List.copyOf(lowQuality); }
}
