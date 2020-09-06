package main;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable {
    public Map<String, Map> table = new HashMap();

    public void addEntry(String routerTo, Integer cost, String nextHop) {
        if (!table.containsKey(routerTo)) {
            table.put(routerTo, new HashMap());
        }
        table.get(routerTo).put("cost", cost);
        table.get(routerTo).put("nextHop", nextHop);
    }

    public String getNextHop(String routerTo) {
        return (String) table.get(routerTo).get("nextHop");
    }

}
