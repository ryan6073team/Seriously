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

import static org.neo4j.driver.Values.parameters;

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

    public static void deleteGraph(int year, int month) {
        String graphName = year + "---" + month;
        try (Session session = driver.session()) {
            String deleteQuery = "MATCH (n {graphName: $graphName}) DETACH DELETE n";
            session.run(deleteQuery, parameters("graphName", graphName));
        }
    }
    public static void storeGraph(String graphName, DirectedPseudograph<Author, Edge> graph){
        // 连接到默认数据库
        try (Session session = driver.session()){
            // 确保索引已经创建
            session.run("CREATE INDEX IF NOT EXISTS FOR (a:Author) ON (a.orcid)");
            session.run("CREATE INDEX IF NOT EXISTS FOR (a:Author) ON (a.graphName)");

            List<Map<String, Object>> authorsList = new ArrayList<>();
            List<Map<String, Object>> edgesList = new ArrayList<>();

// 填充作者节点数据
            for (Author author : graph.vertexSet()) {
                Map<String, Object> authorMap = new HashMap<>();
                List<String> institutions = new ArrayList<>(author.getAuthorInstitutions());
                List<Double> institutionsImpact = new ArrayList<>();
                for (String institution : institutions) {
                    institutionsImpact.add(DataGatherManager.getInstance().dicNameInstitutions.get(institution).getInstitutionImpact());
                }
                authorMap.put("graphName", graphName);
                authorMap.put("authorName", author.getAuthorName());
                authorMap.put("orcid", author.getOrcid());
                authorMap.put("level", author.getLevel().toString().charAt(0)-'A'+1);
                authorMap.put("ifExist", author.getIfExist());
                authorMap.put("authorInstitutions", institutions);
                authorMap.put("authorInstitutionsImpact", institutionsImpact);
                authorMap.put("authorImpact", author.getAuthorImpact());
                authorMap.put("flag", author.getFlag());
                authorsList.add(authorMap);
            }

// 填充边数据
            for (Edge edge : graph.edgeSet()) {
                Map<String, Object> edgeMap = new HashMap<>();
                Author source = graph.getEdgeSource(edge);
                Author target = graph.getEdgeTarget(edge);
                edgeMap.put("sourceOrcid", source.getOrcid());
                edgeMap.put("targetOrcid", target.getOrcid());
                edgeMap.put("citingKey", edge.getCitingKey());
                edgeMap.put("doi", edge.getDoi());
                edgeMap.put("year", edge.getYear());
                edgeMap.put("month", edge.getMonth());
                edgeMap.put("citingDoi", edge.getCitingDoi());
                edgeMap.put("paperImpact", DataGatherManager.getInstance().dicDoiPaper.get(edge.getDoi()).getPaperImpact());
                edgeMap.put("journalImpact", DataGatherManager.getInstance().dicNameJournal.get(DataGatherManager.getInstance().dicDoiPaper.get(edge.getDoi()).getJournal()).getJournalImpact());
                edgeMap.put("paperLife", DataGatherManager.getInstance().dicDoiPaper.get(edge.getDoi()).getLife());
                edgeMap.put("paperLevel", DataGatherManager.getInstance().dicDoiPaper.get(edge.getDoi()).getLevel().toString().charAt(0)-'A'+1);
                edgesList.add(edgeMap);
            }

// 构造并执行Cypher查询
            String cypherNodeQuery =
                            "UNWIND $authorsList AS author " +
                            "CREATE (a:Author {graphName: author.graphName, authorName: author.authorName, orcid: author.orcid, level: author.level, ifExist: author.ifExist, authorInstitutions: author.authorInstitutions, authorInstitutionsImpact: author.authorInstitutionsImpact, authorImpact: author.authorImpact, flag: author.flag}) ";
            String cypherEdgeQuery =
                            "UNWIND $edgesList AS edgeData " +
                            "MATCH (source:Author {orcid: edgeData.sourceOrcid, graphName: $graphName}), (target:Author {orcid: edgeData.targetOrcid, graphName: $graphName}) " +
                            "CREATE (source)-[:CITES {" +
                            "graphName: $graphName, " +
                            "citingKey: edgeData.citingKey, " +
                            "doi: edgeData.doi, " +
                            "year: edgeData.year, " +
                            "month: edgeData.month, " +
                            "citingDoi: edgeData.citingDoi, " +
                            "paperImpact: edgeData.paperImpact, " +
                            "journalImpact: edgeData.journalImpact, " +
                            "paperLife: edgeData.paperLife, " +
                            "paperLevel: edgeData.paperLevel}]->(target)";

            Map<String, Object> params = new HashMap<>();
            params.put("graphName", graphName); // 传入 graphName 参数
            params.put("authorsList", authorsList);
            params.put("edgesList", edgesList);

            session.run(cypherNodeQuery, params);
            session.run(cypherEdgeQuery,params);
        }
    }
