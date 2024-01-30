package com.github.ryan6073.Seriously.Graph;

import com.github.ryan6073.Seriously.BasicInfo.Author;
import com.github.ryan6073.Seriously.BasicInfo.ConfigReader;
import com.github.ryan6073.Seriously.BasicInfo.Edge;
import org.jgrapht.graph.DirectedPseudograph;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public void saveGraphToNeo4j(String graphName, DirectedPseudograph<Author, Edge> graph) {
        try (Session session = driver.session()) {
            // 遍历图中的节点并创建节点
            for (Author author : graph.vertexSet()) {
                List<String> institutions = new ArrayList<>(author.getAuthorInstitutions());
                session.run("CREATE (:Author {authorName: $authorName, orcid: $orcid, level: $level, ifExist: $ifExist, authorInstitutions: $authorInstitutions, authorImpact: $authorImpact, flag: $flag})",
                        Values.parameters("authorName", author.getAuthorName(),
                                "orcid", author.getOrcid(),
                                "level", author.getLevel().toString(),
                                "ifExist", author.getIfExist(),
                                "authorInstitutions", institutions,
                                "authorImpact", author.getAuthorImpact(),
                                "flag", author.getFlag()));
            }
            // 遍历图中的边并创建关系
            // 创建 Edge 关系
            for (Edge edge : graph.edgeSet()) {
                Author source = graph.getEdgeSource(edge);
                Author target = graph.getEdgeTarget(edge);

                session.run("MATCH (source:Author {orcid: $sourceOrcid}), (target:Author {orcid: $targetOrcid}) " +
                                "CREATE (source)-[:CITES {citingKey: $citingKey, doi: $doi, year: $year, month: $month, citingDoi: $citingDoi}]->(target)",
                        Values.parameters("sourceOrcid", source.getOrcid(), "targetOrcid", target.getOrcid(),
                                "citingKey", edge.getCitingKey(), "doi", edge.getDoi(),
                                "year", edge.getYear(), "month", edge.getMonth(),
                                "citingDoi", edge.getCitingDoi()));
            }
        }
    }

    public static void store(String graphName, DirectedPseudograph<Author, Edge> graph) {
        GraphStore graphStorage = new GraphStore("bolt://localhost:7687", ConfigReader.getUser(), ConfigReader.getPassword());//这里修改为自己的用户名，密码
        graphStorage.saveGraphToNeo4j(graphName, graph);
        graphStorage.close();
    }

    public DirectedPseudograph<Author, Edge> readGraphFromNeo4j(String graphName) {
        DirectedPseudograph<Author, Edge> graph = new DirectedPseudograph<>(Edge.class);

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

    public static DirectedPseudograph<Author, Edge> read(String graphName) {
        GraphStore graphStorage = new GraphStore("bolt://localhost:7687", ConfigReader.getUser(), ConfigReader.getPassword());//这里修改为自己的用户名，密码
        DirectedPseudograph<Author, Edge> graph = graphStorage.readGraphFromNeo4j(graphName);
        graphStorage.close();
        return graph;
    }
}
