/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * markerclusterer is an api from google-map
 * @author AdamGu0
 */
public class SplitSmartSwapClusterer {

    private final MapKit mapKit;
    public Cluster[] clustersArray;
    public long duration;
    public boolean startCluster = false;
    private final double[] minDistance;
    private ArrayList<Cluster[]> allClusters;
    private double[] dnear;
    private int maxIteration;
    private String fileName;



    public SplitSmartSwapClusterer(MapKit mapKit) {
        this.mapKit = mapKit;
        this.minDistance = getDistance();
    }
    
    private void loadCache() {
        try {
            FileInputStream fin = new FileInputStream(getFullPath());
            ObjectInputStream ois = new ObjectInputStream(fin);
            allClusters = (ArrayList<Cluster[]>) ois.readObject();
            dnear = (double[]) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cache Not Available: " + getFullPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getFullPath() {
        return "." + File.separator + "cache" + File.separator + fileName;
    }

    public int startSplitSmartSwap(Point[] data, int zoom, String fileName) {
        if (data == null) {
            return 1;
        } else if (data.length == 0) {
            return 2;
        }
        if (!fileName.equals(this.fileName)) {
            this.fileName = fileName;
            this.allClusters = null;
            loadCache();
        }
        this.startCluster = true;
        
        
        long start = System.currentTimeMillis();
        ClusterData cd = new ClusterData(data, true);
        
        this.maxIteration = (int) java.lang.Math.ceil(java.lang.Math.sqrt(data.length));
        if (allClusters == null) {
            allClusters = cd.splitSmartSwap(maxIteration);
            this.dnear = new double[maxIteration];
            for (int k = 2; k < maxIteration; k++) {
                dnear[k] = ClusterData.getNearestDist(allClusters.get(k));
            }
            ObjectOutputStream oos = null;
            FileOutputStream fout = null;
            try {
                File dir = new File("cache");
                dir.mkdir();
                fout = new FileOutputStream(getFullPath(), true);
                oos = new ObjectOutputStream(fout);
                oos.writeObject(allClusters);
                oos.writeObject(dnear);
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // System.out.println(Arrays.toString(dnear));
        duration = System.currentTimeMillis() - start;
        doSplitSmartSwap(zoom);
        return 0;
    }

    public boolean doSplitSmartSwap(int zoom) {
        if (!startCluster) return startCluster;
        zoom = 17 - zoom;
        
        int i;
        for (i = 2; i < maxIteration; i++) {
            System.out.println("dnear: "  + dnear[i] + " mindis:" + minDistance[zoom]);

            if (dnear[i] < minDistance[zoom]) {
                System.out.println(' '  + dnear[i] + ' ' + minDistance[zoom]);
                break;
            }
        }
        
        System.out.println("do split smart swap " + i + ' ' + zoom);
        Cluster[] clusters = allClusters.get(i);

        mapKit.setWaypoints(clusters);
        mapKit.repaint();
        this.clustersArray = clusters;
        return startCluster;
    }
    
    private double[] getDistance() {
        double[] distances = new double[50];
        distances[0] = 2500;
        for (int i = 1; i < distances.length; i++) {
            distances[i] = distances[i - 1] / 2;
        }
        System.out.println(Arrays.toString(distances));
        return distances;
    }
    
}
