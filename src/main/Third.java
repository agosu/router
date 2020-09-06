package main;

import java.io.IOException;

public class Third {
    public static void main(String[] args) throws IOException {
        new Orchestrator("C:\\src\\Third\\src\\main\\resources\\topology.csv").startSimulation();
    }
}
