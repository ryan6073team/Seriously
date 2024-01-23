package com.github.ryan6073.Seriously.Graph;
import Jama.Matrix;
import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Coefficient.CoefficientStrategy;
import com.github.ryan6073.Seriously.Impact.CalGraph;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.*;


public class GraphManager { //单例
    private static GraphManager mGraphManager = new GraphManager();
    public static GraphManager getInstance(){
        return mGraphManager;
    }
    private Map<TimeInfo,DirectedPseudograph<Author,Edge>> GraphItems = new HashMap<>();
    //获取目标子图
    public DirectedPseudograph<Author,Edge> getGraphItem(int year,int month){
        for(Map.Entry<TimeInfo,DirectedPseudograph<Author,Edge>> entry : GraphItems.entrySet()){
            if(entry.getKey().year==year&&entry.getKey().month==month)
                return entry.getValue();
        }
        return null;
    }
    //    public DirectedPseudograph<Author ,Edge> getMatureGraph(){
//        DirectedPseudograph<Author ,Edge> graph = Graph;
//        for(Edge edge:graph.edgeSet()){
//            if(DataGatherManager.getInstance().dicDoiPaper.get(edge.getDoi()).getIsAlive()){
//                graph.removeEdge(edge);
//            }
//        }
//        return graph;
//    }
    public DirectedPseudograph<Author, Edge> getMatureGraph() {
        DirectedPseudograph<Author, Edge> originalGraph = Graph;  // 假设 Graph 是你的原始图
        // 创建一个新的、可修改的图
        DirectedPseudograph<Author, Edge> graph = new DirectedPseudograph<>(Edge.class);
        // 复制边，但不包括不活跃的边
        for (Edge edge : originalGraph.edgeSet()) {
            if (!DataGatherManager.getInstance().dicDoiPaper.get(edge.getDoi()).getIsAlive()) {
                //将该边的结点添加上去
                graph.addVertex(originalGraph.getEdgeSource(edge));
                graph.addVertex(originalGraph.getEdgeTarget(edge));
                //将该边添加上去
                graph.addEdge(originalGraph.getEdgeSource(edge), originalGraph.getEdgeTarget(edge), edge);
            }
        }
        if(graph.vertexSet().size()==0)
            System.out.println("成熟矩阵为空");

//        //打印图的信息
//        System.out.println("图的信息如下：");
//        System.out.println("图的顶点数目为："+graph.vertexSet().size());
//        System.out.println("图的边数目为："+graph.edgeSet().size());
//        System.out.println("图的顶点集合为：");
//        for(Author author:graph.vertexSet()){
//            System.out.println(author.getOrcid());
//        }
//        System.out.println("图的边集合为：");
//        for(Edge edge:graph.edgeSet()){
//            System.out.println(edge.getDoi());
//            System.out.println(graph.getEdgeSource(edge).getOrcid()+"->"+graph.getEdgeTarget(edge).getOrcid());
//            System.out.println("引用次数为："+edge.getCitingKey());
//        }

        return graph;
    }

