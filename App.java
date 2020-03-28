package org.myorg.IS709;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.scoring.AlphaCentrality;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.ClosenessCentrality;
import org.jgrapht.alg.scoring.Coreness;
import org.jgrapht.alg.scoring.HarmonicCentrality;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

public class App {
	private static final int NUM_ROUND_OF_INVITE = 10;
	private static final int MAX_NUM_OF_VERTEX = 20000;
	private static int iMaxNumOfVertex = MAX_NUM_OF_VERTEX;
	private static BufferedWriter brPathFile = null;
	private static BufferedWriter brResultFile = null;
	private static double[] Top10VertexScore = new double[10];
	private static int[] Top10Vertex = new int[10];
	private static double[] fVertexAlphaArray; 
	private static double[] fVertexCorenessArray;
	private static double[] fVertexClosenessArray;
	private static double[] fVertexHarmonicArray;
	private static double[] fVertexBetweennessArray;
	private static double[] fVertexDegreeArray;	
	private static double[][] fVertexMatrixArrays; 
	private static String[] strVertexMatrixArrays = {	"Alpha Centrality",  "Coreness Centrality",  "Closeness Centrality",  
														"Harmonic Centrality",  "Betweenness Centrality", "Degree Centrality"};
	private static String strVertexFile = "C:\\App\\Input.nodes";
	private static String strEdgeFile = "C:\\App\\Input.edges";
	public static void main(String[] args) {		
		try{
		int iOption = 0;
		if ( args.length != 2 ) {
			System.out.println("java -Xms512m -Xmx12G -jar App.jar NumOfVertex Option");
			System.out.println("Option: 1 - Compute degree of separation using original graph.");
			System.out.println("Option: 2 - Compute degree of separation using sample subgraph.");
			System.out.println("Option: 3 - Sample subgraph that contains 'NumOfVertex' vertexes.");
			System.out.println("Option: 4 - Sample subgraph that contains first 'NumofVertex' vertexes.");
			System.out.println("Option: 5 - Run the tests using original graph.");
			System.out.println("Option: 6 - Run the tests using sample subgraph.");
			return;
		}
		
		// Read the option to execute.
		iMaxNumOfVertex = Integer.parseInt(args[0]);
		iOption  = Integer.parseInt(args[1]);			
		if ( iOption == 1 || iOption == 2) { // Compute degree of separation.				
			Graph<Integer, DefaultEdge> myGraph = GraphTypeBuilder
					.undirected().weighted(false)
					.allowingMultipleEdges(false)
					.allowingSelfLoops(false)
					.vertexSupplier(SupplierUtil.createIntegerSupplier())
					.edgeSupplier(SupplierUtil.createDefaultEdgeSupplier())
					.buildGraph();
			if ( iOption == 1) {
				strVertexFile = "C:\\App\\linkedin.nodes";
				strEdgeFile = "C:\\App\\linkedin.edges";
			}
			else {
				strVertexFile = "C:\\App\\Input.nodes";
				strEdgeFile = "C:\\App\\Input.edges";					
			}
			
			BuildGraph(myGraph);
			
			fVertexDegreeArray = new double[iMaxNumOfVertex];				
			for ( int iIndex = 0 ; iIndex < iMaxNumOfVertex ; iIndex++ )
				fVertexDegreeArray[iIndex] = myGraph.incomingEdgesOf(iIndex).size() * 1.0;				
			ComputeTopVertex(fVertexDegreeArray);
			
			DegreeOfSeparation(myGraph);				
			return;
		}
		else if ( iOption == 3 || iOption == 4) { // Create the sample graph with reduced number of vertexes.
			Graph<Integer, DefaultEdge> myGraph = GraphTypeBuilder
					.undirected().weighted(false)
					.allowingMultipleEdges(false)
					.allowingSelfLoops(false)
					.vertexSupplier(SupplierUtil.createIntegerSupplier())
					.edgeSupplier(SupplierUtil.createDefaultEdgeSupplier())
					.buildGraph();
			if ( iOption == 3)
				SampleGraph(myGraph);
			else
				SampleFirstGraph(myGraph);
			return;
		}
		else if ( iOption == 5) { // Run the test using the original graph.
			strVertexFile = "C:\\App\\linkedin.nodes";
			strEdgeFile = "C:\\App\\linkedin.edges";
		}
		else if ( iOption == 6) { // Run the test using the sample subgraph.
			strVertexFile = "C:\\App\\Input.nodes";
			strEdgeFile = "C:\\App\\Input.edges";					
		}
		else {
			System.out.println("Unknown option (" + iOption + ").");
			return;
		}
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date startdate = new Date();
		System.out.println("Start: " + dateFormat.format(startdate));
		
		// Create output file.
		File fileResult = new File("Result_" + iMaxNumOfVertex +".txt");
		File filePath = new File("Path_" + iMaxNumOfVertex + ".txt");
        FileWriter frResult = new FileWriter(fileResult);
        FileWriter frPath = new FileWriter(filePath);
        brResultFile = new BufferedWriter(frResult);
        brPathFile = new BufferedWriter(frPath);
               
		// Create the graph.
		Graph<Integer, DefaultEdge> myGraph = GraphTypeBuilder
				.undirected().weighted(false)
				.allowingMultipleEdges(false)
				.allowingSelfLoops(false)
				.vertexSupplier(SupplierUtil.createIntegerSupplier())
				.edgeSupplier(SupplierUtil.createDefaultEdgeSupplier())
				.buildGraph();
		BuildGraph(myGraph);
		int iTotalVertexSize = myGraph.vertexSet().size();	
		
		// Compute all the matrix.
		fVertexAlphaArray = new double[iTotalVertexSize]; 
		fVertexCorenessArray = new double[iTotalVertexSize]; 
		fVertexClosenessArray = new double[iTotalVertexSize]; 
		fVertexHarmonicArray = new double[iTotalVertexSize]; 
		fVertexBetweennessArray = new double[iTotalVertexSize]; 
		fVertexDegreeArray = new double[iTotalVertexSize]; 		
		
		// Store all the scores into array and file for easy reference.
		File filePathAlpha = new File("C:\\App\\Alpha_" + iMaxNumOfVertex + ".csv");
		File filePathCoreness = new File("C:\\App\\Coreness_" + iMaxNumOfVertex + ".csv");
		File filePathCloseness = new File("C:\\App\\Closeness_" + iMaxNumOfVertex + ".csv");
		File filePathHarmonic= new File("C:\\App\\Harmonic_" + iMaxNumOfVertex + ".csv");
		File filePathBetween = new File("C:\\App\\Between_" + iMaxNumOfVertex + ".csv");
		File filePathDegree = new File("C:\\App\\Degree_" + iMaxNumOfVertex + ".csv");
		
		if ( filePathAlpha.exists() ) {
			FileInputStream fstream = new FileInputStream(filePathAlpha);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(",");
				fVertexAlphaArray[Integer.parseInt(tokens[0])] = Double.parseDouble(tokens[1]); //0,0.12345
			}
			br.close();
			in.close();
			fstream.close();
		}
		else {
			AlphaCentrality<Integer, DefaultEdge> alphaC = new AlphaCentrality<Integer, DefaultEdge>(myGraph, AlphaCentrality.DAMPING_FACTOR_DEFAULT, 0);
			FileWriter frResultAlpha = new FileWriter(filePathAlpha);
			BufferedWriter  brResultFileAlpha = new BufferedWriter(frResultAlpha);
			for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
				fVertexAlphaArray[iIndex] = alphaC.getVertexScore(iIndex);	
				brResultFileAlpha.write(iIndex+","+fVertexAlphaArray[iIndex]+"\n");				
			}
			brResultFileAlpha.close(); frResultAlpha.close();
		}
		
