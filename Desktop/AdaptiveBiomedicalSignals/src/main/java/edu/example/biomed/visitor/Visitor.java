package edu.example.biomed.visitor;

public interface Visitor<R> {
    R visitHospital(Hospital hospital);
    R visitWard(Ward ward);
    R visitPatient(Patient patient);
    R visitEcgChannel(EcgChannel channel);
    R visitPpgChannel(PpgChannel channel);
    R visitEegChannel(EegChannel channel);
    R visitProcessingDevice(ProcessingDevice device);
}
