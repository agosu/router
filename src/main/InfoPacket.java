package main;

public class InfoPacket {
    public String routerFrom;
    public String routerTo;
    public int[] vector;

    public InfoPacket(String routerFrom, String routerTo, int[] vector, int numberOfRouters) {
        this.routerFrom = routerFrom;
        this.routerTo = routerTo;
        this.vector = new int[numberOfRouters];
        for (int i = 0; i < vector.length; i++) {
            this.vector[i] = vector[i];
        }
    }
}
