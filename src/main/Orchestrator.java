package main;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Orchestrator {
    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
    public boolean finish = false;
    public Map<String, Router> routers = new HashMap<String, Router>();
    public Map<String, Packet> packets = new HashMap<String, Packet>();
    private String userInput;

    public Orchestrator(String filePath) throws IOException {
        Map<String, String[]> neighbours = new HashMap<String, String[]>();
        String line = "";
        String separator = ",";
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        int x = -1; // x+1 is count of routers
        Map<String, Integer> orderInVectors = new HashMap<String, Integer>();
        while ((line = br.readLine()) != null) {
            x++;
            String[] routerInfo = line.split(separator);
            orderInVectors.put(routerInfo[0], x);
            Router router = new Router(routerInfo[0]);
            neighbours.put(routerInfo[0], new String[10]);
            for (int i = 1; i < routerInfo.length; i += 2) {
                neighbours.get(routerInfo[0])[i - 1] = routerInfo[i];
                router.routingTable.addEntry(routerInfo[i], Integer.parseInt(routerInfo[i+1]), routerInfo[i]);
                router.directCostToNeighbours.put(routerInfo[i], Integer.parseInt(routerInfo[i+1]));
            }
            // add table entry to the router itself
            router.routingTable.addEntry(routerInfo[0], 0, routerInfo[0]);
            if (!routers.containsKey(routerInfo[0])) {
                routers.put(routerInfo[0], router);
            } else {
                System.out.println("Wrong topology, router names must be unique!\nQuiting the simulation...");
                System.exit(-1);
            }
        }
        // add table entries for all routers not neighbours
        for (Map.Entry<String, Router> routerInCheck : routers.entrySet()) {
            for (Map.Entry<String, Router> router : routers.entrySet()) {
                if (!routerInCheck.getValue().routingTable.table.containsKey(router.getKey())) {
                    routerInCheck.getValue().routingTable.addEntry(router.getKey(), Integer.MAX_VALUE, null);
                }
            }
            routerInCheck.getValue().orderOfDestinationInVectors = orderInVectors;
            routerInCheck.getValue().numberOfRouters = x + 1;
            routerInCheck.getValue().updateVector();
        }
        for (Map.Entry<String, String[]> entry : neighbours.entrySet()) {
            for (String neighbour : entry.getValue()) {
                routers.get(entry.getKey()).addNeighbour(neighbour, routers.get(neighbour));
            }
        }
    }

    public void startSimulation() throws IOException {
        System.out.println("Starting the simulation... Initial topology:");
        for (Map.Entry<String, Router> entry : routers.entrySet()) {
            showRoutingTable(entry.getKey());
        }
        System.out.println("Starting initial exchange with infoPackets, so that routers could " +
                "fill their routing tables and get ready to send packets.");
        for (int i = 0; i < routers.size(); i++) {
            tellRoutersToSendInfoPackets();
        }
        for (Map.Entry<String, Router> entry : routers.entrySet()) {
            showRoutingTable(entry.getKey());
        }
        while (!finish) {
            tellRoutersToSendInfoPackets();
            tellRoutersToSendPackets();
            System.out.println("Time unit has passed, what do you want to do? You can:\n" +
                    "finish simulation [f]\n" +
                    "see routers [routers]\n" +
                    "see packets [packets]\n" +
                    "see routing table of a router [table {router name}]\n" +
                    "see packets in a router [packets {router name}]\n" +
                    "see packet location [location {packet name}]\n" +
                    "see packet path [path {packet name}]\n" +
                    "change path cost between two routers [cost {cost value} {router name} {router name}]\n" +
                    "send a packet [send {packet name} {data} {router from} {router to}]\n" +
                    "continue simulation [continue]\n");
            boolean continueSimulation = false;
            while (!continueSimulation && !finish) {
                System.out.println("Choose action:");
                userInput = consoleReader.readLine();
                String[] args = userInput.split(" ");
                try {
                    if (args[0].equals("f")) {
                        finish = true;
                    } else if (args[0].equals("routers")) {
                        showRouters();
                    } else if (args[0].equals("packets") && args.length == 1) {
                        showPackets();
                    } else if (args[0].equals("table")) {
                        showRoutingTable(args[1]);
                    } else if (args[0].equals("packets")) {
                        showPackets(args[1]);
                    } else if (args[0].equals("location")) {
                        showPacketLocation(args[1]);
                    } else if (args[0].equals("path")) {
                        showPacketPath(args[1]);
                    } else if (args[0].equals("cost")) {
                        changeCost(Integer.parseInt(args[1]), args[2], args[3]);
                    } else if (args[0].equals("send")) {
                        sendPacket(args[1], args[2], args[3], args[4]);
                    } else if (args[0].equals("continue")) {
                        continueSimulation = true;
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Wrong command entered! Try again...");
                }
            }
        }
        System.exit(0);
    }

    private void tellRoutersToSendInfoPackets() {
        for (Map.Entry<String, Router> entry : routers.entrySet()) {
            entry.getValue().sendInfoPackets();
        }
    }

    private void tellRoutersToSendPackets() {
        for (Map.Entry<String, Router> entry : routers.entrySet()) {
            entry.getValue().sendPackets();
        }
    }

    private void showRouters() {
        System.out.println("Routers in this simulation:");
        for (Map.Entry<String, Router> entry : routers.entrySet()) {
            System.out.println(entry.getKey());
        }
    }

    private void showPackets() {
        System.out.println("Packets in this simulation:");
        for (Map.Entry<String, Packet> entry : packets.entrySet()) {
            System.out.println(entry.getKey());
        }
    }
    private void showRoutingTable(String router) {
        try {
            System.out.println("Router " + router + " routing table:");
            System.out.println("---------------------------------------");
            System.out.println("Router to     | Cost        | Next hop");
            Map<String, Map> table = routers.get(router).routingTable.table;
            for (Map.Entry<String, Map> entry : table.entrySet()) {
                System.out.println("---------------------------------------");
                System.out.println(entry.getKey() + " | " + entry.getValue().get("cost") + " | " + entry.getValue().get("nextHop"));
            }
            System.out.println("---------------------------------------");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Router does not exist!");
        }
    }

    private void showPackets(String router) {
        try {
            System.out.println("Packets in router " + router + ":");
            System.out.println("Stored packets (sent for this router):");
            for (Packet packet : routers.get(router).packetsStorage) {
                System.out.println("Packet name: " + packet.name + ", packet data: " + packet.data);
            }
            System.out.println("Ready to send packets:");
            for (Packet packet : routers.get(router).packetsToSend) {
                System.out.println("Packet name: " + packet.name + ", packet data: " + packet.data);
            }
        } catch (NullPointerException e) {
            System.out.println("Router does not exist!");
        }
    }

    private void showPacketLocation(String packet) {
        try {
            List<String> path = packets.get(packet).path;
            String location = path.get(path.size() - 1);
            System.out.println("Packet " + packet + " location: " + location);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Packet " + packet + " was not yet sent!");
        } catch (NullPointerException e) {
            System.out.println("Packet does not exist!");
        }
    }

    private void showPacketPath(String packet) {
        String pathString = "";
        for (String pathElement : packets.get(packet).path) {
            pathString += pathElement;
        }
        System.out.println("Packet " + packet + " path:" + pathString);
    }

    private void changeCost(Integer cost, String routerFrom, String routerTo) {
        for (Map.Entry<String, Router> entry : routers.entrySet()) {
            if (entry.getKey().equals(routerFrom)) {
                entry.getValue().changeDirectCostToNeighbour(routerTo, cost);
            } else if (entry.getKey().equals(routerTo)) {
                entry.getValue().changeDirectCostToNeighbour(routerFrom, cost);
            }
        }
    }

    private void sendPacket(String name, String data, String routerFrom, String routerTo) {
        if (!packets.containsKey(name)) {
            Packet packet = new Packet(name, data, routerFrom, routerTo);
            packets.put(name, packet);
            routers.get(routerFrom).receivePacket(packet);
        } else {
            System.out.println("Packet with name " + name + " already exists! Packet names must be unique!");
        }
    }
}
