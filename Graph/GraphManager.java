package com.github.ryan6073.Seriously.Graph;
import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.TimeInfo;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class GraphManager { //单例
    private static GraphManager mGraph = new GraphManager();
    public static GraphManager getInstance(){return mGraph;}
    private Map<TimeInfo,DirectedGraph<Author,Edge>> GraphItems = new HashMap<>();

    //获取目标子图
    public DirectedGraph<Author,Edge> getGraphItem(int year,int month){
        for(Map.Entry<TimeInfo,DirectedGraph<Author,Edge>> entry : GraphItems.entrySet()){
            if(entry.getKey().year==year&&entry.getKey().month==month)
                return GraphItems.get(entry);
        }
        return null;
    }
    //加入目标子图
    public void addGraphItem(int year, int month, DirectedGraph<Author,Edge> Item){GraphItems.put(new TimeInfo(year,month),Item);}

    //创建初始图，一切故事从这里开始
    public DirectedGraph<Author,Edge> Graph = new DefaultDirectedGraph<>(Edge.class);
    //关于初始图和子图的创建时间参数请查看DataGatherManager新增的startYear/Month和finalYear/Month
    //注意这些图都是作者引用图，而GraphInit中的paperGraph是论文引用图
    private GraphManager(){}

}