		if ( filePathCoreness.exists() ) {
			FileInputStream fstream = new FileInputStream(filePathCoreness);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(",");
				fVertexCorenessArray[Integer.parseInt(tokens[0])] = Double.parseDouble(tokens[1]); //0,0.12345
			}
			br.close();
			in.close();
			fstream.close();
		}
		else {
			Coreness<Integer, DefaultEdge> coreC = new Coreness<Integer, DefaultEdge>(myGraph);
			FileWriter frResultCoreness = new FileWriter(filePathCoreness);
			BufferedWriter  brResultFileCoreness = new BufferedWriter(frResultCoreness);
			for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
				fVertexCorenessArray[iIndex] = coreC.getVertexScore(iIndex);			
				brResultFileCoreness.write(iIndex+","+fVertexCorenessArray[iIndex]+"\n");	
			}
			brResultFileCoreness.close(); frResultCoreness.close();
		}
       
		if ( filePathCloseness.exists() ) {
			FileInputStream fstream = new FileInputStream(filePathCloseness);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(",");
				fVertexClosenessArray[Integer.parseInt(tokens[0])] = Double.parseDouble(tokens[1]); //0,0.12345
			}
			br.close();
			in.close();
			fstream.close();
		}
		else {
			// When the graph is disconnected, the closeness centrality score equals 0 for all vertices. In the case of weakly connected digraphs, the closeness centrality of several vertices might be 0. 
			ClosenessCentrality<Integer, DefaultEdge> closeC = new ClosenessCentrality<Integer, DefaultEdge>(myGraph, false, true);
			FileWriter frResultCloseness = new FileWriter(filePathCloseness);
			BufferedWriter  brResultFileCloseness = new BufferedWriter(frResultCloseness);
		    for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
		    	fVertexClosenessArray[iIndex] = closeC.getVertexScore(iIndex);			
				brResultFileCloseness.write(iIndex+","+fVertexClosenessArray[iIndex]+"\n");	
			}
		    brResultFileCloseness.close(); frResultCloseness.close();
		}
       
		if ( filePathHarmonic.exists() ) {
			FileInputStream fstream = new FileInputStream(filePathHarmonic);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(",");
				fVertexHarmonicArray[Integer.parseInt(tokens[0])] = Double.parseDouble(tokens[1]); //0,0.12345
			}
			br.close();
			in.close();
			fstream.close();
		}
		else {
			HarmonicCentrality<Integer, DefaultEdge> harmonicC = new HarmonicCentrality<Integer, DefaultEdge>(myGraph); // Centrality is normalized and computed using outgoing paths.
			FileWriter frResultHarmonic = new FileWriter(filePathHarmonic);
			BufferedWriter  brResultFileHarmonic = new BufferedWriter(frResultHarmonic);
		    for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
		    	fVertexHarmonicArray[iIndex] = harmonicC.getVertexScore(iIndex);	
		    	brResultFileHarmonic.write(iIndex+","+fVertexHarmonicArray[iIndex]+"\n");
			}
		    brResultFileHarmonic.close(); frResultHarmonic.close();
		}
		
		if ( filePathBetween.exists() ) {
			FileInputStream fstream = new FileInputStream(filePathBetween);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(",");
				fVertexBetweennessArray[Integer.parseInt(tokens[0])] = Double.parseDouble(tokens[1]); //0,0.12345
			}
			br.close();
			in.close();
			fstream.close();
		}
		else {
			BetweennessCentrality<Integer, DefaultEdge> betweenC = new BetweennessCentrality<Integer, DefaultEdge>(myGraph); // Normalize by dividing the closeness by (n−1)⋅(n−2), where n is the number of vertices of the graph
			FileWriter frResultBetween = new FileWriter(filePathBetween);
			BufferedWriter  brResultFileBetween = new BufferedWriter(frResultBetween);
		    for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
		    	fVertexBetweennessArray[iIndex] = betweenC.getVertexScore(iIndex);
				brResultFileBetween.write(iIndex+","+fVertexBetweennessArray[iIndex]+"\n");				
			}
		    brResultFileBetween.close(); frResultBetween.close();
		}

		if ( filePathDegree.exists() ) {
			FileInputStream fstream = new FileInputStream(filePathDegree);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(",");
				fVertexDegreeArray[Integer.parseInt(tokens[0])] = Double.parseDouble(tokens[1]); //0,0.12345
			}
			br.close();
			in.close();
			fstream.close();
		}
		else {
			FileWriter frResultDegree = new FileWriter(filePathDegree);
			BufferedWriter  brResultFileDegree = new BufferedWriter(frResultDegree);
		    for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
		    	fVertexDegreeArray[iIndex] = myGraph.incomingEdgesOf(iIndex).size() * 1.0;
		    	brResultFileDegree.write(iIndex+","+fVertexDegreeArray[iIndex]+"\n");		
			}
		    brResultFileDegree.close(); frResultDegree.close();
		}
		fVertexMatrixArrays = new double[][] {	fVertexAlphaArray, fVertexCorenessArray, fVertexClosenessArray, 
			fVertexHarmonicArray, fVertexBetweennessArray, fVertexDegreeArray} ; 
			
		// Get top 10 vertex
		ComputeTopVertex(fVertexBetweennessArray);
		
		// Test for different number of vertex.
		int[] iNumOfVertexToTestArray = new int[] { 10000, 30000, 50000, 70000, 100000};
		for ( int iNumOfVertexToTestArrayIndex = 0 ; iNumOfVertexToTestArrayIndex < iNumOfVertexToTestArray.length ; iNumOfVertexToTestArrayIndex++ ) {
			brPathFile.write("\nTo test for "+ iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex] + " vertex.\n");
			if ( iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex] > iMaxNumOfVertex ) // Skip if the number of vertex is > maximum of vertex available.
				continue;
			
			// Run the test for each of the vertex matrix type.		
			for ( int iVertexArrayIndex  = 0 ; iVertexArrayIndex < 6 ; iVertexArrayIndex++ ) {			
				brPathFile.write("Using " + strVertexMatrixArrays[iVertexArrayIndex] + "\n");
				
				// Run the test for NUM_TEST_RUN times.
				boolean bTopThree = true;
				for ( int i2Round = 0 ; i2Round < 2 ; i2Round++ ) {
					int iHop = 0;
					int iReachTop = 0;
					int iTotalHop = 0;
					int iTestTop = 0;
					int iMaxHop = 0;
					int iMinHop = Integer.MAX_VALUE;
					bTopThree = !bTopThree;
					if ( bTopThree )
						brPathFile.write("Top 3 potential neighbours\n");
					else
						brPathFile.write("Top 1 potential neighbours\n");
					brPathFile.flush();
					
					// Initialize the flag that indicates if the vertex has been tested.
					boolean[] bRandomVertexArray = new boolean[iTotalVertexSize]; 
					for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
						bRandomVertexArray[iIndex] = false;
					}
					
					if ( iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex] == iMaxNumOfVertex ) {
						// Perform the test for each vertex (up to iNumOfVertexToTest vertex).
						for (int iVertexRunIndex = 0 ; iVertexRunIndex < iMaxNumOfVertex ; iVertexRunIndex++){
							if ( iVertexRunIndex > 10 ) { // We do not perform the test on the top 10 vertex.
								iHop = GetNumberHops(myGraph, iVertexRunIndex, fVertexMatrixArrays[iVertexArrayIndex], bTopThree );
								if (iHop > 0 ) { // -1 indicates that the vertex failed to reach top 10 vertex ; 0 indicates that the vertex already reach top 10 vertex.
									iReachTop++;
									iTotalHop+=iHop;
									if ( iHop > iMaxHop )
										iMaxHop = iHop;
									if ( iHop < iMinHop )
										iMinHop = iHop;
								}
							}														
						}
					}
					else {
						// Generate random integers in range 0 to myGraph.vertexSet().size()
						Random rand = new Random(iTotalVertexSize);
						int iRandomVertex = rand.nextInt(iTotalVertexSize);					
						 
						// Perform the test for each vertex (up to iNumOfVertexToTest vertex).
						//for (int iVertexRunIndex = 0 ; iVertexRunIndex < iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex] ; iVertexRunIndex++){
						for (;;) {
							if ( bRandomVertexArray[iRandomVertex] == false ) { // Skip if the vertex is tested before.
								bRandomVertexArray[iRandomVertex] = true;
								iTestTop++;
								if ( iRandomVertex > 10 ) { // We do not perform the test on the top 10 vertex.
									iHop = GetNumberHops(myGraph, iRandomVertex, fVertexMatrixArrays[iVertexArrayIndex], bTopThree );
									if (iHop > 0 ) { // -1 indicates that the vertex failed to reach top 10 vertex ; 0 indicates that the vertex already reach top 10 vertex.
										iReachTop++;
										iTotalHop+=iHop;
										if ( iHop > iMaxHop )
											iMaxHop = iHop;
										if ( iHop < iMinHop )
											iMinHop = iHop;
									}
								}
							}
							// Stop the test if the number of test reached the number of vertex needed to be tested.
							if ( iTestTop >= iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex] ) 
								break;
							iRandomVertex = rand.nextInt(iTotalVertexSize);
						}
					}
					
					brResultFile.write(strVertexMatrixArrays[iVertexArrayIndex]+","+iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex]+","+bTopThree+","+iReachTop+","+iTotalHop*1.0/iReachTop+","+iMinHop+","+iMaxHop+"\n");
					brResultFile.flush();
				}
			}
			
			// Run the test for each of the vertex matrix type.			
			brPathFile.write("\n!Using all algorithm!\n");	
			brPathFile.write("Top 1 potential neighbours\n");
			int iHop = 0;
			int iReachTop = 0;
			int iTotalHop = 0;
			int iTestTop = 0;
			int iMaxHop = 0;
			int iMinHop = Integer.MAX_VALUE;
				
			// Initialize the flag that indicates if the vertex has been tested.
			boolean[] bRandomVertexArray = new boolean[iTotalVertexSize]; 
			for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
				bRandomVertexArray[iIndex] = false;
			}
			
			if ( iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex] == iMaxNumOfVertex ) {
				// Perform the test for each vertex (up to iNumOfVertexToTest vertex).
				for (int iVertexRunIndex = 0 ; iVertexRunIndex < iMaxNumOfVertex ; iVertexRunIndex++){
					if ( iVertexRunIndex > 10 ) { // We do not perform the test on the top 10 vertex.
						iHop = GetNumberHopsNormalized(myGraph, iVertexRunIndex, false );
						if (iHop > 0 ) { // -1 indicates that the vertex failed to reach top 10 vertex ; 0 indicates that the vertex already reach top 10 vertex.
							iReachTop++;
							iTotalHop+=iHop;
							if ( iHop > iMaxHop )
								iMaxHop = iHop;
							if ( iHop < iMinHop )
								iMinHop = iHop;
						}
					}														
				}
			}
			else {
				// Generate random integers in range 0 to myGraph.vertexSet().size()
				Random rand = new Random(iTotalVertexSize);
				int iRandomVertex = rand.nextInt(iTotalVertexSize);					
						 
				// Perform the test for each vertex (up to iNumOfVertexToTest vertex).
				for (;;) {
					if ( bRandomVertexArray[iRandomVertex] == false ) { // Skip if the vertex is tested before.
						bRandomVertexArray[iRandomVertex] = true;
						iTestTop++;
						if ( iRandomVertex > 10 ) { // We do not perform the test on the top 10 vertex.
							iHop = GetNumberHopsNormalized(myGraph, iRandomVertex, false );
							if (iHop > 0 ) { // -1 indicates that the vertex failed to reach top 10 vertex ; 0 indicates that the vertex already reach top 10 vertex.
								iReachTop++;
								iTotalHop+=iHop;
								if ( iHop > iMaxHop )
									iMaxHop = iHop;
								if ( iHop < iMinHop )
									iMinHop = iHop;
							}
						}
					}
					// Stop the test if the number of test reached the number of vertex needed to be tested.
					if ( iTestTop >= iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex] ) 
						break;
					iRandomVertex = rand.nextInt(iTotalVertexSize);
				}
			}
			brResultFile.write("Normalized,"+iNumOfVertexToTestArray[iNumOfVertexToTestArrayIndex]+",false,"+iReachTop+","+iTotalHop*1.0/iReachTop+","+iMinHop+","+iMaxHop+"\n");
			brResultFile.flush();
			brPathFile.flush();
		}
		
		brPathFile.close();
		brResultFile.close();
		frPath.close();
		frResult.close();
        
		Date stopdate = new Date();
		System.out.println("Stop: " + dateFormat.format(stopdate));		   
        } catch (IOException e) {
            e.printStackTrace();
        }		
	}
	
	private static int GetNumberHops(Graph<Integer, DefaultEdge> myGraph, int iSourceVertex, double[] fVertexMatrixArray , boolean bTopThree) {
		try {
		// Prepare the boolean array to indicate if the vertex is your neighbor.
		int iTotalVertexSize = myGraph.vertexSet().size();		
		boolean[] bNBArray = new boolean[iTotalVertexSize]; 
		boolean[] bPotentialNBArray = new boolean[iTotalVertexSize]; 
		for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
			bNBArray[iIndex] = false;
			bPotentialNBArray[iIndex] = false;
		}
		
		boolean bAnyVertex = true;
		Vector<Integer> vNB = new Vector<>(); 
		Vector<Integer> vPotentialNB = new Vector<>(); 
		int iInviteSent = 0;
		int iRoundOfInvite = 0;
		
		// Add current neighbours into the NB list.
		bNBArray[iSourceVertex] = true;
		brPathFile.write("\n\""+iSourceVertex+"\":");
		for (Integer p : Graphs.neighborListOf(myGraph, iSourceVertex)) {
			vNB.add(p);
			bNBArray[p] = true;
		}
		if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == false ) {	
			do {				
				// Prepare the vector for computing the top degree potential neighbor.
				vPotentialNB.clear();
				for (int iNBIndex = 0 ; iNBIndex < vNB.size() ; iNBIndex++ ) {
					int iCurrentNBVertex = vNB.get(iNBIndex);
					// Add neighbors' neighbor into the potential list.
					for (Integer p : Graphs.neighborListOf(myGraph, iCurrentNBVertex)) {
						if ( bNBArray[p] == true ) // Skip if this vertex is already in the neighbor list.
							continue;
						if (bPotentialNBArray[p] == false) {
							vPotentialNB.add(p);
							bPotentialNBArray[p] = true;
						}
					}					
				}
				
				// Get the top degree potential neighbor.
				int iFirstNB = -1; double fFirstDegree = 0;
				int iSecondNB = -1; double fSecondDegree = 0;
				int iThirdNB = -1; double fThirdDegree = 0;			
				for (int iPotentialNBIndex = 0 ; iPotentialNBIndex < vPotentialNB.size() ; iPotentialNBIndex++ ) {
					int iPotentialNBVertex = vPotentialNB.get(iPotentialNBIndex);
					bPotentialNBArray[iPotentialNBVertex] = false;
					if ( fVertexMatrixArray[iPotentialNBVertex] > fFirstDegree ) {					
						iThirdNB = iSecondNB; fThirdDegree = fSecondDegree;
						iSecondNB = iFirstNB ; fSecondDegree = fFirstDegree;
						iFirstNB = iPotentialNBVertex ; fFirstDegree = fVertexMatrixArray[iPotentialNBVertex];
					}
					else if ( fVertexMatrixArray[iPotentialNBVertex] > fSecondDegree ) {					
						iThirdNB = iSecondNB; fThirdDegree = fSecondDegree;
						iSecondNB = iPotentialNBVertex ; fSecondDegree = fVertexMatrixArray[iPotentialNBVertex];
					}
					else if ( fVertexMatrixArray[iPotentialNBVertex] > fThirdDegree ) {					
						iThirdNB = iPotentialNBVertex ; fThirdDegree = fVertexMatrixArray[iPotentialNBVertex];
					}					
				}
				brPathFile.write(">");
				if ( iFirstNB != -1 ) { // First potential neighbor exists.
					iInviteSent++;		// Send invite to the potential neighbor.
					bNBArray[iFirstNB] = true; // Add potential neighbor into neighbor vector. i.e. Potential neighbor is my neighbor now.
					vNB.add(iFirstNB);
					brPathFile.write("\""+iFirstNB+"\"");
					if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == true ) {
						brPathFile.write("!Reached TOP!");
						break;
					}
				}
				if ( bTopThree == true ) {
					if ( iSecondNB != -1 ) {
						iInviteSent++;
						bNBArray[iSecondNB] = true;	
						vNB.add(iSecondNB);
						brPathFile.write("\""+iSecondNB+"\"");
						if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == true ){
							brPathFile.write("!Reached TOP!");
							break;
						}
					}
					
					if ( iThirdNB != -1 ) {
						iInviteSent++;
						bNBArray[iThirdNB] = true;	
						vNB.add(iThirdNB);
						brPathFile.write("\""+iThirdNB+"\"");
						if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == true ){
							brPathFile.write("!Reached TOP!");
							break;
						}
					}
					
					if ( iFirstNB == -1 && iSecondNB == -1 && iThirdNB == -1)
						break;
				}
				else {
				if ( iFirstNB == -1 )
					break;
				}
			}while (++iRoundOfInvite < NUM_ROUND_OF_INVITE );
		}
		else {
			brPathFile.write(">!Reached TOP!");
		}
		
		if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == false ){
			brPathFile.write("!Failed TOP!");
			iInviteSent = -1;
		}
			
		return iInviteSent;
		} catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
	}
	
	private static boolean NeighborOfTop10Vertex(boolean[] bNBArray, boolean bAnyFlag) { // Not bAnyFlag == All Top Vertex must exist.
		if (bAnyFlag)
			return 	bNBArray[Top10Vertex[0]] || bNBArray[Top10Vertex[1]] || bNBArray[Top10Vertex[2]] || bNBArray[Top10Vertex[3]] || 
					bNBArray[Top10Vertex[4]] || bNBArray[Top10Vertex[5]] || bNBArray[Top10Vertex[6]] || bNBArray[Top10Vertex[7]] || 
					bNBArray[Top10Vertex[8]] || bNBArray[Top10Vertex[9]];
		else 
			return 	bNBArray[Top10Vertex[0]] && bNBArray[Top10Vertex[1]] && bNBArray[Top10Vertex[2]] && bNBArray[Top10Vertex[3]] && 
					bNBArray[Top10Vertex[4]] && bNBArray[Top10Vertex[5]] && bNBArray[Top10Vertex[6]] && bNBArray[Top10Vertex[7]] && 
					bNBArray[Top10Vertex[8]] && bNBArray[Top10Vertex[9]];
	}
	
	public static void SampleGraph(Graph<Integer, DefaultEdge> myGraph) {
		try{
			String strLine;
			File fileSampleEdge = new File("SampleEdge_" + iMaxNumOfVertex +".csv");
			File fileSampleVertex = new File("SampleVertex_" + iMaxNumOfVertex +".csv");
			FileWriter frSampleEdge = new FileWriter(fileSampleEdge);
			FileWriter frSampleVertex = new FileWriter(fileSampleVertex);
			BufferedWriter brSampleEdgeFile = new BufferedWriter(frSampleEdge);
			BufferedWriter brSampleVertexFile = new BufferedWriter(frSampleVertex);
			int iTotalEdges = 0;
			int iTotalVertex = 0;
						
			int iTotalVertexSize = 6726290;
			int iVertexArray[] = new int[iTotalVertexSize];
			for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
				iVertexArray[iIndex] = -1;
			}
			Random rand = new Random(iTotalVertexSize);
			int iVertex = 0;
			for ( int iIndex = 0 ; iIndex < iMaxNumOfVertex ; iIndex++ ) {
				iVertex = rand.nextInt(iTotalVertexSize);
				if ( iVertexArray[iVertex] == -1 ) { //new vertex
					myGraph.addVertex(iVertex);
					iVertexArray[iVertex] = iIndex;
					brSampleVertexFile.write(iIndex+" "+iVertex+"\n");
					iTotalVertex++;
				}
				else // try again
					iIndex--;
			}
			
			FileInputStream fstream = new FileInputStream("C:\\App\\linkedin.edges");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));		
			int iSource, iDest = 0;
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(" ");
				try {
					iSource = Integer.parseInt(tokens[0]);
					iDest =  Integer.parseInt(tokens[1]);
					if ( iVertexArray[iSource] == -1 || iVertexArray[iDest] == -1 )
						continue;
					myGraph.addEdge(iVertexArray[iSource] ,iVertexArray[iDest]); //0 2152448
					brSampleEdgeFile.write( iVertexArray[iSource]+" "+ iVertexArray[iDest]+"\n");
					iTotalEdges++;
				}
				catch (IllegalArgumentException iae) {					
				}
			}
			brSampleEdgeFile.close();
			brSampleVertexFile.close();
			br.close();
			in.close();
			fstream.close();				
			System.out.println("Created: " + fileSampleEdge.getAbsolutePath());
			System.out.println("Created: " + fileSampleVertex.getAbsolutePath());
			System.out.println("Total Vertex: "+ iTotalVertex);
			System.out.println("Total Edge: "+ iTotalEdges);
	
		} catch (Exception e){
			System.err.println("Error reading file. " + e.getMessage());
		}		
	}
	
	public static void SampleFirstGraph(Graph<Integer, DefaultEdge> myGraph) {
		try{
			String strLine;
			File fileSampleEdge = new File("SampleFirstEdge_" + iMaxNumOfVertex +".csv");
			File fileSampleVertex = new File("SampleFirstVertex_" + iMaxNumOfVertex +".csv");
			FileWriter frSampleEdge = new FileWriter(fileSampleEdge);
			FileWriter frSampleVertex = new FileWriter(fileSampleVertex);
			BufferedWriter brSampleEdgeFile = new BufferedWriter(frSampleEdge);
			BufferedWriter brSampleVertexFile = new BufferedWriter(frSampleVertex);
			int iTotalEdges = 0;
			int iTotalVertex = 0;
						
			int iTotalVertexSize = 6726290;
			int iVertexArray[] = new int[iTotalVertexSize];
			for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
				iVertexArray[iIndex] = -1;
			}
			
			for ( int iIndex = 0 ; iIndex < iMaxNumOfVertex ; iIndex++ ) {
				myGraph.addVertex(iIndex);
				iVertexArray[iIndex] = iIndex;
				brSampleVertexFile.write(iIndex+" "+iIndex+"\n");
			}
			
			FileInputStream fstream = new FileInputStream("C:\\App\\linkedin.edges");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));		
			int iSource, iDest = 0;
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(" ");
				try {
					iSource = Integer.parseInt(tokens[0]);
					iDest =  Integer.parseInt(tokens[1]);
					if ( iVertexArray[iSource] == -1 || iVertexArray[iDest] == -1 )
						continue;
					myGraph.addEdge(iVertexArray[iSource] ,iVertexArray[iDest]); //0 2152448
					brSampleEdgeFile.write( iVertexArray[iSource]+" "+ iVertexArray[iDest]+"\n");
					iTotalEdges++;
				}
				catch (IllegalArgumentException iae) {					
				}
			}
			brSampleEdgeFile.close();
			brSampleVertexFile.close();
			br.close();
			in.close();
			fstream.close();				
			System.out.println("Created: " + fileSampleEdge.getAbsolutePath());
			System.out.println("Created: " + fileSampleVertex.getAbsolutePath());
			System.out.println("Total Vertex: "+ iTotalVertex);
			System.out.println("Total Edge: "+ iTotalEdges);
	
		} catch (Exception e){
			System.err.println("Error reading file. " + e.getMessage());
		}		
	}
	
	
	private static void BuildGraph(Graph<Integer, DefaultEdge> myGraph) {
		try{
			FileInputStream fstream = new FileInputStream(strVertexFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int iLineIndex = 0;
			
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split("\t| ");
				myGraph.addVertex(Integer.parseInt(tokens[0])); //0	pub-sandra-arana-querol-2b-561-73 ; The space is a tag.
				if ( ++iLineIndex >= iMaxNumOfVertex )
					break;
			}
			br.close();
			in.close();
			fstream.close();			

			//Total vertex: 6,726,290
			System.out.println("Total vertex: " + myGraph.vertexSet().size() + "\n");
			
			fstream = new FileInputStream(strEdgeFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));		
			while ((strLine = br.readLine()) != null)   {
				String[] tokens = strLine.split(" ");
				try {
					myGraph.addEdge(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1])); //0 2152448
				}
				catch (IllegalArgumentException iae) {
					
				}
			}
			br.close();
			in.close();
			fstream.close();			
			
			// Total edges: 19,360,690
			System.out.println("Total edges: " + myGraph.edgeSet().size() +"\n");	
		} catch (Exception e){
			System.err.println("Error reading file. " + e.getMessage());			
		}		
	}
	
	private static void SortArray(double[] sourceArrary) {	
		try {
		double[] copiedArray = new double[sourceArrary.length];
		System.arraycopy(sourceArrary, 0, copiedArray, 0, sourceArrary.length);
		Arrays.sort(copiedArray);	
		int iIndex = 0;
		for (int i = copiedArray.length - 1; i > copiedArray.length - 11 ; i--) {		
			//brResultFile.write("Top 10:" + copiedArray[i]+"\n");
			Top10VertexScore[iIndex++]=copiedArray[i];
			//brResultFile.flush();		
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void ComputeTopVertex(double[] sourceArrary) {	
		int iArraySize = sourceArrary.length;
		SortArray(sourceArrary);
		boolean fSourceArrayFlag[] = new boolean[iArraySize];
		for ( int iIndex = 0 ; iIndex < iArraySize ; iIndex++ )
			fSourceArrayFlag[iIndex] = false;		
		for ( int iIndex = 0 ; iIndex < 10 ; iIndex++ ) {
			for ( int iArrayIndex = 0 ; iArrayIndex < iArraySize ; iArrayIndex++ )
			{
				if ( sourceArrary[iArrayIndex] == Top10VertexScore[iIndex] && fSourceArrayFlag[iArrayIndex] == false) {
					Top10Vertex[iIndex] = iArrayIndex;
					fSourceArrayFlag[iArrayIndex] = true;
					break;
				}
			}
		}		
	}
	
	@SuppressWarnings("unchecked")
	private static void DegreeOfSeparation(Graph<Integer, DefaultEdge> myGraph) {
		try {
		// From the top connected (highest degree) vertex, can they reach everyone within 8 hops?
		File fileResultDOS = new File("DegreeOfSeparation_" + iMaxNumOfVertex +".csv");
	    FileWriter frResultDOS = new FileWriter(fileResultDOS);
	    BufferedWriter brResultFileDOS = new BufferedWriter(frResultDOS);
	        
		int iTotalVertexSize = myGraph.vertexSet().size();
		Vector<Integer> vNB = new Vector<>(); 
		Vector<Integer> vNextNeighborList = new Vector<>(); 		
		int[] iVertexArray = new int[iTotalVertexSize];
		for (int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ )
			iVertexArray[iIndex] = 50;
		
		// Compute for the top 10 vertex.
		// Add the top 10 vertex and their connecting neigbor.
		for ( int iIndex = 0 ; iIndex < 10 ; iIndex++ ) {
			iVertexArray[Top10Vertex[iIndex]] = 0;
			for (Integer p : Graphs.neighborListOf(myGraph, Top10Vertex[iIndex])) {
				vNB.add(p);				
			}
		}
		
		// Compute connecting neigbor, up to 50 degree of separations.
		for ( int iCycle = 1 ; iCycle < 50 ; iCycle++ ) {		
			for (int iNBIndex = 0 ; iNBIndex < vNB.size() ; iNBIndex++ ) {
				int iCurrentVertex = vNB.get(iNBIndex);
				if ( iVertexArray[iCurrentVertex] == 99 || iVertexArray[iCurrentVertex] > iCycle ) {
					iVertexArray[iCurrentVertex] = iCycle;
					for (Integer p : Graphs.neighborListOf(myGraph, iCurrentVertex)) {
						vNextNeighborList.add(p);
					}
				}
			}
			if ( vNextNeighborList.size() == 0 )
				break;
			vNB.clear();
			vNB = (Vector<Integer>) vNextNeighborList.clone();
			vNextNeighborList.clear();				
		}
			
		
		// Count the number of edge needed to reach the other nodes. 
		// E.g. 0 = Node itself ; 1 = Directly connected ; 9 = Cannot reach the nodes ;  
		int[] iCounterArray = new int[51];
		for (int iVertexArrayIndex = 0 ; iVertexArrayIndex < iVertexArray.length ; iVertexArrayIndex++ ) {
			iCounterArray[iVertexArray[iVertexArrayIndex]]++;						
		}
		
		// Print the results.
		int iTotalCounter = 0;
		int iTotalDegree = 0;
		for (int iIndex= 0 ; iIndex < 50 ; iIndex++) {
			brResultFileDOS.write("DegreeOfSeparation,"+ iIndex + "," + iCounterArray[iIndex] +"\n");
			iTotalDegree += iIndex*iCounterArray[iIndex];
			iTotalCounter += iCounterArray[iIndex];
		}
		brResultFileDOS.write("DegreeOfSeparation,50," + iCounterArray[50] +"\n");
		brResultFileDOS.write("Average," + iTotalDegree*1.0/iTotalCounter +"\n");
		brResultFileDOS.write("Total vertex : " + iTotalCounter +"\n");
		brResultFileDOS.flush();
		brResultFileDOS.close();
		frResultDOS.close();
		}catch(Exception e) {
		}
	}
	
	private static int GetNumberHopsNormalized(Graph<Integer, DefaultEdge> myGraph, int iSourceVertex, boolean bTopThree) {
		try {
		// Prepare the boolean array to indicate if the vertex is your neighbor.
		int iTotalVertexSize = myGraph.vertexSet().size();		
		boolean[] bNBArray = new boolean[iTotalVertexSize]; 
		boolean[] bPotentialNBArray = new boolean[iTotalVertexSize]; 
		for ( int iIndex = 0 ; iIndex < iTotalVertexSize ; iIndex++ ) {
			bNBArray[iIndex] = false;
			bPotentialNBArray[iIndex] = false;
		}
		
		boolean bAnyVertex = true;
		Vector<Integer> vNB = new Vector<>(); 
		Vector<Integer> vPotentialNB = new Vector<>(); 
		int iInviteSent = 0;
		int iRoundOfInvite = 0;
		
		// Add current neighbours into the NB list.
		bNBArray[iSourceVertex] = true;
		brPathFile.write("\n\""+iSourceVertex+"\":");
		for (Integer p : Graphs.neighborListOf(myGraph, iSourceVertex)) {
			vNB.add(p);
			bNBArray[p] = true;
		}
		if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == false ) {	
			do {				
				// Prepare the vector for computing the top degree potential neighbor.
				vPotentialNB.clear();
				for (int iNBIndex = 0 ; iNBIndex < vNB.size() ; iNBIndex++ ) {
					int iCurrentNBVertex = vNB.get(iNBIndex);
					// Add neighbors' neighbor into the potential list.
					for (Integer p : Graphs.neighborListOf(myGraph, iCurrentNBVertex)) {
						if ( bNBArray[p] == true ) // Skip if this vertex is already in the neighbor list.
							continue;
						if (bPotentialNBArray[p] == false) {
							vPotentialNB.add(p);
							bPotentialNBArray[p] = true;
						}
					}					
				}
				
				// Get the top degree potential neighbor.
				int[] iFirstNBArray = new int[6]; double[] fFirstDegreeArray = new double[6]; double[] fFirstDeltaArray = new double[6];
				int[] iSecondNBArray = new int[6]; double[] fSecondDegreeArray = new double[6]; double[] fSecondDeltaArray = new double[6];
				int[] iThirdNBArray = new int[6]; double[] fThirdDegreeArray = new double[6]; double[] fThirdDeltaArray = new double[6];								
				for ( int iIndex = 0 ; iIndex < 6 ; iIndex++ ) {
					iFirstNBArray[iIndex] = -1; fFirstDegreeArray[iIndex] = 0; fFirstDeltaArray[iIndex] = 0;
					iSecondNBArray[iIndex] = -1; fSecondDegreeArray[iIndex] = 0; fSecondDeltaArray[iIndex] = 0;
					iThirdNBArray[iIndex] = -1; fThirdDegreeArray[iIndex] = 0; fThirdDeltaArray[iIndex] = 0;
				}
								
				for (int iPotentialNBIndex = 0 ; iPotentialNBIndex < vPotentialNB.size() ; iPotentialNBIndex++ ) {
					int iPotentialNBVertex = vPotentialNB.get(iPotentialNBIndex);
					bPotentialNBArray[iPotentialNBVertex] = false;
					for ( int iIndex = 0 ; iIndex < 6 ; iIndex++ ) {
						if ( fVertexMatrixArrays[iIndex][iPotentialNBVertex] > fFirstDegreeArray[iIndex] ) {					
							iThirdNBArray[iIndex] = iSecondNBArray[iIndex]; fThirdDegreeArray[iIndex] = fSecondDegreeArray[iIndex];
							iSecondNBArray[iIndex] = iFirstNBArray[iIndex] ; fSecondDegreeArray[iIndex] = fFirstDegreeArray[iIndex];
							iFirstNBArray[iIndex] = iPotentialNBVertex ; fFirstDegreeArray[iIndex] = fVertexMatrixArrays[iIndex][iPotentialNBVertex];
							fFirstDeltaArray[iIndex] = (fFirstDegreeArray[iIndex]-fSecondDegreeArray[iIndex]) / fSecondDegreeArray[iIndex];
						}
						else if ( fVertexMatrixArrays[iIndex][iPotentialNBVertex] > fSecondDegreeArray[iIndex] ) {					
							iThirdNBArray[iIndex] = iSecondNBArray[iIndex]; fThirdDegreeArray[iIndex] = fSecondDegreeArray[iIndex];
							iSecondNBArray[iIndex] = iPotentialNBVertex ; fSecondDegreeArray[iIndex] = fVertexMatrixArrays[iIndex][iPotentialNBVertex];
							fSecondDeltaArray[iIndex] = (fSecondDegreeArray[iIndex]-fThirdDegreeArray[iIndex]) / fThirdDegreeArray[iIndex];
						}
						else if ( fVertexMatrixArrays[iIndex][iPotentialNBVertex] > fThirdDegreeArray[iIndex] ) {
							double dTemp = fThirdDegreeArray[iIndex];
							iThirdNBArray[iIndex] = iPotentialNBVertex ; fThirdDegreeArray[iIndex] = fVertexMatrixArrays[iIndex][iPotentialNBVertex];
							fThirdDeltaArray[iIndex] = (fThirdDegreeArray[iIndex]-dTemp) / dTemp;
						}
					}
				}
				brPathFile.write(">");				
				
				// Choose the algorithm that provides the largest delta percentage.
				int iFirstNB = iFirstNBArray[0]; int iSecondNB = iSecondNBArray[0]; int iThirdNB = iThirdNBArray[0];
				for ( int iIndex = 0 ; iIndex < 5 ; iIndex++ ) {
					if (fFirstDeltaArray[iIndex+1]> fFirstDeltaArray[iIndex]) {
						iFirstNB = iFirstNBArray[iIndex];
					}
					if (fSecondDeltaArray[iIndex+1]> fSecondDeltaArray[iIndex]) {
						iSecondNB = iSecondNBArray[iIndex];
					}
					if (fThirdDeltaArray[iIndex+1]> fThirdDeltaArray[iIndex]) {
						iThirdNB = iThirdNBArray[iIndex];
					}
				}
				if ( iFirstNB != -1 ) { // First potential neighbor exists.
					iInviteSent++;		// Send invite to the potential neighbor.
					bNBArray[iFirstNB] = true; // Add potential neighbor into neighbor vector. i.e. Potential neighbor is my neighbor now.
					vNB.add(iFirstNB);
					brPathFile.write("\""+iFirstNB+"\"");
					if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == true ) {
						brPathFile.write("!Reached TOP!");
						break;
					}
				}
				if ( bTopThree == true ) {
					if ( iSecondNB != -1 ) {
						iInviteSent++;
						bNBArray[iSecondNB] = true;	
						vNB.add(iSecondNB);
						brPathFile.write("\""+iSecondNB+"\"");
						if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == true ){
							brPathFile.write("!Reached TOP!");
							break;
						}
					}
					
					if ( iThirdNB != -1 ) {
						iInviteSent++;
						bNBArray[iThirdNB] = true;	
						vNB.add(iThirdNB);
						brPathFile.write("\""+iThirdNB+"\"");
						if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == true ){
							brPathFile.write("!Reached TOP!");
							break;
						}
					}
					
					if ( iFirstNB == -1 && iSecondNB == -1 && iThirdNB == -1)
						break;
				}
				else {
				if ( iFirstNB == -1 )
					break;
				}
			}while (++iRoundOfInvite < NUM_ROUND_OF_INVITE );
		}
		else {
			brPathFile.write(">!Reached TOP!");
		}
		
		if ( NeighborOfTop10Vertex(bNBArray, bAnyVertex) == false ){
			brPathFile.write("!Failed TOP!");
			iInviteSent = -1;
		}
			
		return iInviteSent;
		} catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
	}
}
