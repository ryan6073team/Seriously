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

import com.github.ryan6073.Seriously.Graph.GraphStore;


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
    public DirectedPseudograph<Author,Edge> getGraphItemFromNeo4j(int year,int month){
        return GraphStore.read(year+"-"+month);
    }

    //返回一个新的图，该图的应该包含所有原图已经存在的作者节点以及已经成熟的引用边
    // 原因在于作者节点可能存在一些论文引用列表为空，此时论文虽然已成熟但是无法在图中得到体现
    public DirectedPseudograph<Author, Edge> getMatureGraph() {
        DirectedPseudograph<Author, Edge> originalGraph = Graph;  // 假设 Graph 是你的原始图
        // 创建一个新的、可修改的图
        DirectedPseudograph<Author, Edge> graph = new DirectedPseudograph<>(Edge.class);
        // 复制边，但不包括不活跃的边
        //这里的成熟指的应该是已经成熟的论文的被引用关系
        for (Edge edge : originalGraph.edgeSet()) {
            if (!DataGatherManager.getInstance().dicDoiPaper.get(edge.getCitingDoi()).getIsAlive()) {
                //将该边的结点添加上去
                graph.addVertex(originalGraph.getEdgeSource(edge));
                graph.addVertex(originalGraph.getEdgeTarget(edge));
                //将该边添加上去
                graph.addEdge(originalGraph.getEdgeSource(edge), originalGraph.getEdgeTarget(edge), edge);
            }
        }
        if(graph.vertexSet().size()==0)
            System.out.println("成熟图为空");

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
        CalImpact.calAuthorsMatureImpact(CalGraph.getGraphImpact(createNewGraph(paper, graph)));//更新删除了结点图的作者影响力
        Map<String, Double> itemsToReturn = new HashMap<>();
        //只统计paper的作者
        for (String orcid : paper.getAuthorIDList()) {
            Double pre = imp.get(orcid);
            Double cur = dataGatherManager.dicOrcidAuthor.get(orcid).getAuthorImpact();
            if(pre>=0&& !Objects.equals(cur, pre))
                itemsToReturn.put(orcid,pre-cur);
        }
        CalImpact.calAuthorsMatureImpact(CalGraph.getGraphImpact(graph));//恢复原作者影响力
        return itemsToReturn;
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
    //将year年month月的图更新到图里，同时更新全部信息
    //关于更新的介绍：
    // 由于现存作者影响力的更新取决于成熟论文引用关系以及未成熟论文的状态向量，
    // 而论文影响力大小取决与作者影响力，这将间接影响到论文和期刊的等级和论文的影响力排名，
    // 也就是说：随着时间的推移，作者影响力不断变化，导致论文和期刊影响力也不断变化，相应的等级和排名自然也不断变动，
    // 从而进一步导致作者的影响力发生变化，作者和论文双方的影响力是相互牵扯、影响的
    // 因此每隔一个月，都要用新的现存作者影响力值重新计算现存的论文和机构影响力，相应的等级排名也应该刷新，
    // 而最难处理的便是随时间不断变化的影响力排名，因为程序必定存在到达成熟期后论文影响力排名仍然不断变化的情况，
    // 不如说这种变化才是正常的，而我们只决定记录论文前一年各阶段的引用量排名并将其用于计算，这本来就是一种取舍，
    // 这代表我们只预测一年时间的论文的状态变化情况，对于一年后的论文的状态如何，状态转移矩阵没有考虑那么长远，
    // 那么这种取舍的依据是什么？我们认为任何论文影响力的增长都类似生物的成长过程，当经历过前期一段时间迅猛的增长后，
    // 论文的“新陈代谢”趋于平缓，影响力波动不再明显，就像一位经历青春期后体型不再增长的成年人，这也是为什么我们
    // 将life=13的论文年龄状态命名为mature。与之对应的，我们认定一篇论文的影响力生长期，也就是它的青春期大致为12，
    // 这是基于统计规律得出的长度--我们认为，论文在到达life>12的成熟期后，其排名和影响力虽然会略微变动但是已经不再剧烈，
    // 其对于学术圈的大致贡献已经定型。当然不否定类似于孟德尔一类的特殊情况，但这毕竟是少数
    // 将预测能力运用到作者影响力的计算上面后，这代表着我们实际上是在预测某位作者在当前状态下，他对于学术圈的贡献，
    // 也就是影响力会是多少：对于那些已经处于成熟期的论文，我们的pageRank网络已经能够处理，但对于处于青春期的论文们，
    // 我们的预测模型将更精准的判断它未来对于学术圈的贡献，而不是将眼光局限于论文现在的成绩。
    public Vector<Vector<String>> updateGraph(int year, int month){


        ////////////////////////////开关/////////////////////////////////////////////
        //DirectedPseudograph<Author,Edge> graphItem = getGraphItem(year, month);
        DirectedPseudograph<Author,Edge> graphItem = getGraphItemFromNeo4j(year,month);
        System.out.println(year+"-"+month);
        ///////////////////////////////////////////////////////////////////////////////



        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        Vector<Vector<String>> ans;

        //若当前时间不存在新的引用关系，则不需要更新图只需要更新现有图论文的信息即可
            //若存在新的引用关系或者新的作者节点
        if(graphItem != null&&graphItem.vertexSet().size()!=0) {
            //合并点+作者激活
            //子图的作者点包含了全部在目标时间段发表的论文的作者，可能由于暂时没有引用关系为孤立点
            for (Author author : graphItem.vertexSet()) {
                if (!Graph.containsVertex(author)) {
                    Graph.addVertex(author);
                    author.setIfExist(1);
                }
            }
            //合并边,边并不是论文的全部，存在论文暂时没有引用关系
            for (Edge edge : graphItem.edgeSet()) {
                Graph.addEdge(graphItem.getEdgeSource(edge), graphItem.getEdgeTarget(edge), edge);
            }
            //激活论文
            Vector<String> dois = dataGatherManager.dicTimeInfoDoi.get(new TimeInfo(year,month));
            //存在目标时间段没有论文的可能
            if(dois!=null)
                for(String doi:dois){
                    dataGatherManager.dicDoiPaper.get(doi).setIsRead(1);
                    dataGatherManager.dicNameJournal.get(dataGatherManager.dicDoiPaper.get(doi).getJournal()).setIfExist(1);
                }
        }


        //更新现存作者影响力
        ans = updatePaperLifeInfo(year,month, dataGatherManager);
        CalImpact.initorUpdateAuthorImpact(ans);
        System.out.println("完成作者等级和影响力更新");

        //更新全部现存论文影响力
        CalImpact.updatePaperImpact();
        System.out.println("完成论文影响力更新");

        //更新期刊影响力
        CalImpact.updateJournalImpact();
        //初始化原始总图的期刊和相应的论文等级，不论论文是否出现或成熟，其等级已经确定和期刊等级一致，因此直接更新即可
        JournalKMeans.JournalkMeans(dataGatherManager);
        System.out.println("完成期刊和论文等级初始化");
        //利用现存作者更新机构影响力
        CalImpact.updateInstitutionImpact();
        //更新现存的论文引用等级
        DataGatherManager.updateCitationLevel();
        System.out.println("完成论文引用等级更新");


        //更新Strategy的状态转移矩阵
            //更新该年论文集
        TimeInfo timeInfo = new TimeInfo(year,month);
        for(TimeInfo item:dataGatherManager.dicTimeInfoDoi.keySet()){
            if(timeInfo.equals(item)){
                dataGatherManager.currentCoefficientStrategy.currentYearPapers.addAll(dataGatherManager.dicTimeInfoDoi.get(item));
                break;
            }
        }
            //如果时间已经到了最后一个月则通过今年与去年论文集的数据更新状态转移矩阵
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
//        ImpactForm.getInstance().cal_impact();
        return ans;
    }
    //更新论文life，定期更新论文CitationLevel，当月论文life=0
    private Vector<Vector<String>> updatePaperLifeInfo(int year, int month, DataGatherManager dataGatherManager){
        //导入已经读过了的论文
        Vector<Paper> papers = new Vector<>();
        int i=year*12+month-1;
        for(int j=0;j<13;j++){
            int tempyear,tempmonth;
            if(i%12==0){
                tempyear = i/12-1;
                tempmonth = 12;
            }else{
                tempyear = i/12;
                tempmonth = i%12;
            }
            Vector<String> somePapers = dataGatherManager.dicTimeInfoDoi.get(new TimeInfo(tempyear,tempmonth));
            if(somePapers!=null)
                for(String doi:somePapers) {
                    papers.add(dataGatherManager.dicDoiPaper.get(doi));
                }
            i--;
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
}
