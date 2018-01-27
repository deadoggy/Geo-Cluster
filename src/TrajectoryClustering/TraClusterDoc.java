/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TrajectoryClustering;

/**
 *
 * @author yinjiazhang
 */
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Application;
import main.Point;
import org.json.JSONArray;
import org.json.JSONObject;

public class TraClusterDoc {
	
	public int m_nDimensions;
	public int m_nTrajectories;
	public int m_nClusters;
	public double m_clusterRatio;
	public int m_maxNPoints;
	public ArrayList<Trajectory> m_trajectoryList;
	public ArrayList<Cluster> m_clusterList;
        public double maxX;
        public double maxY;
        public double minX;
        public double minY;
        public long duration;
        public Parameter parameter;
        public String fileName;
        
	
	public TraClusterDoc() {
			
		m_nTrajectories = 0;
		m_nClusters = 0;
		m_clusterRatio = 0.0;	
		m_trajectoryList = new ArrayList<Trajectory>();
		m_clusterList = new ArrayList<Cluster>();
                maxX = maxY = 0;
                minX = minY = 10000;
                parameter = null;
                fileName = null;
	}
        
        public void refresh(){
            m_nClusters = 0;
            m_clusterList = new ArrayList<Cluster>();
            duration = 0;
            parameter = null;
        }
	
	public class Parameter {
		double epsParam;
		int minLnsParam;
	}
        
        public boolean loadFile(String inputFileName){
            try{
                return onOpenDocument(inputFileName);
            }catch(Exception e){
                return false;
            }
        }
	
