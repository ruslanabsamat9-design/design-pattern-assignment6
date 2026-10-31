package edu.example.biomed.visitor;

public class EcgChannel implements Element {
    private final String name;
    private final double quality;
    private final int alarms;

    public EcgChannel(String name, double quality, int alarms) {
        this.name = name;
        this.quality = quality;
        this.alarms = alarms;
    }

    public String name() { return name; }
    public double quality() { return quality; }
    public int alarms() { return alarms; }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitEcgChannel(this);
    }
}
