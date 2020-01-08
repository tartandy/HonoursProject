package ahunte206.DAO;

import ahunte206.Model.Edge;
import ahunte206.Model.Network;
import ahunte206.Model.Node;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FileManager {

    //load OSM XML data to a document ready to be parsed
    public static Document loadXML(String option, String url){
        String osmFile = "osm\\" + option + ".osm";


        try {
            //create URL from string
            URL website = new URL(url);
            //open connection to URL
            System.out.print("\rWaiting for URL... (Can take > 15s for overpass API to respond)");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            //create output stream to file and download
            System.out.print("\rDownloading file...");
            FileOutputStream fos = new FileOutputStream(osmFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            System.out.print("\rDownload complete\n");
            fos.close();
        } catch (FileNotFoundException e) {
            System.out.println("File could not be downloaded from the internet.");
            return null;
        } catch (IOException e){
            System.err.println("\n" + e.getMessage() + "\n Likely a HTTP response code, link either down or request blocked.");
            return null;
        }

        //Load file to doc once downloaded
        File osmXML;
        DocumentBuilderFactory dbFactory;
        DocumentBuilder dBuilder;
        Document doc;
        try {
            System.out.print("Loading OSM XML file...");
            osmXML = new File(osmFile);
            //check file exists
            if(!osmXML.exists()) return null;
            //creates document object from file
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(osmXML);
            //normalises document
            doc.getDocumentElement().normalize();
            System.out.print(" Done\n");
            //delete OSM XML file
            deleteFile(osmFile);
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        deleteFile(osmFile);
        return null;
    }

    //load road lookup information used by the parser
    public static HashMap<String, Integer> loadRoadData() {
        String file = "lookupInformation\\roadData.txt";
        HashMap<String, Integer> speedLimits = new HashMap<>();
        try{
            FileReader fl = new FileReader(file);
            BufferedReader br = new BufferedReader(fl);
            String line;
            while((line = br.readLine()) != null){
                String key = line.split(",")[0];
                Integer value = Integer.parseInt(line.split(",")[1]);
                speedLimits.put(key, value);
            }
        } catch(IOException e){
            System.out.println("Error opening speedLimits file: " + file);
            e.printStackTrace();
        }
        return speedLimits;
    }

    //save network to file
    public static boolean saveNetwork(String fileName, Network network) {
        try{
            File file = new File(fileName);
            FileWriter fw = new FileWriter(file);
            for(Node n : network.getNodes().values()) { fw.write((n.toFile()) + "\n"); }
            for(Edge e : network.getEdges().values()) { fw.write(e.toFile() + "\n");}
            fw.flush();
            fw.close();
            return true;
        } catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    //load network from file
    public static Network loadNetwork(String fileName){
        HashMap<String, Node> nodes = new HashMap<>();
        HashMap<String, Edge> edges = new HashMap<>();

        try{
            //create new buffered reader for file
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line;
            //while next line in file exists
            while ((line = br.readLine()) != null){
                String[] args = line.split(",");
                if(args.length == 2){
                    Node n = loadNode(args);
                    nodes.put(n.getId(), n);
                } else if(args.length == 7){
                    Edge e = loadEdge(args);
                    edges.put(e.getId(), e);
                } else System.err.println("invalid line in file: " + line);
            }
        } catch(FileNotFoundException e){
            System.out.println("File: '" + fileName + "' not found. Please try again.");
            return null;
        } catch (IOException e){
            e.printStackTrace();
        }
        return new Network(nodes, edges, edges.size());
    }

    //takes args from file and creates an edge
    private static Edge loadEdge(String[] args) {
        ArrayList<String> connectedNodeIds = new ArrayList<>(Arrays.asList(args[4].split(";")));
        int maxSpeed = Integer.parseInt(args[2]);
        boolean oneWay = Boolean.parseBoolean(args[3]);
        double distance = Double.parseDouble(args[5]);
        double cost = Double.parseDouble(args[6]);
        return new Edge(args[0], connectedNodeIds, args[1], maxSpeed, oneWay, distance, cost);
    }
    //takes args from file and creates a node
    private static Node loadNode(String[] args) {
        //load node data from args and return
        ArrayList<String> connectedEdgeIds = new ArrayList<>(Arrays.asList(args[1].split(";")));
        return new Node(args[0], connectedEdgeIds);
    }

    //delete file
    private static void deleteFile(String filepath){
        new File(filepath).delete();
    }
}
