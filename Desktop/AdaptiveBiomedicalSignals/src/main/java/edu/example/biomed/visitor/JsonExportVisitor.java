package edu.example.biomed.visitor;

import java.util.stream.Collectors;

public class JsonExportVisitor implements Visitor<String> {
    @Override public String visitHospital(Hospital h) {
        return "{\"hospital\":\"" + escape(h.name()) + "\",\"wards\":["
                + h.wards().stream().map(w -> w.accept(this)).collect(Collectors.joining(",")) + "]}";
    }
    @Override public String visitWard(Ward w) {
        return "{\"ward\":\"" + escape(w.name()) + "\",\"patients\":["
                + w.patients().stream().map(p -> p.accept(this)).collect(Collectors.joining(",")) + "]}";
    }
    @Override public String visitPatient(Patient p) {
        return "{\"patientId\":\"" + escape(p.id()) + "\",\"devices\":["
                + p.devices().stream().map(d -> d.accept(this)).collect(Collectors.joining(",")) + "]}";
    }
    private String leaf(String type, String name, double quality, int alarms) {
        return "{\"type\":\"" + type + "\",\"name\":\"" + escape(name)
                + "\",\"quality\":" + quality + ",\"alarms\":" + alarms + "}";
    }
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    @Override public String visitEcgChannel(EcgChannel c) { return leaf("ECG", c.name(), c.quality(), c.alarms()); }
    @Override public String visitPpgChannel(PpgChannel c) { return leaf("PPG", c.name(), c.quality(), c.alarms()); }
    @Override public String visitEegChannel(EegChannel c) { return leaf("EEG", c.name(), c.quality(), c.alarms()); }
    @Override public String visitProcessingDevice(ProcessingDevice d) { return leaf("Device", d.name(), d.quality(), d.alarms()); }
}
