package ahunte206.Controller;

import ahunte206.DAO.FileManager;
import ahunte206.DAO.Parser;
import ahunte206.Model.Network;

public class NetworkController {

    private Network network;

    //constructor
    public NetworkController(){}

    public boolean parseNetwork(int option){
        //load and parse OSM XML
        network = Parser.parse(Integer.toString(option), getURL(option));
        if(network == null) return false;
        //store network class
        String networkFile = "networks\\" + option + ".csv";
        if(FileManager.saveNetwork(networkFile, network)){
            network = null;
            return true;
        } else{
            network = null;
            return false;
        }
    }

    public boolean loadNetwork(String filename){
        //load the network from file
        return (network = FileManager.loadNetwork(filename)) != null;
    }

    //gets URL of each map to download
    private String getURL(int option){
        if(option == 1) return "https://overpass-api.de/api/map?bbox=-4.13946,55.91910,-4.13579,55.92088";
        if(option == 2) return "https://overpass-api.de/api/map?bbox=-4.2167,55.9134,-4.1034,55.9782";
        else return "https://overpass-api.de/api/map?bbox=-4.2895,55.8491,-4.1178,55.9757";
    }

    //returns true if the network is loaded
    public boolean isNetworkLoaded(){
        return network != null;
    }

    //checks a node is in the network
    public boolean networkContainsNode(String nodeId){return network.getNodes().containsKey(nodeId);}

    //manages the shortest path calculation
    public boolean calculateSPA(String sourceNodeId, String destinationNodeId) {
        //initialise network
        network.initialiseNetwork(sourceNodeId, destinationNodeId);
        return network.calculateShortestPath(sourceNodeId, destinationNodeId);
    }

    public String buildResult() {
        String networkStats = network.getNetworkStats();
        String route = network.printRoute();
        return route + "\n\n" + networkStats;
    }
}
