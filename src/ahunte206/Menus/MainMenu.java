package ahunte206.Menus;

import ahunte206.Controller.NetworkController;
import ahunte206.DAO.InputHelper;

public class MainMenu {

    private InputHelper inputHelper;
    private int input;
    private boolean finished;
    private NetworkController controller;

    public MainMenu(){
        inputHelper = new InputHelper();
        finished = false;
        controller = new NetworkController();
    }

    public void start(){
        do{
            System.out.println(getOptions());
            input = inputHelper.readInt("What would you like to do?", 3, 0);
            switch(input){
                case 0:
                    System.out.println("Exiting application.");
                    finished = true;
                    break;
                case 1: //parse file
                    parseFile();
                    break;
                case 2: //load file
                    loadNetwork();
                    break;
                case 3:
                    calculateShortestPath();
                    break;
                default:
                    break;
            }
        }while(!finished);
    }

    //parse OSM XML to csv file
    private void parseFile() {
        System.out.println(getMapList());
        int input = inputHelper.readInt("Please enter which map file you would like to parse:", 3, 0);
        if(input == 0) return;
        if(controller.parseNetwork(input)){
            System.out.println("File stored successfully");
        }
    }

    private void loadNetwork(){
        System.out.println(getNetworkList());
        int input = inputHelper.readInt("Please enter which network you would like to load:", 3, 0);
        if(input == 0) return;
        boolean success;
        success = controller.loadNetwork("networks\\" + input + ".csv");
        if(success) System.out.println("File loaded successfully");
        else System.out.println("Failed to load file.");
    }

    private void calculateShortestPath() {
        //ensures network is loaded before the shortest path is conducted
        if(!controller.isNetworkLoaded()){
            //ensure network is loaded
            System.out.println("No network is currently loaded");
            int option = inputHelper.readInt("Would you like to laod one now?\n1 - YES\n0 - NO", 1, 0);
            if(option == 0) return;
            else{
                loadNetwork();
                return; //avoids bug, could otherwise bypass loading a network
            }
        }
        //get valid nodes
        String sourceNode = getSourceNode();
        if(sourceNode == null) return;
        String destinationNode = getDestinationNode(sourceNode);
        if(destinationNode == null) return;

        //Calculate shortest path
        if(!controller.calculateSPA(sourceNode, destinationNode))
            System.out.println("Calculation failed...");
        else {
            System.out.println("Calculation complete");
            System.out.println(controller.buildResult());
        }

    }

    //gets a valid source node
    private String getSourceNode() {
        String nodeId;
        boolean loop = true;
        do{
            //get input from user and exit if required
            nodeId = inputHelper.readString("Please enter the source node ID (type 0 to exit)");
            if(nodeId.equalsIgnoreCase("0")) return null;
            //check if node is in network
            if(!controller.networkContainsNode(nodeId))
                System.out.println("Node does not exist, please try again. Refer to csv file for a list of nodes.");
            //stop loop if valid node found
            else loop = false;
        } while(loop);
        return nodeId;
    }

    //gets a valid source node
    private String getDestinationNode(String sourceNodeId) {
        String nodeId;
        boolean loop = true;
        do{
            //get input from user and exit if required
            nodeId = inputHelper.readString("Please enter the destination node ID (type 0 to exit)");
            if(nodeId.equalsIgnoreCase("0")) return null;
            //check if node is in network
            if(!controller.networkContainsNode(nodeId))
                System.out.println("Node does not exist, please try again. Refer to csv file for a list of nodes.");
            //if source node and destination node match
            else if(nodeId.equals(sourceNodeId))
                System.out.println("Destination node cannot be the same as the source node");
            //stop loop if valid node found
            else loop = false;
        } while(loop);
        return nodeId;
    }

    private String getOptions() {
        String output = "\n==============================================\n";
        output += "0 - Back\n";
        output += "1 - Parse and store network\n";
        output += "2 - Load parsed network\n";
        output += "3 - Calculate shortest path";
        output += "\n==============================================";
        return output;
    }

    //returns a list of map options to load
    private String getMapList() {
        String output = "\nWhich map would you like to parse?" +
                "\n==============================================\n";
        output += "0 - Cancel \n";
        output += "1 - Kirkintilloch (Test)\n";
        output += "2 - East Dunbartonshire (Small)\n";
        output += "3 - Greater Glasgow (Large)[WARNING: time to parse > 2 hours]";
        output += "\n==============================================";
        return output;
    }

    private String getNetworkList() {
        String output = "\nWhich network would you like to load?" +
                "\n==============================================\n";
        output += "0 - Cancel \n";
        output += "1 - Kirkintilloch\n";
        output += "2 - East Dunbartonshire\n";
        output += "3 - Greater Glasgow";
        output += "\n==============================================";
        return output;
    }
}
