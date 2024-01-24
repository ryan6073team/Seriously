package com.github.ryan6073.Seriously.Graph;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Coefficient.CoefficientStrategy;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import com.github.ryan6073.Seriously.TimeInfo;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.DefaultEdge;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GraphInit {
    private static DirectedPseudograph<Paper, DefaultEdge> paperGraph = new DirectedPseudograph<>(DefaultEdge.class);  //创建一个论文的图用以检验是否存在环
    public static void deleteSinglePoint(DirectedPseudograph<Author,Edge> graph){
        if(graph.edgeSet().isEmpty()) return;
        for (Author vertex : graph.vertexSet()) {
            int inDegree = graph.inDegreeOf(vertex);
            int outDegree = graph.outDegreeOf(vertex);
            if(inDegree+outDegree==0) graph.removeVertex(vertex);
        }
    }// 删除图中孤立点

    //检查是否存在环
    public static void DetectCycles(DirectedPseudograph<Paper,DefaultEdge> detectGraph) {
        CycleDetector<Paper, DefaultEdge> cycleDetector
                = new CycleDetector<Paper, DefaultEdge>(detectGraph);

        assert(!cycleDetector.detectCycles());
        Set<Paper> cycleVertices = cycleDetector.findCycles();

        assert(!(cycleVertices.size() > 0));
    }

    //获取论文作者中存在于数据源中的数量
    public static int getAuthorNumber(Paper paper, DataGatherManager dataGatherManager){
        int num = 0;
        for(String str:paper.getAuthorIDList()){
            Author start_author = dataGatherManager.dicOrcidAuthor.get(str);
            if(start_author.getFlag()) num++;
        }
        return num;
    }

    public static Vector<Paper> getAllPapers(int year, int month){
        Vector<Paper> papers = new Vector<>();
        //根据时间获取之前的论文
        for(Map.Entry<TimeInfo,Vector<String>> entry: DataGatherManager.getInstance().dicTimeInfoDoi.entrySet()){
            if(entry.getKey().year<year) {
                for (String doi : entry.getValue()) {
                    papers.add(DataGatherManager.getInstance().dicDoiPaper.get(doi));
                }
            }
            else if(entry.getKey().year==year && entry.getKey().month<=month){
                for (String doi : entry.getValue()) {
                    papers.add(DataGatherManager.getInstance().dicDoiPaper.get(doi));
                }
            }
        }
        return papers;
    }
    public static Vector<Paper> getPapers(int year, int month){
        Vector<Paper> papers = new Vector<>();
        //根据时间获取论文
        for(Map.Entry<TimeInfo,Vector<String>> entry: DataGatherManager.getInstance().dicTimeInfoDoi.entrySet()){
            if(entry.getKey().year==year && entry.getKey().month==month){
                for(String doi:entry.getValue()){
                    papers.add(DataGatherManager.getInstance().dicDoiPaper.get(doi));
                }
            }
        }
        return papers;
    }

    //新增函数，意在替换原函数，仅将x年y月之前的作者引用关系而不是所有作者引用关系构造为一张图
    public static void initGraph(GraphManager graphManager,DataGatherManager dataGatherManager,int year,int month){

        //更新相应时间的论文，并利用该论文集形成原初图并更新处理相应的论文的life信息和isAlive.isRead信息，以及作者的ifExist信息
        Vector<Paper> papers = getAllPapers(year,month);
        for(Paper paper: papers){
            if(paper.getPublishedYear()>year) continue;
            else if(paper.getPublishedYear()==year && paper.getPublishedMonth()>month) continue;
            paper.setIsRead(1);

            int lifeSpan = (year - paper.getPublishedYear()) * 12 + month - paper.getPublishedMonth();
            if(lifeSpan > 12){
                paper.setLife(13);
                paper.setIsAlive(false);
            }
            else paper.setLife(lifeSpan);
            System.out.println(paper.getDoi() + "的存活时间为" + paper.getLife());

            //在论文图中添加论文结点
            if(!paperGraph.containsVertex(paper)){
                paperGraph.addVertex(paper);
            }
            //获取存在于数据源中的作者数量
            int startNum = getAuthorNumber(paper,dataGatherManager);  //引用作者数量，即与边起点有关的作者数

            //获取引用论文
            for(String doi: paper.getCitingList()){

                Paper citingPaper = dataGatherManager.dicDoiPaper.get(doi);

                //在论文图中添加论文结点
                if(!paperGraph.containsVertex(citingPaper)){
                    //System.out.println();
                    paperGraph.addVertex(citingPaper);
                }
                //论文图中添加引用边
                if(!paperGraph.containsEdge(paper,citingPaper)){
                    paperGraph.addEdge(paper, citingPaper);
                }
                //检测是否存在环
                DetectCycles(paperGraph);

                //获取作者数量
                int endNum = getAuthorNumber(citingPaper,dataGatherManager);

                for(String auOrcid: citingPaper.getAuthorIDList()){
                    Author endAuthor = dataGatherManager.dicOrcidAuthor.get(auOrcid);
                    //判断数据源中是否存在该作者
                    if(endAuthor.getFlag()){
                        //判断是否需要创建结点
                        if(!graphManager.Graph.containsVertex(endAuthor)){
                            graphManager.Graph.addVertex(endAuthor);
                            endAuthor.setIfExist(1);
                        }
                        //创建边
                        Vector<String> authorIDList = paper.getAuthorIDList();
                        for(String authorID: authorIDList){
                            Author author = dataGatherManager.dicOrcidAuthor.get(authorID);
                            if(!graphManager.Graph.containsVertex(author)){
                                graphManager.Graph.addVertex(author);
                                author.setIfExist(1);
                            }
                            double citingKey = (double) 1 /(startNum * endNum * paper.getCitingList().size());
                            Edge edge = new Edge(citingKey, paper.getPublishedYear(),paper.getDoi(),doi);  //论文状态为此篇论文状态
                            graphManager.Graph.addEdge(author,endAuthor,edge);
                            dataGatherManager.dicDoiPaper.get(paper.getDoi()).getEdgeList().add(edge);
                            dataGatherManager.dicDoiPaper.get(doi).getEdgeList().add(edge);
                        }
                    }
                }
            }
        }



        //初始化完图之后对于其它参数进行初始化为程序的后续运行提供必要的数据支持
            //更新现存论文的被引用信息，包含被引文章列表以及被引次数，两项数据全部基于当下而不考虑未来
        GraphInit.initCitedInfo();

            //初始化原图的作者影响力和等级
        Vector<Vector<String>> ans = new Vector<>();
        Vector<String> alive = new Vector<>();
        Vector<String> dead = new Vector<>();
        for(Edge edge:graphManager.Graph.edgeSet()){
            if(dataGatherManager.dicDoiPaper.get(edge.getDoi()).getIsAlive()==false)
                dead.add(edge.getDoi());
            else alive.add(edge.getDoi());
        }
        ans.add(alive);
        ans.add(dead);
        CalImpact.initorUpdateAuthorImpact(ans);
        System.out.println("完成作者等级和影响力初始化");


        //初始化原始总图的论文的影响力
        CalImpact.initPapersImpact();
        System.out.println("完成论文影响力初始化");
        //初始化原始总图的期刊影响力
        CalImpact.initJournalImpact();
        //初始化原始总图的期刊和相应的论文等级，不论论文是否出现或成熟，其等级已经确定和期刊等级一致，因此直接更新即可
        JournalKMeans.JournalkMeans(dataGatherManager);
        System.out.println("完成期刊和论文等级初始化");
        //初始化机构影响力
        CalImpact.initInstitutionImpact();
        //初始化原始总图的论文引用等级
        DataGatherManager.updateCitationLevel();
        System.out.println("完成论文引用等级初始化");
        GraphInit.initGraphItems(graphManager,dataGatherManager,dataGatherManager.firstYear,dataGatherManager.firstMonth+1,dataGatherManager.finalYear,dataGatherManager.finalMonth);
        System.out.println("完成初始图集的初始化");

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
        ImpactForm.getInstance().cal_impact();

        for(int i=dataGatherManager.firstYear*12+dataGatherManager.firstMonth;i<= dataGatherManager.startYear*12+ dataGatherManager.startMonth;i++){
            //注意在startyear startmonth的时候就开始更新了
            int tempyear,tempmonth;
            if(i%12==0){
                tempmonth = 12;
                tempyear = i/12-1;
            }
            else{
                tempmonth = i%12;
                tempyear = i/12;
            }
            graphManager.updateGraph(tempyear,tempmonth);
        }
        //deleteSinglePoint(graphManager.Graph);
    }
    //新增函数，即将x年y月的作者引用关系构成一张图并将其存储在GraphItems中
    //GraphItems中包含了从start到final的所有时间段，部分没有更新信息的item存在但size为0，但是并不代表相应的item=null
    public static void initGraphItem(GraphManager graphManager,DataGatherManager dataGatherManager,int year,int month){
        DirectedPseudograph<Author,Edge> GraphTemp = new DirectedPseudograph<>(Edge.class);
        Vector<Paper> papers = getPapers(year,month);
        for(Paper paper: papers){
            //获取存在于数据源中的作者数量
            int startNum = getAuthorNumber(paper,dataGatherManager);  //引用作者数量，即与边起点有关的作者数

            //获取引用论文
            for(String doi: paper.getCitingList()){

                Paper citingPaper = dataGatherManager.dicDoiPaper.get(doi);
                //获取作者数量
                int endNum = getAuthorNumber(citingPaper,dataGatherManager);

                for(String auOrcid: citingPaper.getAuthorIDList()){
                    Author endAuthor = dataGatherManager.dicOrcidAuthor.get(auOrcid);
                    //判断数据源中是否存在该作者
                    if(endAuthor.getFlag()){
                        //判断是否需要创建结点
                        if(!GraphTemp.containsVertex(endAuthor)){
                            GraphTemp.addVertex(endAuthor);
                        }
                        //创建边
                        Vector<String> authorIDList = paper.getAuthorIDList();
                        for(String authorID: authorIDList){
                            Author author = dataGatherManager.dicOrcidAuthor.get(authorID);
                            if(!GraphTemp.containsVertex(author)){
                                GraphTemp.addVertex(author);
                            }
                            double citingKey = (double) 1 /(startNum * endNum * paper.getCitingList().size());
                            Edge edge = new Edge(citingKey, paper.getPublishedYear(), paper.getDoi(),doi);  //论文状态为此篇论文状态
                            GraphTemp.addEdge(author,endAuthor,edge);
                            dataGatherManager.dicDoiPaper.get(paper.getDoi()).getEdgeList().add(edge);
                            dataGatherManager.dicDoiPaper.get(doi).getEdgeList().add(edge);
                        }
                    }
                }
            }
        }
        //deleteSinglePoint(GraphTemp);

        graphManager.addGraphItem(year,month,GraphTemp);
        System.out.println("完成"+year+"年"+month+"月的图初始化");

        String name = year+"-"+month;
//        GraphStore.store(name, GraphTemp);
//        System.out.println("完成"+year+"年"+month+"月的图存储");
    }
    public static void initGraphItems(GraphManager graphManager,DataGatherManager dataGatherManager,int startYear,int startMonth, int endYear, int endMonth){
        if(startYear==endYear)
            for(int i=startMonth;i<=endMonth;++i){
                initGraphItem(graphManager,dataGatherManager,startYear,i);
            }
        else {
            for(int i=startMonth;i<=12;++i){
                initGraphItem(graphManager,dataGatherManager,startYear,i);
            }
            for (int i = startYear + 1; i < endYear; ++i) {
                for (int j =1;j<=12;++j){
                    initGraphItem(graphManager,dataGatherManager,i,j);
                }
            }
            for(int i=1;i<=endMonth;++i){
                initGraphItem(graphManager,dataGatherManager,endYear,i);
            }
        }
    }
    //新增函数，根据paperGraph将dataGatherManager中所有paper的citedList初始化，citedList是Paper新增成员变量
    //指的是引用该论文的论文集合，所以对于该论文来说是被动语态cited
    //可以将culCitedTimes和该函数合并成initCitedInfo

    public static void initCitedInfo(){
        for (Paper vertex : paperGraph.vertexSet()) {
            int inDegree = paperGraph.inDegreeOf(vertex);
            vertex.setCitedTimes(inDegree);
        }
        for (DefaultEdge edge : paperGraph.edgeSet()) {
            Paper startPoint = paperGraph.getEdgeSource(edge);
            Paper endPoint = paperGraph.getEdgeTarget(edge);
            endPoint.getCitedList().add(startPoint.getDoi());
        }
    }

    public static void givenAdaptedGraph_whenWriteBufferedImage_thenFileShouldExist() throws IOException {

        JGraphXAdapter<Author,Edge> graphAdapter =
                new JGraphXAdapter<>(GraphManager.getInstance().Graph);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        File imgFile = new File("graph.png");
        ImageIO.write(image, "PNG", imgFile);

        assert(imgFile.exists());
    }

}