	boolean onOpenDocument(String inputFileName) {
		
                boolean valid = true;
		int nDimensions = 2;		// default dimension = 2
		int nTrajectories = 0;
		int nTotalPoints = 0;		//no use
		int trajectoryId;
		int nPoints;
		double value;

		DataInputStream in;
		BufferedReader inBuffer = null;
		try {
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFileName)));
			
			inBuffer = new BufferedReader(new InputStreamReader(in));			
			nDimensions = Integer.parseInt(inBuffer.readLine()); // the number of dimensions
			m_nDimensions = nDimensions;
			nTrajectories = Integer.parseInt(inBuffer.readLine()); // the number of trajectories
			m_nTrajectories = nTrajectories;
			
			m_maxNPoints = -1; // initialize for comparison
			
			// the trajectory Id, the number of points, the coordinate of a point ...
			for (int i = 0; i < nTrajectories; i++) {				
	
				String str = inBuffer.readLine();
				
				Scanner sc = new Scanner(str); 
				sc.useLocale(Locale.US);
				
				trajectoryId = sc.nextInt(); //trajectoryID
				nPoints = sc.nextInt(); // number of points in the trajectory
				
				if (nPoints > m_maxNPoints) {
                                    m_maxNPoints = nPoints;
				}
				nTotalPoints += nPoints;
				Trajectory pTrajectoryItem = new Trajectory(trajectoryId, nDimensions);		
				for (int j = 0; j < nPoints; j++) {
					Point point = new Point(nDimensions);   // initialize the CMDPoint class for each point
					
					for (int k = 0; k < nDimensions; k++) {						
						value = sc.nextDouble();
						point.setVectors(k, value);						
					}
                                        maxX = maxX > point.vectors[0] ? maxX : point.vectors[0];
                                        minX = minX < point.vectors[0] ? minX : point.vectors[0];
                                        maxY = maxY > point.vectors[1] ? maxY : point.vectors[1];
                                        minY = minY < point.vectors[1] ? minY : point.vectors[1]; 
					pTrajectoryItem.addPointToArray(point);				
				}
				
				m_trajectoryList.add(pTrajectoryItem);
			}					
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("Unable to open input file");
                        valid=false;
		} catch (NumberFormatException e) {
			//e.printStackTrace();
                        valid=false;
		} catch (IOException e) {
			//e.printStackTrace();
                        valid=false;
		}  finally {
			try {
				inBuffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
                fileName = inputFileName.substring(inputFileName.lastIndexOf("/")+1,
                        inputFileName.lastIndexOf(".")!=-1?inputFileName.lastIndexOf("."): inputFileName.length());
		return valid;
	}
	
	public boolean onClusterGenerate(String clusterFileName, double epsParam, int minLnsParam) {
//////////////////////////////////////////////////still to be written
                parameter = new Parameter();
                parameter.epsParam = epsParam;
                parameter.minLnsParam = minLnsParam;
		long startTime = System.currentTimeMillis();
                long endTime;
		ClusterGen generator = new ClusterGen(this);
		
		if(m_nTrajectories == 0) {
			System.out.println("Load a trajectory data set first");
		}
		
		// FIRST STEP: Trajectory Partitioning
		if (!generator.partitionTrajectory())
		{
			System.out.println("Unable to partition a trajectory\n");
			return false;
		}

		// SECOND STEP: Density-based Clustering
		if (!generator.performDBSCAN(epsParam, minLnsParam))
		{
			System.out.println("Unable to perform the DBSCAN algorithm\n");
			return false;
		}

		// THIRD STEP: Cluster Construction
		if (!generator.constructCluster())
		{
			System.out.println( "Unable to construct a cluster\n");
			return false;
		}

		
		for (int i = 0; i <m_clusterList.size(); i++) {
			//m_clusterList.
			System.out.println(m_clusterList.get(i).getM_clusterId());
			for (int j = 0; j<m_clusterList.get(i).getM_PointArray().size(); j++) {
				
				double x = m_clusterList.get(i).getM_PointArray().get(j).getVectors(0);
				double y = m_clusterList.get(i).getM_PointArray().get(j).getVectors(1);
			System.out.print("   "+ x +" "+ y +"   ");
			}
			System.out.println();
		}
//		FileOutputStream fos = null;
//		BufferedWriter bw = null;
//		OutputStreamWriter osw = null;
//		try {
//			fos = new FileOutputStream(clusterFileName);
//			osw = new OutputStreamWriter(fos);
//			bw = new BufferedWriter(osw);
//			
//			bw.write("epsParam:"+epsParam +"   minLnsParam:"+minLnsParam);
//			
//			for (int i = 0; i < m_clusterList.size(); i++) {
//				// m_clusterList.
//				bw.write("\nclusterID: "+ m_clusterList.get(i).getM_clusterId() + "  Points Number:  " + m_clusterList.get(i).getM_PointArray().size() + "\n");
//				for (int j = 0; j < m_clusterList.get(i).getM_PointArray().size(); j++) {
//					
//					double x = m_clusterList.get(i).getM_PointArray().get(j).getVectors(0);
//					double y = m_clusterList.get(i).getM_PointArray().get(j).getVectors(1);
//					bw.write(x+" "+y+"   ");
//				}
//			}						
//			
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//                        
//			try {
//				bw.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
                duration = System.currentTimeMillis() - startTime;
		return true;		
	}
	
	public Parameter onEstimateParameter() {
		Parameter p = new Parameter();
		ClusterGen generator = new ClusterGen(this);
		if (!generator.partitionTrajectory()) {
			System.out.println("Unable to partition a trajectory\n");
			return null;
		}
		if (!generator.estimateParameterValue(p)) {
			System.out.println("Unable to calculate the entropy\n");
			return null;
		}
		return p;
	}
        
        
        public void clearResult(){
            m_clusterList = null;
        }
        
        public String exportResult(){
            if(null==m_clusterList||0==m_clusterList.size()){
                return null;
            }
            JSONObject info = new JSONObject();
            info.append("algorithm", "trajectory clustering");
            info.append("duration", duration);
            info.append("eps",parameter.epsParam);
            info.append("minLns", parameter.minLnsParam);
            JSONObject cluster = new JSONObject();
            for(int i=0 ; i<m_clusterList.size(); i++){
                JSONArray clusterArray = new JSONArray();
                for(Point pt : m_clusterList.get(i).getM_PointArray()){
                    JSONArray ptArr = new JSONArray();
                    ptArr.put(pt.vectors[0]);
                    ptArr.put(pt.vectors[1]);
                    clusterArray.put(ptArr);
                }
                cluster.append(String.valueOf(i), clusterArray);
            }
            info.append("cluster", cluster);
            
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String presentTime = df.format(new Date());
            
            String exportedFileName = fileName + "_traj_" + presentTime + ".json";
            try {
                FileOutputStream os = new FileOutputStream(exportedFileName);
                OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
                info.write(writer);
                writer.close();
                os.close();
            } catch (Exception ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return exportedFileName;
        }

}