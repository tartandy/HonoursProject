package ahunte206.Model;

import java.util.ArrayList;

public class Node {

    private String id;
    private ArrayList<String> connectedEdgeIds;
    private double lat;
    private double lon;
    private double costFromSource;
    private String previousNodeId;

    //constructors
    public Node(String id, double lat, double lon){
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.connectedEdgeIds = new ArrayList<>();
    }

    public Node(String id, ArrayList<String> connectedEdgeIds){
        this.id = id;
        this.connectedEdgeIds = connectedEdgeIds;
    }
    //getters and setters

    public String getId() {
        return id;
    }
    public ArrayList<String> getConnectedEdgeIds() {
        return connectedEdgeIds;
    }
    public void addConnectedEdgeId(String edgeId){connectedEdgeIds.add(edgeId);}
    public void removeConnectedEdgeId(String edgeId){connectedEdgeIds.remove(edgeId);}
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public double getCostFromSource() {
        return costFromSource;
    }
    public void setCostFromSource(double costFromSource) {
        this.costFromSource = costFromSource;
    }
    public String getPreviousNodeId() {
        return previousNodeId;
    }
    public void setPreviousNodeId(String previousNodeId) {
        this.previousNodeId = previousNodeId;
    }

    //to string
    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", connectedEdgeIds=" + connectedEdgeIds +
                ", lat=" + lat +
                ", lon=" + lon +
                ", costFromSource=" + costFromSource +
                ", previousNodeId='" + previousNodeId + '\'' +
                '}';
    }

    public String toFile(){
        StringBuilder output = new StringBuilder(id + ",");
        for(String edgeId : connectedEdgeIds){
            output.append(edgeId).append(";");
        }
        return output.toString().substring(0, output.toString().length() - 1);
    }
}
