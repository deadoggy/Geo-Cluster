/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import TrajectoryClustering.TraClusterDoc;
import TrajectoryClustering.Trajectory;
import TrajectoryClustering.Cluster;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author yinjiazhang
 */
public class TrajectoryClusterPanel extends JPanel {
    
    private TraClusterDoc traData;
    private Graphics gs;
    
    public boolean startTrajectoryClusterPanel(TraClusterDoc traData, Integer minLns, Double eps){
        try{
            this.traData = traData;
            traData.refresh();
            traData.onClusterGenerate("dataset/result.txt",eps, minLns);
            showCluster();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    public void showCluster(){
        this.repaint();
    }
    
    @Override
    public void paint(Graphics g){
        this.gs=g;
        gs.setColor(Color.WHITE);
        gs.clearRect(0, 0, this.getWidth(), this.getHeight());
        drawTrajectory();
    }
    
    private void drawTrajectory() {
                if(null == traData||null==traData.m_clusterList){
                    return;
                }
		
                ArrayList<Trajectory> trajectoryAL = traData.m_trajectoryList;
                ArrayList<Cluster> clusterRepresentativeTrajectoryAL = traData.m_clusterList;

		
		for (int i = 0; i < trajectoryAL.size();i++) {
			for (int m = 0; m < trajectoryAL.get(i).getM_pointArray().size() - 2 ;m++) {
                                Point start = normalizePoint(trajectoryAL.get(i).getM_pointArray().get(m));
                                Point end = normalizePoint(trajectoryAL.get(i).getM_pointArray().get(m+1));
				int startX = (int)(start.getVectors(0));
				int startY = (int)(start.getVectors(1));
				int endX = (int)(end.getVectors(0));
				int endY = (int)(end.getVectors(1));
                                gs.setColor(Color.GREEN);
				gs.drawLine(startX, startY, endX, endY);
			}
		}
		
		for (int i = 0; i < clusterRepresentativeTrajectoryAL.size();i++) {
			for (int j = 0; j < clusterRepresentativeTrajectoryAL.get(i).getM_PointArray().size() - 2; j++) {
                                Point start = normalizePoint(clusterRepresentativeTrajectoryAL.get(i).getM_PointArray().get(j));
                                Point end = normalizePoint(clusterRepresentativeTrajectoryAL.get(i).getM_PointArray().get(j+1));
				int startX = (int)(start.getVectors(0));
				int startY = (int)(start.getVectors(1));
				int endX = (int)(end.getVectors(0));
				int endY = (int)(end.getVectors(1));
				gs.setColor(Color.RED);
				gs.drawLine(startX, startY, endX, endY);
			}
		}	
    }
    
    private Point normalizePoint(Point point) {
        int width = this.getWidth() - 20;   // leave some margin  ******************change
        int height = this.getHeight() - 20; // leave some margin
        double newX, newY;                     // coordinates in the screen
        double x, y;
        double minX = traData.minX;
        double minY = traData.minY;
        double maxX = traData.maxX;
        double maxY = traData.maxY;
        
        if (point.vectors != null) {
            double[] vectors;
            
            x = point.vectors[0];
            y = point.vectors[1];
            newX = (x - minX) / (maxX - minX) * width + 10;// leave some margin 10 pixle
            newY = (y - minY) / (maxY - minY) * height + 10;
            vectors = new double[2];
            vectors[0] = newX;
            vectors[1] = newY;
            Point newPoint = new Point(vectors, false);

            return newPoint;
        } else {
            return point;
        }
    }
}
