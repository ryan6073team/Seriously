package com.github.ryan6073.Seriously.Graph;
import Jama.Matrix;
import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Coefficient.CoefficientStrategy;
import com.github.ryan6073.Seriously.Impact.CalGraph;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.*;


public class GraphManager { //单例
    private static GraphManager mGraphManager = new GraphManager();
    public static GraphManager getInstance(){return mGraphManager;}
    private Map<TimeInfo,DirectedGraph<Author,Edge>> GraphItems = new HashMap<>();
    //获取目标子图
    public DirectedGraph<Author,Edge> getGraphItem(int year,int month){
        for(Map.Entry<TimeInfo,DirectedGraph<Author,Edge>> entry : GraphItems.entrySet()){
            if(entry.getKey().year==year&&entry.getKey().month==month)
                return entry.getValue();
        }
        return null;
    }
    public DirectedGraph<Author ,Edge> getMatureGraph(){
        DirectedGraph<Author ,Edge> graph = Graph;
        for(Edge edge:graph.edgeSet()){
            if(DataGatherManager.getInstance().dicDoiPaper.get(edge.getDoi()).getIsAlive()){
                graph.removeEdge(edge);
            }
        }
        return graph;
    }
    //加入目标子图
    public void addGraphItem(int year, int month, DirectedGraph<Author,Edge> Item){GraphItems.put(new TimeInfo(year,month),Item);}
    //创建初始图，一切故事从这里开始
    public DirectedGraph<Author,Edge> Graph = new DefaultDirectedGraph<>(Edge.class);
    //关于初始图和子图的创建时间参数请查看DataGatherManager新增的startYear/Month和finalYear/Month
    //注意这些图都是作者引用图，而GraphInit中的paperGraph是论文引用图
    private GraphManager(){}
    public DirectedGraph<Author,Edge> createNewGraph(Paper paper, DirectedGraph<Author,Edge> graph){
        for(Edge edge: paper.getEdgeList()){
            graph.removeEdge(edge);
        }
        return graph;
    }//生成删除了要研究论文的图
    public Map<String,Double> calNewPaperImp(DataGatherManager dataGatherManager,Paper paper, DirectedGraph<Author,Edge> graph){
        Map<String,Double> imp = new HashMap<>();
        for(String orcid : dataGatherManager.dicOrcidMatrixOrder.keySet()){
            imp.put(orcid,dataGatherManager.dicOrcidAuthor.get(orcid).getAuthorImpact());
        }
        CalImpact.initAuthorImpact(CalGraph.getGraphImpact(createNewGraph(paper, graph)));//更新删除了结点图的作者影响力
        Map<String, Double> itemsToRemove = new HashMap<>();
        for (String orcid : dataGatherManager.dicOrcidMatrixOrder.keySet()) {
            if (Objects.equals(dataGatherManager.dicOrcidAuthor.get(orcid).getAuthorImpact(), imp.get(orcid))) {
                itemsToRemove.put(orcid,imp.get(orcid));
            } else {
                imp.replace(orcid, imp.get(orcid) - dataGatherManager.dicOrcidAuthor.get(orcid).getAuthorImpact());
            }
        }
        for (String orcid : itemsToRemove.keySet()) {
            imp.remove(orcid);
        }
        CalImpact.initAuthorImpact(CalGraph.getGraphImpact(graph));//恢复原作者影响力
        return imp;
    }//计算删除了要研究论文的图的作者影响力
    public double[][] calAllPaperImp(DataGatherManager dataGatherManager,DirectedGraph<Author,Edge> graph){
        double [][] Matrix = new double[5][5];
        int [][] number = new int[5][5];
        Vector<Paper> papers = new Vector<>();
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                Matrix[i][j]=0.0;
                number[i][j]=0;
            }
        }
        for(Edge edge:graph.edgeSet()) {
            Paper paper = dataGatherManager.dicDoiPaper.get(edge.getDoi());
            if (!papers.contains(paper)) papers.add(paper);
        }
        for(Paper paper:papers){
            if(paper.getIsAlive()) continue;
            int paperRank = paper.getLevel().getIndex();
            Map<String,Double> imp = calNewPaperImp(dataGatherManager,paper,graph);
            for(String orcid: imp.keySet()){
                Author author = dataGatherManager.dicOrcidAuthor.get(orcid);
                int authorRank = author.getLevel().getIndex();
                Matrix[authorRank][paperRank] += imp.get(orcid);
                number[authorRank][paperRank]++;
            }
        }
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                if(number[i][j]!=0){
                    Matrix[i][j]/=number[i][j];
                }
            }
        }
        return Matrix;
    }//计算全部不在保护期的论文对作者的平均影响力
    //将year年month月的图更新到图里，同时更新Strategy的矩阵
    public Vector<Vector<String>> updateGraph(int year, int month){
        DirectedGraph<Author,Edge> graphItem = getGraphItem(year, month);

        if(graphItem == null) return updatePaperLifeInfo(Graph,DataGatherManager.getInstance());

        for(Author author:graphItem.vertexSet()){
            if(!Graph.containsVertex(author)){
                Graph.addVertex(author);
                author.setIfExist(1);
            }
        }
        for(Edge edge:graphItem.edgeSet()){
            Graph.addEdge(graphItem.getEdgeSource(edge),graphItem.getEdgeTarget(edge),edge);
            DataGatherManager.getInstance().dicDoiPaper.get(edge.getDoi()).setIsRead(1);
        }
        Vector<Vector<String>> ans = updatePaperLifeInfo(Graph,DataGatherManager.getInstance());
        //更新Strategy的矩阵
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        //更新该年论文集
        dataGatherManager.currentCoefficientStrategy.currentYearPapers.addAll(dataGatherManager.dicTimeInfoDoi.get(new TimeInfo(year,month)));
        if(month==12) {
            if(year== dataGatherManager.startYear){
                dataGatherManager.currentCoefficientStrategy.initorUpdateTransitionMatrixItems();
                dataGatherManager.currentCoefficientStrategy.initOtherMatrixs();
            }else {
                dataGatherManager.currentCoefficientStrategy.initorUpdateTransitionMatrixItems();
                dataGatherManager.currentCoefficientStrategy.updateOtherTransitionMatrixs();
            }
        }
        return ans;
    }
    //更新论文life，定期更新论文CitationLevel
    private Vector<Vector<String>> updatePaperLifeInfo(DirectedGraph<Author,Edge> graph, DataGatherManager dataGatherManager){
        Vector<Paper> papers = new Vector<>();
        for(Edge edge:graph.edgeSet()) {
            Paper paper = dataGatherManager.dicDoiPaper.get(edge.getDoi());
            if (!papers.contains(paper)) papers.add(paper);
        }
        Vector<String> alive = new Vector<>();
        Vector<String> dead = new Vector<>();
        for(Paper paper:papers){
            if(!paper.getIsAlive()) {
                dead.add(paper.getDoi());
                continue;
            }
            if (paper.getLife() <= 12) {
                paper.setLife(paper.getLife() + 1);
                int lifeSpan = paper.getLife();
                //更新论文的citationlevel
                if(lifeSpan==4)
                    paper.youthCitationLevel = CoefficientStrategy.getCitationLevel(paper);
                else if(lifeSpan==8)
                    paper.strongCitationLevel = CoefficientStrategy.getCitationLevel(paper);
                else if(lifeSpan==12)
                    paper.matureCitationLevel = CoefficientStrategy.getCitationLevel(paper);
            } // life+1
            if(paper.getLife() <= 12){
                alive.add(paper.getDoi());
                paper.setRankWeight(1.0 - (double) paper.getLife() / 12);
            } // 存活
            else {
                paper.setIsAlive(false);
                dead.add(paper.getDoi());
                paper.setRankWeight(0.0);
            } // 死亡
        }
        Vector<Vector<String>> ans = new Vector<>();
        ans.add(alive);
        ans.add(dead);
        return ans; // 0为仍保护的，1为脱离保护期的
    }

    public static void createTestGraph(){
// 创建测试图并存入数据库
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
