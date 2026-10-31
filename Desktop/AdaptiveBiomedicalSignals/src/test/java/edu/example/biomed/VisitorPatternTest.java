package edu.example.biomed;

import edu.example.biomed.visitor.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class VisitorPatternTest {
    private Hospital sample() {
        return new Hospital("Test", List.of(
                new Ward("W1", List.of(
                        new Patient("P1", List.of(
                                new EcgChannel("ECG", 0.9, 1),
                                new PpgChannel("PPG", 0.4, 2),
                                new ProcessingDevice("Filter", 0.8, 0)))))));
    }

    @Test void averageQualityMatchesHandCalculation() {
        // (0.9 + 0.4 + 0.8) / 3 = 0.7
        assertEquals(0.7, sample().accept(new QualityVisitor()), 0.0001);
    }

    @Test void alarmCountMatchesHandCalculation() {
        assertEquals(3, sample().accept(new AlarmStatisticsVisitor()));
    }

    @Test void lowQualityFilterReturnsOnlyLowItems() {
        assertEquals(List.of("PPG"), sample().accept(new LowQualityFilterVisitor(0.7)));
    }

    @Test void jsonExportContainsNestedNames() {
        String json = sample().accept(new JsonExportVisitor());
        assertTrue(json.contains("\"hospital\":\"Test\""));
        assertTrue(json.contains("\"ward\":\"W1\""));
        assertTrue(json.contains("\"patientId\":\"P1\""));
        assertTrue(json.contains("\"type\":\"ECG\""));
    }

    @Test void emptyHospitalHasZeroQuality() {
        Hospital hospital = new Hospital("Empty", List.of());
        assertEquals(0.0, hospital.accept(new QualityVisitor()));
    }

    @Test void emptyHospitalHasNoAlarms() {
        Hospital hospital = new Hospital("Empty", List.of());
        assertEquals(0, hospital.accept(new AlarmStatisticsVisitor()));
    }
}
