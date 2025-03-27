package org.sbpo2025.challenge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Instance {
	
	BufferedReader reader;
	BufferedWriter writer;
	
    public List<Map<Integer, Integer>> orders;
    public List<Map<Integer, Integer>> aisles;
    public List<Map<Integer, Integer>> itemsPerOrders;
    public List<Map<Integer, Integer>> itemsPerAisles;
    public int LB, UB, n;

    public Instance(String file) {
        readInput(file);
    }
    
    private void readInput(String inputFilePath) {
        try {
            this.reader = new BufferedReader(new FileReader(inputFilePath));
            
            // Read first line.
            String line = this.reader.readLine();
            String[] firstLine = line.split(" ");
            int nOrders = Integer.parseInt(firstLine[0]);
            int nItems = Integer.parseInt(firstLine[1]);
            int nAisles = Integer.parseInt(firstLine[2]);

            // Initialize orders and aisles arrays
            orders = new ArrayList<>(nOrders);
            aisles = new ArrayList<>(nAisles);
            itemsPerOrders = new ArrayList<>(nItems);
            itemsPerAisles = new ArrayList<>(nItems);
            this.n = nItems;

            // Read orders
            readItemQuantityPairs(orders, nOrders);

            // Read aisles
            readItemQuantityPairs(aisles, nAisles);
            transposeOrdersToItems();
            transposeAislesToItems();

            // Read wave size bounds
            line = this.reader.readLine();
            String[] bounds = line.split(" ");
            LB = Integer.parseInt(bounds[0]);
            UB = Integer.parseInt(bounds[1]);

            this.reader.close();
        } catch (IOException e) {
            System.err.println("Error reading input from " + inputFilePath);
            e.printStackTrace();
        }
    }

    private void readItemQuantityPairs(List<Map<Integer, Integer>> orders, int nLines) throws IOException {
        String line;
        for (int orderIndex = 0; orderIndex < nLines; orderIndex++) {
            line = this.reader.readLine();
            String[] orderLine = line.split(" ");
            int nOrderItems = Integer.parseInt(orderLine[0]);
            Map<Integer, Integer> orderMap = new HashMap<>();
            for (int k = 0; k < nOrderItems; k++) {
                int itemIndex = Integer.parseInt(orderLine[2 * k + 1]);
                int itemQuantity = Integer.parseInt(orderLine[2 * k + 2]);
                orderMap.put(itemIndex, itemQuantity);
            }
            orders.add(orderMap);
        }
    }
    
    public void writeOutput(ChallengeSolution challengeSolution, String outputFilePath) {
        if (challengeSolution == null) {
            System.err.println("Solution not found");
            return;
        }
        try {
            this.writer = new BufferedWriter(new FileWriter(outputFilePath));
            var orders = challengeSolution.orders();
            var aisles = challengeSolution.aisles();

            // Write the number of orders
            this.writer.write(String.valueOf(orders.size()));
            this.writer.newLine();

            // Write each order
            for (int order : orders) {
            	this.writer.write(String.valueOf(order));
            	this.writer.newLine();
            }

            // Write the number of aisles
            this.writer.write(String.valueOf(aisles.size()));
            this.writer.newLine();

            // Write each aisle
            for (int aisle : aisles) {
            	this.writer.write(String.valueOf(aisle));
            	this.writer.newLine();
            }

            this.writer.close();
            System.out.println("Output written to " + outputFilePath);

        } catch (IOException e) {
            System.err.println("Error writing output to " + outputFilePath);
            e.printStackTrace();
        }
    }
    
    protected void transposeAislesToItems() {    	
    	for(int i = 0; i < n; i++)
    		itemsPerAisles.add(i, new HashMap<Integer, Integer>());
    	
    	for(int a = 0; a < aisles.size(); a++) 
    		for(int i : aisles.get(a).keySet())
    			itemsPerAisles.get(i).put(a, aisles.get(a).get(i));
    }
    
    protected void transposeOrdersToItems() {    	
    	for(int i = 0; i < n; i++)
    		itemsPerOrders.add(i, new HashMap<Integer, Integer>());
    	
    	for(int o = 0; o < orders.size(); o++) 
    		for(int i : orders.get(o).keySet())
    			itemsPerOrders.get(i).put(o, orders.get(o).get(i));
    }
}
