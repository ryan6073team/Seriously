package com.github.ryan6073.Seriously.Graph;
import Jama.Matrix;
import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Impact.CalGraph;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;


public class GraphManager { //单例
    private static GraphManager mGraphManager = new GraphManager();
    public static GraphManager getInstance(){return mGraphManager;}
    private Map<TimeInfo,DirectedGraph<Author,Edge>> GraphItems = new HashMap<>();
    //获取目标子图
    public DirectedGraph<Author,Edge> getGraphItem(int year,int month){
        for(Map.Entry<TimeInfo,DirectedGraph<Author,Edge>> entry : GraphItems.entrySet()){
            if(entry.getKey().year==year&&entry.getKey().month==month)
                return GraphItems.get(entry);
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
    //将year年month月的图更新到图里
    public Vector<Vector<String>> updateGraph(int year, int month){
        DirectedGraph<Author,Edge> graphItem = getGraphItem(year, month);
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
        return updatePaperLifeInfo(year,month,Graph,DataGatherManager.getInstance());
    }
    private Vector<Vector<String>> updatePaperLifeInfo(int year, int month, DirectedGraph<Author,Edge> graph, DataGatherManager dataGatherManager){
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
            int finalMonth = paper.getPublishedMonth() + paper.getLifeSpan() - 1;
            int finalYear = paper.getPublishedYear() + finalMonth / 12;
            finalMonth = finalMonth % 12;
            if(finalMonth == 0) {
                finalMonth = 12;
                finalYear--;
            }
            if(year<finalYear){
                alive.add(paper.getDoi());
                int leftTime = (finalYear - year) * 12 + finalMonth - month + 1;
                paper.setRankWeight(1.0 - (double) leftTime / paper.getLifeSpan());
            }
            else if(year>finalYear){
                paper.setIsAlive(false);
                dead.add(paper.getDoi());
            }
            else{
                if(month>finalMonth){
                    paper.setIsAlive(false);
                    dead.add(paper.getDoi());
                    paper.setRankWeight(0.0);
                }
                else{
                    alive.add(paper.getDoi());
                    int leftTime = finalMonth - month + 1;
                    paper.setRankWeight(1.0 - (double) leftTime / paper.getLifeSpan());
                }
            }
        }
        Vector<Vector<String>> ans = new Vector<>();
        ans.add(alive);
        ans.add(dead);
        return ans; // 0为仍保护的，1为脱离保护期的
    }
}
