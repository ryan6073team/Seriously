package com.github.ryan6073.Seriously.Graph;

import com.github.ryan6073.Seriously.BasicInfo.Author;
import com.github.ryan6073.Seriously.BasicInfo.ConfigReader;
import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.BasicInfo.Edge;
import org.jgrapht.graph.DirectedPseudograph;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphStore {
    private static Driver driver;

    private GraphStore(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
    private static GraphStore mGraphStore = new GraphStore("bolt://localhost:7687", ConfigReader.getUser(), ConfigReader.getPassword());//这里修改为自己的用户名，密码
    public static GraphStore getInstance(){return mGraphStore;}
    //每一次关闭都会重开一个driver
    public void renovateDriver() {
        driver.close();
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic(ConfigReader.getUser(), ConfigReader.getPassword()));
    }

    public void closeDriver(){
        driver.close();
    }
    public void saveGraphToNeo4j(String graphName, DirectedPseudograph<Author, Edge> graph) {
        // 连接到默认数据库
        try (Session session = driver.session()) {
            // 可选：清空特定图的数据（根据 graphName）
//            String clearGraphQuery = "MATCH (n {graphName: $graphName}) DETACH DELETE n";
//            session.run(clearGraphQuery, Values.parameters("graphName", graphName));

            // 遍历图中的节点并创建节点
            for (Author author : graph.vertexSet()) {
                List<String> institutions = new ArrayList<>(author.getAuthorInstitutions());
                session.run("CREATE (:Author {graphName: $graphName, authorName: $authorName, orcid: $orcid, level: $level, ifExist: $ifExist, authorInstitutions: $authorInstitutions, authorImpact: $authorImpact, flag: $flag})",
                        Values.parameters("graphName", graphName,
                                "authorName", author.getAuthorName(),
                                "orcid", author.getOrcid(),
                                "level", author.getLevel().toString(),
                                "ifExist", author.getIfExist(),
                                "authorInstitutions", institutions,
                                "authorImpact", author.getAuthorImpact(),
                                "flag", author.getFlag()));
            }

            // 遍历图中的边并创建关系
            for (Edge edge : graph.edgeSet()) {
                Author source = graph.getEdgeSource(edge);
                Author target = graph.getEdgeTarget(edge);

                session.run("MATCH (source:Author {orcid: $sourceOrcid, graphName: $graphName}), (target:Author {orcid: $targetOrcid, graphName: $graphName}) " +
                                "CREATE (source)-[:CITES {graphName: $graphName, citingKey: $citingKey, doi: $doi, year: $year, month: $month, citingDoi: $citingDoi}]->(target)",
                        Values.parameters("graphName", graphName,
                                "sourceOrcid", source.getOrcid(), "targetOrcid", target.getOrcid(),
                                "citingKey", edge.getCitingKey(), "doi", edge.getDoi(),
                                "year", edge.getYear(), "month", edge.getMonth(),
                                "citingDoi", edge.getCitingDoi()));
            }
        }



    }

    public static void store(String graphName, DirectedPseudograph<Author, Edge> graph) {
        GraphStore graphStorage = getInstance();
        graphStorage.saveGraphToNeo4j(graphName, graph);
    }

    public DirectedPseudograph<Author, Edge> readGraphFromNeo4j(String graphName) {
        DirectedPseudograph<Author, Edge> graph = new DirectedPseudograph<>(Edge.class);

        // 连接到默认数据库
        try (Session session = driver.session()) {
            // 读取 Author 节点
            String readAuthorsQuery = "MATCH (a:Author {graphName: $graphName}) RETURN a";
            Result authorResults = session.run(readAuthorsQuery, Values.parameters("graphName", graphName));
            while (authorResults.hasNext()) {
                Record record = authorResults.next();
                Value aValue = record.get("a");
                Author author = DataGatherManager.getInstance().dicOrcidAuthor.get(aValue.get("orcid").asString());
                graph.addVertex(author);
            }

            String readEdgesQuery = "MATCH (source:Author {graphName: $graphName})-[r:CITES]->(target:Author {graphName: $graphName}) RETURN source, r, target";
            Result edgeResults = session.run(readEdgesQuery, Values.parameters("graphName", graphName));
            while (edgeResults.hasNext()) {
                Record record = edgeResults.next();

                // 获取整个节点和关系对象
                Value sourceNode = record.get("source");
                Value targetNode = record.get("target");
                Value edgeRelation = record.get("r");

                // 从节点对象中提取属性
                String sourceAu = sourceNode.get("orcid").asString();
                String targetAu = targetNode.get("orcid").asString();

                // 从关系对象中提取属性
                String doi = edgeRelation.get("doi").asString();
                String citingDoi = edgeRelation.get("citingDoi").asString();
                Double citingKey = edgeRelation.get("citingKey").asDouble();
                int year = edgeRelation.get("year").asInt();

                // 创建 Edge 对象并添加到图中
                Edge edge = new Edge(citingKey, year, doi, citingDoi);
                graph.addEdge(DataGatherManager.getInstance().dicOrcidAuthor.get(sourceAu), DataGatherManager.getInstance().dicOrcidAuthor.get(targetAu), edge);
            }
        }

        return graph;
    }

    public static DirectedPseudograph<Author, Edge> read(String graphName) {
        GraphStore graphStorage = getInstance();
        DirectedPseudograph<Author, Edge> graph = graphStorage.readGraphFromNeo4j(graphName);
        return graph;
    }
}
