package ahunte206.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;

public class Network {

    private HashMap<String, Node> nodes;
    private HashMap<String, Edge> edges;
    private int lastEdgeId;
    private PriorityQueue<Entry> priorityQueue;
    private double calculationTime;
    private String sourceNodeId;
    private String destinationNodeId;

    public Network(HashMap<String, Node> nodes, HashMap<String, Edge> edges, int lastEdgeId) {
        this.nodes = nodes;
        this.edges = edges;
        this.lastEdgeId = lastEdgeId;
    }

    //getters
    public HashMap<String, Node> getNodes() {
        return nodes;
    }
    public HashMap<String, Edge> getEdges() {
        return edges;
    }

    //gets all neighbouring nodes for a node
    private ArrayList<Node> getNeighbourNodes(String nodeId){
        ArrayList<Node> neighbourNodes = new ArrayList<>();
        //get a nodes connected edges
        for(String connectedEdgeId : nodes.get(nodeId).getConnectedEdgeIds())
            //get an edges connected nodes
            for(String neighbourNodeId : edges.get(connectedEdgeId).getConnectedNodeIds())
                //if nodeID is not source node then add to neighbour node
                if(!neighbourNodeId.equals(nodeId)) neighbourNodes.add(nodes.get(neighbourNodeId));
        return neighbourNodes;
    }

    //gets edge that connected two nodes together
    private Edge getConnectingEdge(Node n1, Node n2){
        for(String edgeId : n1.getConnectedEdgeIds())
            if(n2.getConnectedEdgeIds().contains(edgeId)) return edges.get(edgeId);
        return null;
    }

    //get next valid edge ID then increment
    public int getNextEdgeId(){
        int output = lastEdgeId;
        lastEdgeId++;
        return output;
    }

    //initialises the network for the shortest path calculation
    public void initialiseNetwork(String sourceNode, String destinationNode) {
        //set source and destination nodes
        sourceNodeId = sourceNode;
        destinationNodeId = destinationNode;
        priorityQueue = new PriorityQueue<>();
        //sets the distance for each node to max
        for(Node n : nodes.values()) n.setCostFromSource(Double.MAX_VALUE);
        //sets source node distance to 0
        nodes.get(sourceNode).setCostFromSource(0);
        //build priority queue
        for (Node n : nodes.values()) priorityQueue.add(new Entry(n.getId(), n.getCostFromSource()));
    }

    //calculate shortest path between two points
    public boolean calculateShortestPath(String sourceNodeId, String destinationNodeId) {
        boolean solved = false;
        System.out.print("Calculating the shortest path...");
        long startTime = System.nanoTime();
        //loop until shortest path has been found
        do{
            if(priorityQueue.size() == 0) return false;
            //get the lowest cost unsettled node
            Entry entry = priorityQueue.poll();
            //if destination node found then stop loop
            if(entry.getKey().equals(destinationNodeId))solved = true;
            else{
                Node n1 = nodes.get(entry.getKey());
                //for each node that neighbours this node
                for(Node n2 : getNeighbourNodes(n1.getId())){
                    //get the connecting edge
                    Edge e = getConnectingEdge(n1, n2);
                    assert e != null;
                    //calculate cost to the outer node
                    double costToNode2 = n1.getCostFromSource() + e.getCost();
                    //if this path is the fastest path to that node
                    if(costToNode2 < n2.getCostFromSource()){
                        //set the cost from source and previous node ID
                        n2.setCostFromSource(costToNode2);
                        n2.setPreviousNodeId(n1.getId());
                        //update the priority queue
                        priorityQueue.remove(getItemFromQueue(n2.getId()));
                        priorityQueue.add(new Entry(n2.getId(), n2.getCostFromSource()));
                    }
                }
            }
        } while(!solved);
        long endTime = System.nanoTime();
        calculationTime = (double)(endTime - startTime) / 1000000000; //in seconds
        System.out.print("\r");
        return true;
    }

    //gets an entry from the priority queue
    private Entry getItemFromQueue(String id) {
        for (Entry e : priorityQueue) {
            if (e.getKey().equals(id)) return e;
        }
        return null;
    }

    //builds the shortest path
    public Stack<Node> getShortestPath() {
        Stack<Node> shortestPath = new Stack<>();
        Node node = nodes.get(destinationNodeId);
        shortestPath.push(node);
        boolean loop = true;
        do{
            node = nodes.get(node.getPreviousNodeId());
            shortestPath.push(node);
            if(node.getId().equals(sourceNodeId)) loop = false;
        } while(loop);
        return shortestPath;
    }

    //gets all the SPA/Network stats after calculation
    public String getNetworkStats() {
        String output =  "Total: nodes=" + nodes.size() + ", edges=" + edges.size() + "\n";
        output += "Evaluated: nodes=" + getTotalNodesEvaluated() + "\n";
        int time = (int) nodes.get(destinationNodeId).getCostFromSource();
        output += "Calculation time: " + new BigDecimal(calculationTime).setScale(3, RoundingMode.HALF_UP).doubleValue() + "s\n\n";
        output += "Travel time: " + (int)Math.floor(time/60) + "m" + time%60 + "s\n";
        output += "Travel distance: " + new BigDecimal(getDistance()).setScale(1,RoundingMode.HALF_UP).doubleValue() + " km\n";
        return output;
    }


    //counts the total number of nodes evaluated by the network
    private int getTotalNodesEvaluated(){
        int numberEvaluated = 0;
        for(Node n : nodes.values())
            if(n.getPreviousNodeId() != null) numberEvaluated++;
        return numberEvaluated;
    }

    //gets the distance of the shortest path
    private double getDistance(){
        double distance = 0;
        Stack<Node> shortestPath = getShortestPath();
        Node n1, n2;
        Edge e;
        //total up the distance of all edges in the shortest path
        do{
            n1 = shortestPath.pop();
            n2 = shortestPath.peek();
            e = getConnectingEdge(n1, n2);
            assert e != null;
            distance += e.getDistance();
        }while(shortestPath.size() > 1);
        return distance;
    }

    public String printRoute() {
        ArrayList<String> roads = new ArrayList<>();
        Stack<Node> shortestPath = getShortestPath();
        Node n1, n2;
        Edge e;
        //builds and returns the road names of the fastest route
        do{
            n1 = shortestPath.pop();
            n2 = shortestPath.peek();
            e = getConnectingEdge(n1, n2);
            assert e != null;
            if(roads.size()>0)
                if(roads.get(roads.size() - 1).equals(e.getName()))
                    continue;
            roads.add(e.getName());
        }while(shortestPath.size() > 1);
        StringBuilder output = new StringBuilder("\n\nThe shortest path is along the following roads:\n");
        for(String road : roads){
            output.append(road).append("\n");
        }
        return output.toString();
    }
}