    //加入目标子图
    public void addGraphItem(int year, int month, DirectedPseudograph<Author,Edge> Item){GraphItems.put(new TimeInfo(year,month),Item);}
    //创建初始图，一切故事从这里开始
    public DirectedPseudograph<Author,Edge>  Graph = new DirectedPseudograph<>(Edge.class);
    //关于初始图和子图的创建时间参数请查看DataGatherManager新增的startYear/Month和finalYear/Month
    //注意这些图都是作者引用图，而GraphInit中的paperGraph是论文引用图
    private GraphManager(){}
    public DirectedPseudograph<Author,Edge> createNewGraph(Paper paper, DirectedPseudograph<Author,Edge> graph){
        DirectedPseudograph<Author,Edge> newGraph = new DirectedPseudograph<>(Edge.class);
        for(Author author:graph.vertexSet()){
            newGraph.addVertex(author);
        }
        for(Edge edge:graph.edgeSet()){
            newGraph.addEdge(graph.getEdgeSource(edge),graph.getEdgeTarget(edge),edge);
        }
        for(Edge edge: paper.getEdgeList()){
            newGraph.removeEdge(edge);
        }
        boolean ifExistsOutDegreeZero = true;
        while(ifExistsOutDegreeZero){
            ifExistsOutDegreeZero = false;
            for(Author author:newGraph.vertexSet()){
                if(newGraph.outDegreeOf(author)==0&&newGraph.inDegreeOf(author)!=0){
                    newGraph.removeVertex(author);
                    newGraph.addVertex(author);
                    ifExistsOutDegreeZero = true;
                    break;
                }
            }
        }

//        //打印图的信息
//        System.out.println("图的信息如下：");
//        System.out.println("图的顶点数目为："+newGraph.vertexSet().size());
//        System.out.println("图的边数目为："+newGraph.edgeSet().size());
//        System.out.println("图的顶点集合为：");
//        for(Author author:newGraph.vertexSet()){
//            System.out.println(author.getOrcid());
//        }
//        System.out.println("图的边集合为：");
//        for(Edge edge:newGraph.edgeSet()){
//            System.out.println(edge.getDoi());
//            System.out.println(newGraph.getEdgeSource(edge).getOrcid()+"->"+newGraph.getEdgeTarget(edge).getOrcid());
//            System.out.println("引用次数为："+edge.getCitingKey());
//        }

        return newGraph;
    }//生成删除了要研究论文的图
    public Map<String,Double> calNewPaperImp(DataGatherManager dataGatherManager,Paper paper, DirectedPseudograph<Author,Edge> graph){
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
    public double[][][] calAllPaperImp(DataGatherManager dataGatherManager,DirectedPseudograph<Author,Edge> graph){
        double [][][] Matrix = new double[5][5][3];
        int [][][] number = new int[5][5][3];
        Set<Paper> papers = new HashSet<>();
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                for(int k=0;k<3;k++){
                    Matrix[i][j][k]=0.0;
                    number[i][j][k]=0;
                }
            }
        }
        for(Edge edge:graph.edgeSet()) {
            Paper paper = dataGatherManager.dicDoiPaper.get(edge.getDoi());
            papers.add(paper);
        }
        for(Paper paper:papers){
            if(paper.getIsAlive()) continue;
            int paperRank = paper.getLevel().getIndex();
            int citationRank = CoefficientStrategy.getCitationLevel(paper).getIndex();
            Map<String,Double> imp = calNewPaperImp(dataGatherManager,paper,graph);

            for(String orcid: imp.keySet()){
                Author author = dataGatherManager.dicOrcidAuthor.get(orcid);
                int authorRank = author.getLevel().getIndex();
                Matrix[authorRank][paperRank][citationRank] += imp.get(orcid);
                number[authorRank][paperRank][citationRank]++;
            }
        }
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                for(int k=0;k<3;k++){
                    if(number[i][j][k]!=0){
                        Matrix[i][j][k]/=number[i][j][k];
                    }
                }
            }
        }
        return Matrix;
    }//计算全部不在保护期的论文对作者的平均影响力
    //将year年month月的图更新到图里，同时更新Strategy的矩阵
    public Vector<Vector<String>> updateGraph(int year, int month){
        DirectedPseudograph<Author,Edge> graphItem = getGraphItem(year, month);
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        Vector<Vector<String>> ans = new Vector<>();

        //如果更新时间是start，那么不用更新图和论文信息直接返回现有图的成熟论文和不成熟论文即可
        if(year==DataGatherManager.getInstance().startYear&&month==DataGatherManager.getInstance().startMonth){
            Vector<String> alive = new Vector<>();
            Vector<String> dead = new Vector<>();
            for(Edge edge:Graph.edgeSet()){
                if(dataGatherManager.dicDoiPaper.get(edge.getDoi()).getIsAlive()==false)
                    dead.add(edge.getDoi());
                else alive.add(edge.getDoi());
            }
            ans.add(alive);
            ans.add(dead);
            //但是impactForm需要初始化
            ImpactForm.getInstance().cal_impact();
        }
        //若当前时间不存在新的引用关系，则不需要更新图只需要更新现有图论文的信息即可
        else if(graphItem == null||graphItem.vertexSet().size()==0) {
            ans = updatePaperLifeInfo(Graph,dataGatherManager);
            //论文状态被更新之后需要重新运行cal_impact更新form
            ImpactForm.getInstance().cal_impact();
        }
        else {
            for (Author author : graphItem.vertexSet()) {
                if (!Graph.containsVertex(author)) {
                    Graph.addVertex(author);
                    author.setIfExist(1);
                }
            }
            for (Edge edge : graphItem.edgeSet()) {
                Graph.addEdge(graphItem.getEdgeSource(edge), graphItem.getEdgeTarget(edge), edge);
                dataGatherManager.dicDoiPaper.get(edge.getDoi()).setIsRead(1);
            }
            ans = updatePaperLifeInfo(Graph, dataGatherManager);
        }

        GraphInit.initorUpdatePapersCitationLevel(GraphManager.getInstance().Graph);
        //更新Strategy的矩阵
        //更新该年论文集
        TimeInfo timeInfo = new TimeInfo(year,month);
        for(TimeInfo item:dataGatherManager.dicTimeInfoDoi.keySet()){
            if(timeInfo.equals(item)){
                dataGatherManager.currentCoefficientStrategy.currentYearPapers.addAll(dataGatherManager.dicTimeInfoDoi.get(item));
                break;
            }
        }
        //dataGatherManager.currentCoefficientStrategy.currentYearPapers.addAll(dataGatherManager.dicTimeInfoDoi.get(new TimeInfo(year,month)));
        if(month==12) {
            if(year== dataGatherManager.startYear){
                dataGatherManager.currentCoefficientStrategy.initorUpdateTransitionMatrixItems(year);
                dataGatherManager.currentCoefficientStrategy.initOtherMatrixs();
            }else {
                dataGatherManager.currentCoefficientStrategy.initorUpdateTransitionMatrixItems(year);
                dataGatherManager.currentCoefficientStrategy.updateOtherTransitionMatrixs();
            }
        }

        //更新impactForm
        ImpactForm.getInstance().cal_impact();
        return ans;
    }
    //更新论文life，定期更新论文CitationLevel
    private Vector<Vector<String>> updatePaperLifeInfo(DirectedPseudograph<Author,Edge> graph, DataGatherManager dataGatherManager){
        Set<Paper> papers = new HashSet<>();
        for(Edge edge:graph.edgeSet()) {
            Paper sourcePaper = dataGatherManager.dicDoiPaper.get(edge.getDoi());
            Paper targetPaper = dataGatherManager.dicDoiPaper.get(edge.getCitingDoi());
            papers.add(sourcePaper);
            papers.add(targetPaper);
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
        DirectedPseudograph<Author, Edge> testGraph1 = new DirectedPseudograph<>(Edge.class);
        DirectedPseudograph<Author, Edge> testGraph2 = new DirectedPseudograph<>(Edge.class);
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
