package ahunte206.DAO;

import ahunte206.Model.Edge;
import ahunte206.Model.Network;
import ahunte206.Model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Parser {

    public static Network parse(String option, String url){
        Document doc = FileManager.loadXML(option, url);
        if (doc == null) return null;
        HashMap<String, Node> nodes;
        HashMap<String, Edge> edges;

        //load dataset
        nodes = initialLoadNodes(doc);
        edges = initialLoadEdges(doc, nodes);

        //Network structure
        addEdgeReferencesToNodes(nodes, edges);
        removeDisconnectedNodes(nodes);
        modelOneWayStreets(nodes, edges);

        //reduce network
        Network network = reduceNetwork(nodes, edges);

        return new Network(nodes, edges, edges.size());
    }

    //loads nodes from document node elements
    private static HashMap<String, Node> initialLoadNodes(Document doc) {
        HashMap<String, Node> nodes = new HashMap<>();
        //create NodeList of elements
        NodeList nList = doc.getElementsByTagName("node");
        //progress-update information setup
        int progressNumber = 1;
        String progressText = "/"+ nList.getLength();
        //loop for each element in NodeList
        for(int i = 0; i < nList.getLength(); i++){
            Element e = (Element) nList.item(i);
            String id = e.getAttribute("id");
            double lat = Double.parseDouble(e.getAttribute("lat"));
            double lon = Double.parseDouble(e.getAttribute("lon"));
            //create and add new node to collection
            Node n = new Node(id, lat, lon);
            nodes.put(id, n);
            System.out.print("\rLoading nodes... " + progressNumber + progressText);
            progressNumber++;
        }
        System.out.print("\n");
        return nodes;
    }

    //loads edges from document way elements
    private static HashMap<String, Edge> initialLoadEdges(Document doc, HashMap<String, Node> nodes) {
        HashMap<String, Edge> edges = new HashMap<>();
        //incremented ID number so that each ID is unique
        int edgeIdIncrementer = 0;
        //create NodeList of ways
        NodeList nList = doc.getElementsByTagName("way");
        //load road data list of types and speed limits
        HashMap<String, Integer> roadLookupData = FileManager.loadRoadData();
        //progress-update information setup
        int progressNumber = 1;
        String progressText = "/"+ nList.getLength();
        //loop for each element in NodeList
        for(int i = 0; i < nList.getLength(); i++){
            Element e = (Element)nList.item(i);

            //get a list of tags for the way
            HashMap<String, String> tags = getEdgeTagsFromElement(e.getElementsByTagName("tag"));
            //skip way if not road or not valid road type
            if(!tags.containsKey("highway")) {
                System.out.print("\rLoading edges... " + progressNumber + progressText);
                progressNumber++;
                continue;
            }
            else if(!roadLookupData.containsKey(tags.get("highway"))){
                System.out.print("\rLoading edges... " + progressNumber + progressText);
                progressNumber++;
                continue;
            }

            String name;
            int maxSpeed;
            boolean oneway = false;
            //get name if available
            name = getName(tags);
            //get maxspeed if available
            maxSpeed = getMaxSpeed(tags, roadLookupData);
            //check if one way road
            if(tags.containsKey("oneway"))
                if (tags.get("oneway").equalsIgnoreCase("true"))
                    oneway = true;

            //create edge between nodes and populate connectedNode data
            ArrayList<String> nodeIds = getNodeIdsFromElement(e.getElementsByTagName("nd"));
            while(nodeIds.size() > 1) {
                ArrayList<String> connectedNodes = new ArrayList<>();
                connectedNodes.add(nodeIds.remove(0));
                connectedNodes.add(nodeIds.get(0));
                //calculate cost and distance
                double distance = calculateDistance(
                        nodes.get(connectedNodes.get(0)), nodes.get(connectedNodes.get(1)));
                double cost = calculateCost(distance, maxSpeed);
                Edge edge = new Edge(
                        Integer.toString(edgeIdIncrementer), connectedNodes,
                        name, maxSpeed, oneway, distance, cost);
                edges.put(Integer.toString(edgeIdIncrementer), edge);
                edgeIdIncrementer++;
            }
            System.out.print("\rLoading edges... " + progressNumber + progressText);
            progressNumber++;
        }
        System.out.print("\n");
        return edges;
    }

    //gets road name from way tags where possible
    private static String getName(HashMap<String, String> tags){
        if(tags.containsKey("name"))
            return tags.get("name");
        //if no name tag is supplied, the road reference will be returned if possible, otherwise the road will
        //be given a default name
        else return tags.getOrDefault("ref", "Unknown Road");
    }

    //gets max speed for a way or populates from lookup table
    private static int getMaxSpeed(HashMap<String, String> tags, HashMap<String, Integer> speedLimits){
        //set default speed to 30mph
        String maxSpeed = "30";
        //get maxspeed from tag if available
        if(tags.containsKey("maxspeed")) maxSpeed = tags.get("maxspeed");
        else{ //tries to estimate speed limit from road type
            String roadType = tags.get("highway");
            if(speedLimits.containsKey(roadType)) return speedLimits.get(roadType);
        }
        //remove all non numbers and return
        return Integer.parseInt(maxSpeed.replaceAll("[^0-9]", ""));
    }

    //gets the collection of tags from each way
    private static HashMap<String, String> getEdgeTagsFromElement(NodeList nList) {
        HashMap<String, String> tags = new HashMap<>();
        //for each item in the NodeList
        for(int i = 0; i < nList.getLength(); i++){
            //cast to element and get the key / value of tag
            Element e = (Element)nList.item(i);
            String k = e.getAttribute("k");
            String v = e.getAttribute("v");
            //add tag to list
            tags.put(k,v);
        }
        return tags;
    }

    //gets the collection of nodes from each way
    private static ArrayList<String> getNodeIdsFromElement(NodeList nList) {
        ArrayList<String> nodeIds = new ArrayList<>();
        //for each item in NodeList
        for(int i = 0; i < nList.getLength(); i++){
            //cast to element and get ID number
            Element e = (Element) nList.item(i);
            String ref = e.getAttribute("ref");
            //add to collection of IDs
            nodeIds.add(ref);
        }
        return nodeIds;
    }

    //add references in each node to the edges that connect to it
    private static void addEdgeReferencesToNodes(HashMap<String, Node> nodes, HashMap<String, Edge> edges) {
        int progressNumber = 1;
        String progressText = "/" + edges.values().size();
        //Loop for each edge
        for(Edge e : edges.values()) {
            //get each connected node ID, then add edge ID to that node's connected edge
            for (String nodeId : e.getConnectedNodeIds())
                nodes.get(nodeId).addConnectedEdgeId(e.getId());
            System.out.print("\rAdding edge references to nodes... " + progressNumber + progressText);
            progressNumber++;
        }
        System.out.print("\n");
    }

    //finds and removes all nodes that are not connected to any edge
    private static void removeDisconnectedNodes(HashMap<String, Node> nodes) {
        //remove node in collection if number of connected edges equals 0
        System.out.print("Removing disconnected nodes...");
        nodes.entrySet().removeIf(e->e.getValue().getConnectedEdgeIds().size() == 0);
        System.out.print("Done\n");
    }

    //remove references to one-way street edges from the wrong entry side
    private static void modelOneWayStreets(HashMap<String, Node> nodes, HashMap<String, Edge> edges) {
        int progressNumber = 1;
        String progressText = "/" + edges.size();
        //for each edge in the collection
        for (String key : edges.keySet()) {
            //if edge is a one-way street, remove edge reference from street exit
            if (edges.get(key).isOneway())
                nodes.get(edges.get(key).getOneWayExitNode()).removeConnectedEdgeId(key);
            System.out.print("\rModeling one-way streets... " + progressNumber + progressText);
            progressNumber++;
        }
        System.out.print("\n");
    }

    //calculates the distance of an edge
    private static double calculateDistance(Node n1, Node n2){

        double lat1 = n1.getLat();
        double lat2 = n2.getLat();
        double lon1 = n1.getLon();
        double lon2 = n2.getLon();

        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance;
        distance = R * c;
        return distance;
    }

    //calculates the cost of an edge
    private static double calculateCost(double distance, int maxSpeed) {
        return (1000 * distance) / (maxSpeed * 0.44704);
    }

    //combines sections of roads between junction into one edge
    private static Network reduceNetwork(HashMap<String, Node> nodes, HashMap<String, Edge> edges) {
        int lastEdgeId = edges.size();
        //get a list of nodes to iterate
        ArrayList<String> listOfNodes = new ArrayList<>(nodes.keySet());
        //for each node in network
        for(String nodeId : listOfNodes){
            //if node has two connected edges
            if(nodes.get(nodeId).getConnectedEdgeIds().size() == 2){
                String edge1Id = nodes.get(nodeId).getConnectedEdgeIds().get(0);
                String edge2Id = nodes.get(nodeId).getConnectedEdgeIds().get(1);
                //if edge road names are the same, replace the node and edges with new edge
                if(edges.get(edge2Id).getName().equals(edges.get(edge1Id).getName())){
                    //get outer node IDs
                    String outerNode1 = edges.get(edge1Id).getOtherConnectedNode(nodeId);
                    String outerNode2 = edges.get(edge2Id).getOtherConnectedNode(nodeId);
                    //create new edge
                    ArrayList<String> connectedNodeIds = new ArrayList<>();
                    connectedNodeIds.add(outerNode1);
                    connectedNodeIds.add(outerNode2);
                    int maxSpeed = edges.get(edge1Id).getMaxSpeed();
                    boolean oneway = edges.get(edge1Id).isOneway();
                    String name = edges.get(edge1Id).getName();
                    double distance = edges.get(edge1Id).getDistance()
                            + edges.get(edge2Id).getDistance();
                    double cost = edges.get(edge1Id).getCost()
                            + edges.get(edge2Id).getCost();
                    Edge newEdge = new Edge(Integer.toString(lastEdgeId), connectedNodeIds,
                            name, maxSpeed, oneway, distance, cost);
                    lastEdgeId++;
                    edges.put(newEdge.getId(), newEdge);
                    //remove edge references in outer nodes
                    nodes.get(outerNode1).removeConnectedEdgeId(edge1Id);
                    nodes.get(outerNode2).removeConnectedEdgeId(edge2Id);
                    //add new edge references
                    nodes.get(outerNode1).addConnectedEdgeId(newEdge.getId());
                    nodes.get(outerNode2).addConnectedEdgeId(newEdge.getId());
                    //delete inner node
                    nodes.remove(nodeId);
                    //delete old edges
                    edges.remove(edge1Id);
                    edges.remove(edge2Id);
                }
            }
        }
        return new Network(nodes, edges, lastEdgeId);
    }
}
