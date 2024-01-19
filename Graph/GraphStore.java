package com.github.ryan6073.Seriously.Graph;

import com.github.ryan6073.Seriously.BasicInfo.Author;
import com.github.ryan6073.Seriously.BasicInfo.ConfigReader;
import com.github.ryan6073.Seriously.BasicInfo.Edge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.HashMap;
import java.util.Map;

public class GraphStore {
    private final Driver driver;

    public Driver getDriver() {
        return driver;
    }

    public GraphStore(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
    public void close() {
        driver.close();
    }
    public void saveGraphToNeo4j(String graphName, DirectedGraph<Author, Edge> graph) {
        try (Session session = driver.session()) {
            // 遍历图中的节点并创建节点
            for (Author author : graph.vertexSet()) {
                session.run("CREATE (:Author {name: $name, graphName: $graphName})", Values.parameters("name", author.getOrcid(),
                        "graphName", graphName));
            }
            // 遍历图中的边并创建关系
            for (Edge edge : graph.edgeSet()) {
                Author source = graph.getEdgeSource(edge);
                Author target = graph.getEdgeTarget(edge);

                session.run("MATCH (source:Author {name: $sourceName, graphName: $graphName}), (target:Author {name: $targetName, graphName: $graphName}) " +
                                "CREATE (source)-[:CITES {graphName: $graphName}]->(target)",
                        Values.parameters("sourceName", source.getOrcid(), "targetName", target.getOrcid(), "graphName", graphName));
            }
        }
    }

    public static void store(String graphName, DirectedGraph<Author, Edge> graph) {
        GraphStore graphStorage = new GraphStore("bolt://localhost:7687", ConfigReader.getUser(), ConfigReader.getPassword());//这里修改为自己的用户名，密码
        graphStorage.saveGraphToNeo4j(graphName, graph);
        graphStorage.close();
    }

    public DirectedGraph<Author, Edge> readGraphFromNeo4j(String graphName) {
        DirectedGraph<Author, Edge> graph = new DefaultDirectedGraph<>(Edge.class);

        Map<String, Author> authors = new HashMap<>();
        try (Session session = driver.session()) {
            // Read authors from Neo4j
            Result authorResult = session.run("(a:Author {graphName: $graphName}) RETURN a.name AS name", Values.parameters("graphName", graphName));
            while (authorResult.hasNext()) {
                Record record = authorResult.next();
                String name = record.get("name").asString();
                String orcid = record.get("orcid").asString();
                String institution = record.get("institution").asString();
                Author author = new Author(name,orcid,institution); // Assuming Author has a constructor that accepts a name
                System.out.println("orcid:" + orcid);
                authors.put(name, author);
                graph.addVertex(author);
            }

            // Read relationships from Neo4j
            Result edgeResult = session.run("MATCH (source:Author {graphName: $graphName})-[r:CITES {graphName: $graphName}]->(target:Author {graphName: $graphName}) " +
                            "RETURN source.name AS sourceName, target.name AS targetName, r.year AS year, r.citingKey AS citingKey, r.doi AS doi",
                    Values.parameters("graphName", graphName));
            while (edgeResult.hasNext()) {
                Record record = edgeResult.next();
                Author source = authors.get(record.get("sourceName").asString());
                Author target = authors.get(record.get("targetName").asString());
                String doi = record.get("doi").asString();
                String citingDoi = record.get("citingDoi").asString();
                double citingKey = record.get("citingKey").asDouble();
                int year = record.get("year").asInt();
                graph.addEdge(source, target, new Edge(citingKey,year,doi,citingDoi)); // Assuming Edge has a default constructor
            }
        }
        return graph;
    }

    public static DirectedGraph<Author, Edge> read(String graphName) {
        GraphStore graphStorage = new GraphStore("bolt://localhost:7687", ConfigReader.getUser(), ConfigReader.getPassword());//这里修改为自己的用户名，密码
        DirectedGraph<Author, Edge> graph = graphStorage.readGraphFromNeo4j(graphName);
        graphStorage.close();
        return graph;
    }

    public static void createTestGraph(){
        DirectedGraph<Author, Edge> testGraph1 = new DefaultDirectedGraph<>(Edge.class);
        DirectedGraph<Author, Edge> testGraph2 = new DefaultDirectedGraph<>(Edge.class);
        for(int i=1;i<=5;i++){
            Author author = new Author("author"+i, String.valueOf(i),"institution"+i);
            testGraph1.addVertex(author);
            testGraph2.addVertex(author);
        }
        for(Author author1:testGraph2.vertexSet()){
            for(Author author2:testGraph2.vertexSet()){
                if(author1.getOrcid().equals("1")){
                    if(author2.getOrcid().equals("2")) {
                        Edge edge = new Edge(0.5, 2024, "1", 1);
                        testGraph2.addEdge(author1, author2, edge);
                    }
                    if(author2.getOrcid().equals("3")) {
                        Edge edge = new Edge(0.5, 2023, "2", 2);
                        testGraph2.addEdge(author1, author2, edge);
                    }
                }
                if(author1.getOrcid().equals("3")){
                    if(author2.getOrcid().equals("4")) {
                        Edge edge = new Edge(0.5, 2022, "3", 3);
                        testGraph2.addEdge(author1, author2, edge);
                    }
                    if(author2.getOrcid().equals("5")) {
                        Edge edge = new Edge(0.5, 2021, "4", 4);
                        testGraph2.addEdge(author1, author2, edge);
                    }
                }
            }
        }
        GraphStore.store("test1", testGraph1);
        GraphStore.store("test2", testGraph2);
    }
}
