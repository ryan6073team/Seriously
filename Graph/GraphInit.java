package com.github.ryan6073.Seriously.Graph;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Coefficient.CoefficientStrategy;
import com.github.ryan6073.Seriously.TimeInfo;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class GraphInit {
    private static DirectedGraph<Paper, DefaultEdge> paperGraph = new DefaultDirectedGraph<>(DefaultEdge.class);  //创建一个论文的图用以检验是否存在环
//    public static void deleteSinglePoint(DirectedGraph<Author,Edge> graph){
//        if(graph.edgeSet().isEmpty()) return;
//        for (Author vertex : graph.vertexSet()) {
//            int inDegree = graph.inDegreeOf(vertex);
//            int outDegree = graph.outDegreeOf(vertex);
//            if(inDegree+outDegree==0) graph.removeVertex(vertex);
//        }
//    }// 删除图中孤立点

    //检查是否存在环
    public static void DetectCycles(DirectedGraph<Paper,DefaultEdge> detectGraph) {
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

    //利用初始化后的dataGatherManager对graphManager进行初始化
//    public static void initGraph(GraphManager graphManager,DataGatherManager dataGatherManager){
//        for(Map.Entry<Author, Vector<Paper>> entry : dataGatherManager.dicAuthorPaper.entrySet()){
//            if(!entry.getKey().getFlag()) continue;  //不存在该作者则进行下一个循环
//            //如果不存在作者结点则创建
//            if(!graphManager.Graph.containsVertex(entry.getKey())){
//                graphManager.Graph.addVertex(entry.getKey());
//            }
//            //遍历该作者的论文
//            for(Paper paper: entry.getValue()){
//                //在论文图中添加论文结点
//                if(!paperGraph.containsVertex(paper)){
//                    paperGraph.addVertex(paper);
//                }
//                //获取存在于数据源中的作者数量
//                int startNum = getAuthorNumber(paper,dataGatherManager);  //引用作者数量，即与边起点有关的作者数
//
//                //获取引用论文
//                for(String doi: paper.getCitingList()){
//
//                    Paper citingPaper = dataGatherManager.dicDoiPaper.get(doi);
//
//                    //在论文图中添加论文结点
//                    if(!paperGraph.containsVertex(citingPaper)){
//                        paperGraph.addVertex(citingPaper);
//                    }
//                    //论文图中添加引用边
//                    if(!paperGraph.containsEdge(paper,citingPaper)){
//                        paperGraph.addEdge(paper, citingPaper);
//                    }
//                    //检测是否存在环
//                    DetectCycles(paperGraph);
//
//                    //获取作者数量
//                    int endNum = getAuthorNumber(citingPaper,dataGatherManager);
//
//                    for(String auOrcid: citingPaper.getAuthorIDList()){
//                        Author endAuthor = dataGatherManager.dicOrcidAuthor.get(auOrcid);
//                        //判断数据源中是否存在该作者
//                        if(endAuthor.getFlag()){
//                            //判断是否需要创建结点
//                            if(!graphManager.Graph.containsVertex(endAuthor)){
//                                graphManager.Graph.addVertex(endAuthor);
//                            }
//                            //创建边
//                            double citingKey = (double) 1 /(startNum * endNum);
//                            Edge edge = new Edge(citingKey, paper.getPaperStatus().ordinal()+1, paper.getPublishedYear());  //论文状态为此篇论文状态
//                            graphManager.Graph.addEdge(entry.getKey(),endAuthor,edge);
//                        }
//                    }
//                }
//            }
//        }
//        culCitedTimes();
//    }
    //新增函数，意在替换原函数，仅将x年y月之前的作者引用关系而不是所有作者引用关系构造为一张图，同时初始化Strategy的矩阵
    public static void initGraph(GraphManager graphManager,DataGatherManager dataGatherManager,int year,int month){
        for(Map.Entry<Author, Vector<Paper>> entry : dataGatherManager.dicAuthorPaper.entrySet()){
            if(!entry.getKey().getFlag()) continue;  //不存在该作者则进行下一个循环
            //遍历该作者的论文
            for(Paper paper: entry.getValue()){
                if(paper==null) continue;
                if(paper.getPublishedYear()>year) continue;
                else if(paper.getPublishedMonth()>month) continue;
                paper.setIsRead(1);

                //如果不存在作者结点则创建
                if(!graphManager.Graph.containsVertex(entry.getKey())){
                    graphManager.Graph.addVertex(entry.getKey());
                    entry.getKey().setIfExist(1);
                }

                int lifeSpan = (year - paper.getPublishedYear()) * 12 + month - paper.getPublishedMonth();
                if(lifeSpan > 12) paper.setLife(13);
                else paper.setLife(lifeSpan);

                //更新论文的citationlevel
                if(lifeSpan==4)
                    paper.youthCitationLevel = CoefficientStrategy.getCitationLevel(paper);
                else if(lifeSpan==8)
                    paper.strongCitationLevel = CoefficientStrategy.getCitationLevel(paper);
                else if(lifeSpan==12)
                    paper.matureCitationLevel = CoefficientStrategy.getCitationLevel(paper);

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
                        System.out.println();
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
                            double citingKey = (double) 1 /(startNum * endNum);
                            Edge edge = new Edge(citingKey, paper.getPublishedYear(), paper.getDoi());  //论文状态为此篇论文状态
                            graphManager.Graph.addEdge(entry.getKey(),endAuthor,edge);
                            dataGatherManager.dicDoiPaper.get(paper.getDoi()).getEdgeList().add(edge);
                        }
                    }
                }
            }
        }
        initCitedInfo();//更新被引信息
        //初始化论文的引用等级
        initPapersCitationLevel();
        //将去年和今年发表的论文分别存储
        for(TimeInfo timeInfo:dataGatherManager.dicTimeInfoDoi.keySet()){
            if(timeInfo.year==year-1)
                dataGatherManager.currentCoefficientStrategy.lastYearPapers.addAll(dataGatherManager.dicTimeInfoDoi.get(timeInfo));
            else if(timeInfo.year==year)
                dataGatherManager.currentCoefficientStrategy.currentYearPapers.addAll(dataGatherManager.dicTimeInfoDoi.get(timeInfo));
        }
        //初始化strategy的矩阵
        if(month==12){
            dataGatherManager.currentCoefficientStrategy.initorUpdateTransitionMatrixItems();
            dataGatherManager.currentCoefficientStrategy.initOtherMatrixs();
        }
        //deleteSinglePoint(graphManager.Graph);
    }
    //新增函数，即将x年y月的作者引用关系构成一张图并将其存储在GraphItems中
    public static void initGraphItem(GraphManager graphManager,DataGatherManager dataGatherManager,int year,int month){
        DirectedGraph<Author,Edge> GraphTemp = new DefaultDirectedGraph<>(Edge.class);
        for(Map.Entry<Author, Vector<Paper>> entry : dataGatherManager.dicAuthorPaper.entrySet()){
            if(!entry.getKey().getFlag()) continue;  //不存在该作者则进行下一个循环
            //遍历该作者的论文
            for(Paper paper: entry.getValue()){
                if(paper==null) continue;
                if(paper.getPublishedYear()!=year||paper.getPublishedMonth()!=month) continue;

                //如果不存在作者结点则创建
                if(!GraphTemp.containsVertex(entry.getKey())){
                    GraphTemp.addVertex(entry.getKey());
                }

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
                            double citingKey = (double) 1 /(startNum * endNum);
                            Edge edge = new Edge(citingKey, paper.getPublishedYear(), paper.getDoi());  //论文状态为此篇论文状态
                            GraphTemp.addEdge(entry.getKey(),endAuthor,edge);
                            dataGatherManager.dicDoiPaper.get(paper.getDoi()).getEdgeList().add(edge);
                        }
                    }
                }
            }
        }
        //deleteSinglePoint(GraphTemp);
        graphManager.addGraphItem(year,month,GraphTemp);
        System.out.println(year + "年" + month + "月已更新");
//        GraphStore.store(year+"-"+month,GraphTemp);
//        System.out.println("完成"+year + "年" + month + "月图的存储");
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

    public static void initPapersCitationLevel(){
        for (Paper vertex : paperGraph.vertexSet()) {
            vertex.setCitationLevel(CoefficientStrategy.getCitationLevel(vertex));
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
