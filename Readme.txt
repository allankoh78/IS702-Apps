To setup: 
1. Create the folder C:\App
2. Unzip the content into the folder. Ensure App.jar is in the folder.
3. Download and unzip linkedin.tar.gz from https://www.aminer.cn/data-sna#Linkedin. This creates the linkedin.edges and linkedin.nodes data files.
4. For sample graph, the data file names must be Input.edges and Input.nodes.

Note that all data files must be in C:\App.

To run: java -Xms512m -Xmx12G -jar App.jar <NumOfVertex> <Option> 
Option: 1 - Compute degree of separation using original graph.
Option: 2 - Compute degree of separation using sample subgraph.
Option: 3 - Sample subgraph that contains 'NumOfVertex' vertexes.
Option: 4 - Sample subgraph that contains first 'NumOfVertex' vertexes.
Option: 5 - Run the tests using original graph.
Option: 6 - Run the tests using sample subgraph.

E.g.	cd C:\Users\allan\eclipse-workspace\IS709
	"C:\Program Files\Java\jdk-13.0.1\bin\java" -Xms512m -Xmx12G -jar "App.jar" 100000 6