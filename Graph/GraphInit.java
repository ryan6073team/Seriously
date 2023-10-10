package com.github.ryan6073.Seriously.Graph;

import com.github.ryan6073.Seriously.BasicInfo.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class GraphInit {
    private static DirectedGraph<Paper, DefaultEdge> paperGraph = new DefaultDirectedGraph<>(DefaultEdge.class);  //创建一个论文的图用以检验是否存在环

    public static void culCitedTimes(){
        for (Paper vertex : paperGraph.vertexSet()) {
            int inDegree = paperGraph.inDegreeOf(vertex);
        }
    }
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
    public static void initGraph(GraphManager graphManager,DataGatherManager dataGatherManager){
        for(Map.Entry<Author, Vector<Paper>> entry : dataGatherManager.dicAuthorPaper.entrySet()){
            if(!entry.getKey().getFlag()) continue;  //不存在该作者则进行下一个循环
            //如果不存在作者结点则创建
            if(!graphManager.Graph.containsVertex(entry.getKey())){
                graphManager.Graph.addVertex(entry.getKey());
            }
            //遍历该作者的论文
            for(Paper paper: entry.getValue()){
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
                            }
                            //创建边
                            double citingKey = (double) 1 /(startNum * endNum);

                        }
                    }
                }
            }
        }
        culCitedTimes();
    }


}
