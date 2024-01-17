package com.github.ryan6073.Seriously.Impact;
import com.github.ryan6073.Seriously.BasicInfo.Author;
import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.BasicInfo.Edge;
import Jama.Matrix;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

public class CalGraph {
    static Map<Integer,String> tempMap;
    private static double[][] getGraphMatrix(DirectedGraph<Author, Edge> mGraph){
        Iterator<Author> mBreadthFirstIterator = new BreadthFirstIterator<>(mGraph);
        int authorNum = mGraph.vertexSet().size();
        Map<Integer,String> _tempMap = new HashMap<>();
        Map<String,Integer> _tempMap2 = new HashMap<>();
        double[][] tempMatrix = new double[mGraph.vertexSet().size()][mGraph.vertexSet().size()];
        for(int i=0;i<authorNum;i++)
            for(int j=0;j<authorNum;j++)
                tempMatrix[i][j] = 0.0;
        int i=0;
        for(Author author:mGraph.vertexSet()){
            _tempMap.put(i,author.getOrcid());
            _tempMap2.put(author.getOrcid(),i);
            i++;
        }
        while (mBreadthFirstIterator.hasNext()){
            Author nowAuthor = mBreadthFirstIterator.next();
            Set<Edge> outgoingEdges = mGraph.edgesOf(nowAuthor);
            Set<Edge> ans = new HashSet<>();
            Iterator<Edge> iterator = outgoingEdges.iterator();
            while (iterator.hasNext()){
                Edge nowEdge = iterator.next();
                if(mGraph.getEdgeSource(nowEdge)==nowAuthor){
                    ans.add(nowEdge);
                }
            }
            //获得从nowAuthor发出的边集合
            outgoingEdges = ans;
            Iterator<Edge> outgoingIterator = outgoingEdges.iterator();
            while(outgoingIterator.hasNext()){
                Edge edgeItem = outgoingIterator.next();
                Author citedAuthor = mGraph.getEdgeTarget(edgeItem);
                int nowAuthorOrder = _tempMap2.get(nowAuthor.getOrcid());
                int citedAuthorOrder = _tempMap2.get(citedAuthor.getOrcid());
                tempMatrix[citedAuthorOrder][nowAuthorOrder]+=edgeItem.getCitingKey();
            }
        }
        tempMap = _tempMap;
        return tempMatrix;
    }

    private static void processTransitionMatrix(double [][] sourceMatrix){
        //对矩阵进行归一化处理
        for(int i=0;i<sourceMatrix.length;i++){
            double sum = 0.0;
            for(int j=0;j<sourceMatrix.length;j++)
                sum += sourceMatrix[j][i];
            for(int j=0;j<sourceMatrix.length;j++)
                if(sum!=0.0)
                    sourceMatrix[j][i]=sourceMatrix[j][i]/sum;
                else sourceMatrix[j][i]=0.0;
        }
    }
    private static double [] getTargetVector( double [][] transitionMatrix, int matrixSize, double D){
        D = 0.85;
        int currentAuthorNum = transitionMatrix.length;
        for(int i=0;i<currentAuthorNum;i++){
            double sum=0.0;
            for(int j=0;j<currentAuthorNum;j++){
                sum+=transitionMatrix[j][i];
            }
            if(sum!=1.0) {
                System.out.println("矩阵归一化异常");
                return null;
            }
        }
        double [][] authorVector = new double[1][currentAuthorNum];
        double [][] tempVector = new double[1][currentAuthorNum];
        for(int i=0;i<currentAuthorNum;i++){
            authorVector[0][i] = 1.0/currentAuthorNum;
            tempVector[0][i] = (1.0-D)/currentAuthorNum;
        }
        Matrix transpose = new Matrix(transitionMatrix);
        //列向量
        //作者向量
        Matrix author = (new Matrix(authorVector)).transpose();
        //阻尼因子
        Matrix temp = (new Matrix(tempVector)).transpose();
        //算一百次？？？？？？？？？
        for(int i=0;i<100;i++){
            author = (transpose.times(author)).times(D).plus(temp);
            //transpose*author*0.85+temp
        }
        //权重向量归一化
        double sum=0.0;
        for(int i=0;i<currentAuthorNum;i++)
            sum+=author.getArray()[i][0];
        double[] ans = new double[currentAuthorNum];
        for(int i=0;i<currentAuthorNum;i++)
            ans[i]=author.getArray()[i][0]/sum;

        double[] ret = new double[matrixSize];
        for(int i=0;i<matrixSize;i++)
            ret[i]=-1.0;

        for(int i=0;i<currentAuthorNum;i++){
            ret[DataGatherManager.getInstance().dicOrcidMatrixOrder.get(tempMap.get(i))] = ans[i];
        }
        return ret;
    }

    public static Vector<Double> getGraphImpact(DirectedGraph<Author, Edge> mGraph){
        int matrixSize = DataGatherManager.getInstance().authorNum;
        //获得引用矩阵
        double [][] targetMatrix = getGraphMatrix(mGraph);
        //获得转移矩阵
        processTransitionMatrix(targetMatrix);
        //获得作者影响力数组
        double[] impactArray = getTargetVector(targetMatrix, matrixSize, 0.85);
        //转换成向量输出
        Vector<Double> graphImpact = new Vector<>();
        for(int i=0;i<matrixSize;i++)
            graphImpact.add(impactArray[i]);
        return graphImpact;
    }
}
