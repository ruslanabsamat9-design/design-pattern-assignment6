package edu.example.biomed.visitor;

public interface Element {
    <R> R accept(Visitor<R> visitor);
}
