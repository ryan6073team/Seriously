package com.github.ryan6073.Seriously.Graph;
import com.github.ryan6073.Seriously.BasicInfo.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class GraphManager { //单例
    private static GraphManager mGraph = new GraphManager();
    public static GraphManager getInstance(){return mGraph;}
    public DirectedGraph<Author,Edge> Graph = new DefaultDirectedGraph<>(Edge.class);
    private GraphManager(){}

}
