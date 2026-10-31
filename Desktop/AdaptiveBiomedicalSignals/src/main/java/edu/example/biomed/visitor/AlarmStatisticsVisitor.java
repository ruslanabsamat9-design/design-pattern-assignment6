package edu.example.biomed.visitor;

public class AlarmStatisticsVisitor implements Visitor<Integer> {
    private int total;

    @Override public Integer visitHospital(Hospital h) {
        total = 0;
        h.wards().forEach(w -> w.accept(this));
        return total;
    }
    @Override public Integer visitWard(Ward w) {
        w.patients().forEach(p -> p.accept(this));
        return total;
    }
    @Override public Integer visitPatient(Patient p) {
        p.devices().forEach(d -> d.accept(this));
        return total;
    }
    @Override public Integer visitEcgChannel(EcgChannel c) { total += c.alarms(); return total; }
    @Override public Integer visitPpgChannel(PpgChannel c) { total += c.alarms(); return total; }
    @Override public Integer visitEegChannel(EegChannel c) { total += c.alarms(); return total; }
    @Override public Integer visitProcessingDevice(ProcessingDevice d) { total += d.alarms(); return total; }
}
