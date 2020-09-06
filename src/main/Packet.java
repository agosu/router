package main;

import java.util.ArrayList;
import java.util.List;

public class Packet {
    public String name;
    public String routerFrom;
    public String nextHop;
    public String routerTo;
    public String data;
    public List<String> path;

    public Packet(String name, String data, String routerFrom, String routerTo) {
        this.name = name;
        this.routerFrom = routerFrom;
        this.routerTo = routerTo;
        this.data = data;
        this.path = new ArrayList<String>();
    }
}