//    public static void storeGraph(String graphName, DirectedPseudograph<Author, Edge> graph) {
//        // 连接到默认数据库
//        try (Session session = driver.session()) {
//            // 可选：清空特定图的数据（根据 graphName）
////          String clearGraphQuery = "MATCH (n {graphName: $graphName}) DETACH DELETE n";
////          session.run(clearGraphQuery, Values.parameters("graphName", graphName));
//
//            StringBuilder queryBuilder = new StringBuilder();
//
//            // 构建创建节点的Cypher语句
//            for (Author author : graph.vertexSet()) {
//                List<String> institutions = new ArrayList<>(author.getAuthorInstitutions());
//                List<Double> institutionsImpact = new ArrayList<>();
//                for (String institution : institutions)
//                    institutionsImpact.add(DataGatherManager.getInstance().dicNameInstitutions.get(institution).getInstitutionImpact());
//                String nodeQuery = String.format(
//                        "CREATE (:Author {graphName: '%s', authorName: '%s', orcid: '%s', level: '%s', ifExist: %d, authorInstitutions: [%s], authorInstitutionsImpact: [%f], authorImpact: %f, flag: %b}) ",
//                        graphName, author.getAuthorName(), author.getOrcid(), author.getLevel(), author.getIfExist(), institutions, institutionsImpact, author.getAuthorImpact(), author.getFlag()
//                );
//                queryBuilder.append(nodeQuery);
//            }
//
//            // 遍历图中的节点并创建节点
////            for (Author author : graph.vertexSet()) {
////                List<String> institutions = new ArrayList<>(author.getAuthorInstitutions());
////                List<Double> institutionsImpact = new ArrayList<>();
////                for (String institution : institutions)
////                    institutionsImpact.add(DataGatherManager.getInstance().dicNameInstitutions.get(institution).getInstitutionImpact());
////                session.run("CREATE (:Author {graphName: $graphName, authorName: $authorName, orcid: $orcid, level: $level, ifExist: $ifExist, authorInstitutions: $authorInstitutions, authorInstitutionsImpact: $authorInstitutionsImpact, authorImpact: $authorImpact, flag: $flag})",
////                        parameters("graphName", graphName,
////                                "authorName", author.getAuthorName(),
////                                "orcid", author.getOrcid(),
////                                "level", author.getLevel().toString().charAt(0)-'A'+1,
////                                "ifExist", author.getIfExist(),
////                                "authorInstitutions", institutions,
////                                "authorInstitutionsImpact", institutionsImpact,
////                                "authorImpact", author.getAuthorImpact(),
////                                "flag", author.getFlag()));
////            }
//            DataGatherManager dataGatherManager = DataGatherManager.getInstance();
//
//            // 构建创建边的Cypher语句
//            for (Edge edge : graph.edgeSet()) {
//                Author source = graph.getEdgeSource(edge);
//                Author target = graph.getEdgeTarget(edge);
//                String edgeQuery = String.format(
//                        "MATCH (source:Author {orcid: '%s'}), (target:Author {orcid: '%s'}) " +
//                                "CREATE (source)-[:CITES {graphName: '%s', citingKey: %f, doi: '%s', year: %d, month: %d, citingDoi: '%s', paperImpact: %f, journalImpact: %f, paperLife: %d, paperLevel: '%s'}]->(target) ",
//                        source.getOrcid(), target.getOrcid(), graphName, edge.getCitingKey(), edge.getDoi(), edge.getYear(), edge.getMonth(), edge.getCitingDoi(), edge.getPaperImpact(), edge.getJournalImpact(), edge.getPaperLife(), edge.getPaperLevel()
//                );
//                queryBuilder.append(edgeQuery);
//            }
//
//            // 遍历图中的边并创建关系
//            for (Edge edge : graph.edgeSet()) {
//                Author source = graph.getEdgeSource(edge);
//                Author target = graph.getEdgeTarget(edge);
//                if(dataGatherManager.dicDoiPaper.get(edge.getDoi()).getLevel()==null)
//                    System.out.println();
////                session.run("MATCH (source:Author {orcid: $sourceOrcid, graphName: $graphName}), " +
////                                "(target:Author {orcid: $targetOrcid, graphName: $graphName}) " +
////                                "CREATE (source)-[:CITES {" +
////                                "graphName: $graphName, " +
////                                "citingKey: $citingKey, " +
////                                "doi: $doi, " +
////                                "year: $year, " +
////                                "month: $month, " +
////                                "citingDoi: $citingDoi, " +
////                                "paperImpact: $paperImpact, " +
////                                "journalImpact: $journalImpact, " +
////                                "paperLife: $paperLife, " +
////                                "paperLevel: $paperLevel}]->(target)",
////                        parameters("graphName", graphName,
////                                "sourceOrcid", source.getOrcid(),
////                                "targetOrcid", target.getOrcid(),
////                                "citingKey", edge.getCitingKey(),
////                                "doi", edge.getDoi(),
////                                "year", edge.getYear(),
////                                "month", edge.getMonth(),
////                                "citingDoi", edge.getCitingDoi(),
////                                "paperImpact", dataGatherManager.dicDoiPaper.get(edge.getDoi()).getPaperImpact(),
////                                "journalImpact", dataGatherManager.dicNameJournal.get(dataGatherManager.dicDoiPaper.get(edge.getDoi()).getJournal()).getJournalImpact(),
////                                "paperLife", dataGatherManager.dicDoiPaper.get(edge.getDoi()).getLife(),
////                                "paperLevel", dataGatherManager.dicDoiPaper.get(edge.getDoi()).getLevel().toString().charAt(0)-'A'+1));
//
//            }
//
//        }
//    }

    public DirectedPseudograph<Author, Edge> readGraphFromNeo4j(String graphName) {
        DirectedPseudograph<Author, Edge> graph = new DirectedPseudograph<>(Edge.class);

        // 连接到默认数据库
        try (Session session = driver.session()) {
            // 读取 Author 节点
            String readAuthorsQuery = "MATCH (a:Author {graphName: $graphName}) RETURN a";
            Result authorResults = session.run(readAuthorsQuery, parameters("graphName", graphName));
            while (authorResults.hasNext()) {
                Record record = authorResults.next();
                Value aValue = record.get("a");
                Author author = DataGatherManager.getInstance().dicOrcidAuthor.get(aValue.get("orcid").asString());
                graph.addVertex(author);
            }

            String readEdgesQuery = "MATCH (source:Author {graphName: $graphName})-[r:CITES]->(target:Author {graphName: $graphName}) RETURN source, r, target";
            Result edgeResults = session.run(readEdgesQuery, parameters("graphName", graphName));
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
