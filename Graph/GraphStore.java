package com.github.ryan6073.Seriously.Graph;

import com.github.ryan6073.Seriously.BasicInfo.Author;
import com.github.ryan6073.Seriously.BasicInfo.Edge;
import org.jgrapht.DirectedGraph;
import org.neo4j.driver.*;

public class GraphStore {
    private final Driver driver;
    public GraphStore(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
    public void close() {
        driver.close();
    }
    public void saveGraphToNeo4j(DirectedGraph<Author, Edge> graph) {
        try (Session session = driver.session()) {
            // 遍历图中的节点并创建节点
            for (Author author : graph.vertexSet()) {
                session.run("CREATE (:Author {name: $name})", Values.parameters("name", author.getOrcid()));
            }
            // 遍历图中的边并创建关系
            for (Edge edge : graph.edgeSet()) {
                Author source = graph.getEdgeSource(edge);
                Author target = graph.getEdgeTarget(edge);

                session.run("MATCH (source:Author {name: $sourceName}), (target:Author {name: $targetName}) " +
                                "CREATE (source)-[:WRITES]->(target)",
                        Values.parameters("sourceName", source.getOrcid(), "targetName", target.getOrcid()));
            }
        }
    }

    public static void store(DirectedGraph<Author, Edge> graph) {
        GraphStore graphStorage = new GraphStore("bolt://localhost:7687", "username", "password");//这里修改为自己的用户名，密码
        graphStorage.saveGraphToNeo4j(graph);
        graphStorage.close();
    }
}
