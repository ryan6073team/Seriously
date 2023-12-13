package com.github.ryan6073.Seriously.Graph;

import com.github.ryan6073.Seriously.BasicInfo.Author;
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

                session.run("MATCH (source:Author {name: $sourceName, graph: $graphName}), (target:Author {name: $targetName, graph: $graphName}) " +
                                "CREATE (source)-[:WRITES {graph: $graphName}]->(target)",
                        Values.parameters("sourceName", source.getOrcid(), "targetName", target.getOrcid(), "graphName", graphName));
            }
        }
    }

    public static void store(String graphName, DirectedGraph<Author, Edge> graph) {
        GraphStore graphStorage = new GraphStore("bolt://localhost:7687", "neo4j", "密码");//这里修改为自己的用户名，密码
        graphStorage.saveGraphToNeo4j(graphName, graph);
        graphStorage.close();
    }

    public DirectedGraph<Author, Edge> readGraphFromNeo4j(String graphName) {
        DirectedGraph<Author, Edge> graph = new DefaultDirectedGraph<>(Edge.class);

        Map<String, Author> authors = new HashMap<>();
        try (Session session = driver.session()) {
            // Read authors from Neo4j
            Result authorResult = session.run("(a:Author {graph: $graphName}) RETURN a.name AS name", Values.parameters("graphName", graphName));
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
            Result edgeResult = session.run("MATCH (source:Author {graph: $graphName})-[r:WRITES {graph: $graphName}]->(target:Author {graph: $graphName}) " +
                    "RETURN source.name AS sourceName, target.name AS targetName, r.year AS year, r.citingKey AS citingKey, r.doi AS doi",
                    Values.parameters("graphName", graphName));
            while (edgeResult.hasNext()) {
                Record record = edgeResult.next();
                Author source = authors.get(record.get("sourceName").asString());
                Author target = authors.get(record.get("targetName").asString());
                String doi = record.get("doi").asString();
                double citingKey = record.get("citingKey").asDouble();
                int year = record.get("year").asInt();
                graph.addEdge(source, target, new Edge(citingKey,year,doi)); // Assuming Edge has a default constructor
            }
        }
        return graph;
    }

    public static DirectedGraph<Author, Edge> read(String graphName) {
        GraphStore graphStorage = new GraphStore("bolt://localhost:7687", "neo4j", "密码");//这里修改为自己的用户名，密码
        DirectedGraph<Author, Edge> graph = graphStorage.readGraphFromNeo4j(graphName);
        graphStorage.close();
        return graph;
    }


}
