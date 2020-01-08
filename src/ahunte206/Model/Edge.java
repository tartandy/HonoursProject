package ahunte206.Model;

import java.util.ArrayList;

public class Edge {

    private String id;
    private ArrayList<String> connectedNodeIds;
    private String name;
    private int maxSpeed;
    private double distance;
    private double cost;
    private boolean oneway;

    //constructor
    public Edge(String id, ArrayList<String> connectedNodeIds, String name, int maxSpeed, boolean oneway, double distance, double cost) {
        this.id = id;
        this.connectedNodeIds = connectedNodeIds;
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.oneway = oneway;
        this.distance = distance;
        this.cost = cost;
    }

    //getters and setters
    public String getId() {
        return id;
    }
    public ArrayList<String> getConnectedNodeIds() {
        return connectedNodeIds;
    }
    public String getOneWayExitNode() { return connectedNodeIds.get(1); }
    public String getOtherConnectedNode(String nodeId){
        for(String id : connectedNodeIds)
            if(!id.equals(nodeId)) return id;
        return null;
    }
    public void setConnectedNodeIds(ArrayList<String> connectedNodeIds) {
        this.connectedNodeIds = connectedNodeIds;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getMaxSpeed() {
        return maxSpeed;
    }
    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public double getCost() {
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean isOneway() {
        return oneway;
    }
    //to string

    @Override
    public String toString() {
        return "Edge{" +
                "id='" + id + '\'' +
                ", connectedNodeIds=" + connectedNodeIds +
                ", name='" + name + '\'' +
                ", maxSpeed=" + maxSpeed +
                ", distance=" + distance +
                ", cost=" + cost +
                ", oneway=" + oneway +
                '}';
    }

    public String toFile(){
        StringBuilder output = new StringBuilder(id + ",");
        output.append(name).append(",");
        output.append(maxSpeed).append(",");
        output.append(oneway).append(",");
        for(String nodeId : connectedNodeIds){
            output.append(nodeId).append(";");
        }
        output = new StringBuilder(output.toString().substring(0, output.toString().length() - 1));
        output.append(",").append(distance).append(",");
        output.append(cost);
        return output.toString();
    }
}
