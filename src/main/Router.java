package main;

import java.util.*;

public class Router {
    public final String name;
    public RoutingTable routingTable;
    public List<Packet> packetsStorage;
    public List<Packet> packetsToSend;
    public List<Packet> processedPackets;
    public Map<String, Router> neighbours;
    public Map<String, Integer> directCostToNeighbours;
    public int numberOfRouters;
    public int[] vector;
    public Map<String, int[]> neighbourVectors = new HashMap<String, int[]>();
    public Map<String, Integer> orderOfDestinationInVectors;

    public Router(String name) {
        this.name = name;
        routingTable = new RoutingTable();
        packetsStorage = new ArrayList<Packet>();
        packetsToSend = new ArrayList<Packet>();
        processedPackets = new ArrayList<Packet>();
        neighbours = new HashMap<String, Router>();
        directCostToNeighbours = new HashMap<String, Integer>();
    }

    public void updateVector() {
        if (vector == null)
            vector = new int[numberOfRouters];
        for (Map.Entry<String, Map> destinationEntry : routingTable.table.entrySet()) {
            vector[orderOfDestinationInVectors.get(destinationEntry.getKey())] = (Integer) destinationEntry.getValue().get("cost");
        }
    }

    public void addNeighbour(String name, Router neighbour) {
        neighbours.put(name, neighbour);
    }

    public void receivePacket(Packet packet) {
        packet.path.add(name);
        if (packet.routerTo.equals(name)) {
            packetsStorage.add(packet);
        } else {
            packet.nextHop = routingTable.getNextHop(packet.routerTo);
            if (packet.routerFrom.equals(name)) {
                packetsToSend.add(packet);
            } else {
                processedPackets.add(packet);
            }
        }
    }

    public void sendPackets() {
        for (Packet packet : packetsToSend) {
            neighbours.get(packet.nextHop).receivePacket(packet);
        }
        packetsToSend.clear();
        packetsToSend.addAll(processedPackets);
        processedPackets.clear();
    }

    public void receiveInfoPacket(InfoPacket infoPacket) {
        neighbourVectors.put(infoPacket.routerFrom, infoPacket.vector);
        updateRoutingTable();
        updateVector();
    }

    private void updateRoutingTable() {
        for (Map.Entry<String, Map> entry : routingTable.table.entrySet()) {
            String currentDestination = entry.getKey();
            if (!currentDestination.equals(name)) {
                Map<String, Integer> costs = new HashMap<String, Integer>();
                for (Map.Entry<String, int[]> nVector : neighbourVectors.entrySet()) {
                    String neighbour = nVector.getKey();
                    Integer costToCheckedNeighbour = directCostToNeighbours.get(neighbour);
                    Integer cost = nVector.getValue()[orderOfDestinationInVectors.get(currentDestination)];
                    if (cost != Integer.MAX_VALUE && costToCheckedNeighbour != Integer.MAX_VALUE) {
                        if (neighbours.containsKey(currentDestination)) {
                            if (directCostToNeighbours.get(currentDestination) < costToCheckedNeighbour + cost) {
                                costs.put(currentDestination, directCostToNeighbours.get(currentDestination));
                            } else {
                                costs.put(neighbour, costToCheckedNeighbour + cost);
                            }
                        } else {
                            costs.put(neighbour, costToCheckedNeighbour + cost);
                        }
                    } else {
                        if (neighbours.containsKey(currentDestination)) {
                            if (directCostToNeighbours.get(currentDestination) < cost) {
                                costs.put(currentDestination, directCostToNeighbours.get(currentDestination));
                            } else {
                                costs.put(neighbour, cost);
                            }
                        } else {
                            costs.put(neighbour, cost);
                        }
                    }
                }
                for (Map.Entry<String, Integer> e : costs.entrySet()) {
                    if (e.getValue() < 0) {
                        costs.put(e.getKey(), Integer.MAX_VALUE);
                    }
                }
                Integer minCost = Collections.min(costs.values());
                String nextHop = "blaaaah errrrorr";
                for (Map.Entry<String, Integer> min : costs.entrySet()) {
                    if (min.getValue() == minCost) {
                        nextHop = min.getKey();
                        break;
                    }
                }
                routingTable.addEntry(currentDestination, minCost, nextHop);
            }
        }
    }

    public void changeDirectCostToNeighbour(String neighbour, Integer newCost) {
        directCostToNeighbours.put(neighbour, newCost);
        updateRoutingTable();
        updateVector();
    }

    public void sendInfoPackets() {
        for (Map.Entry<String, Router> entry : neighbours.entrySet()) {
            if (entry.getValue() != null) {
                int[] vectorToSend = new int[numberOfRouters];
                for (int i = 0; i < vector.length; i++) {
                    vectorToSend[i] = vector[i];
                }
                entry.getValue().receiveInfoPacket(new InfoPacket(name, entry.getKey(), vectorToSend, numberOfRouters));
            }
        }
    }
}
