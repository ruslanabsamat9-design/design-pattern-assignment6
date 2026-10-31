package edu.example.biomed.visitor;

public class QualityVisitor implements Visitor<Double> {
    private double qualitySum;
    private int count;

    private double average() { return count == 0 ? 0.0 : qualitySum / count; }
    private void add(double quality) { qualitySum += quality; count++; }

    @Override public Double visitHospital(Hospital h) {
        qualitySum = 0; count = 0;
        h.wards().forEach(w -> w.accept(this));
        return average();
    }
    @Override public Double visitWard(Ward w) {
        w.patients().forEach(p -> p.accept(this));
        return average();
    }
    @Override public Double visitPatient(Patient p) {
        p.devices().forEach(d -> d.accept(this));
        return average();
    }
    @Override public Double visitEcgChannel(EcgChannel c) { add(c.quality()); return average(); }
    @Override public Double visitPpgChannel(PpgChannel c) { add(c.quality()); return average(); }
    @Override public Double visitEegChannel(EegChannel c) { add(c.quality()); return average(); }
    @Override public Double visitProcessingDevice(ProcessingDevice d) { add(d.quality()); return average(); }
}
