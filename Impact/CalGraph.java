package com.github.ryan6073.Seriously.Impact;

import com.github.ryan6073.Seriously.BasicInfo.Author;
import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.BasicInfo.Edge;
import Jama.Matrix;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class CalGraph {
    public static double[][] getGraphMatrix(DirectedGraph<Author, Edge> mGraph){
        Iterator<Author> mBreadthFirstIterator = new BreadthFirstIterator<>(mGraph);
        int authorNum = mGraph.vertexSet().size();
        double[][] Matrix = new double[authorNum][authorNum];
        //初始化
        for(int i=0;i<authorNum;i++)
            for(int j=0;j<authorNum;j++)
                Matrix[i][j]=0.0;
        int publishednum=0;
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
                int nowAuthorOrder = DataGatherManager.getInstance().dicOrcidMatrixOrder.get(nowAuthor.getOrcid());
                int citedAuthorOrder = DataGatherManager.getInstance().dicOrcidMatrixOrder.get(citedAuthor.getOrcid());
                //此矩阵实际上与定义的引用矩阵互为转置矩阵
                Matrix[nowAuthorOrder][citedAuthorOrder]+=edgeItem.getCitingKey();
            }
        }
        return Matrix;
    }

    public static void getTransitionMatrix(double [][] sourceMatrix, int MatrixSize){
        //此矩阵为转移矩阵的转置矩阵
        for(int i=0;i<MatrixSize;i++){
            double sum = 0.0;
            for(int j=0;j<MatrixSize;j++)
                sum += sourceMatrix[i][j];
            for(int j=0;j<MatrixSize;j++)
                if(sum!=0.0)
                    sourceMatrix[i][j]=sourceMatrix[i][j]/sum;
                else sourceMatrix[i][j]=0.0;
        }
    }
    public static double [] getTargetVector( double [][] transitionMatrix, int matrixSize, double D){
        D = 0.85;
        double [][] authorVector = new double[1][matrixSize];
        double [][] tempVector = new double[1][matrixSize];
        for(int i=0;i<matrixSize;i++){
            authorVector[0][i] = 1.0/matrixSize;
            tempVector[0][i] = (1.0-D)/matrixSize;
        }
        Matrix temp = new Matrix(transitionMatrix);
        //因为转移矩阵 列->行 为引用关系，而初始化时 行->列 为引用关系，因此需要转置
        Matrix transpose = temp.transpose();
        //列向量
        //作者向量
        Matrix author = (new Matrix(authorVector)).transpose();
        //阻尼因子
        temp = (new Matrix(tempVector)).transpose();
        //算一百次？？？？？？？？？
        for(int i=0;i<100;i++){
            author = (transpose.times(author)).times(D).plus(temp);
            //transpose*author*0.85+temp
        }
        //权重向量归一化
        double sum=0.0;
        for(int i=0;i<matrixSize;i++)
            sum+=author.getArray()[i][0];
        double[] ans = new double[matrixSize];
        for(int i=0;i<matrixSize;i++)
            ans[i]=author.getArray()[i][0]/sum;
        return ans;
    }

//    public static void calAccuImpact(double[] impactArray, int authorNum){
//        for(int i=DataGatherManager.getInstance().startMonth;i<=12;i++){
//            DirectedGraph<Author,Edge> graphItem = GraphManager.getInstance().getGraphItem(DataGatherManager.getInstance().startYear,i);
//            double[] tempImpact = getGraphItemImpact(graphItem);
//            for(int j=0;j<authorNum;j++)
//                impactArray[j] += tempImpact[j];
//        }
//        for(int i=DataGatherManager.getInstance().startYear+1;i<=DataGatherManager.getInstance().finalYear-1;i++){
//            for(int j=1;j<=12;j++){
//                DirectedGraph<Author,Edge> graphItem = GraphManager.getInstance().getGraphItem(i,j);
//                double[] tempImpact = getGraphItemImpact(graphItem);
//                for(int k=0;k<authorNum;k++)
//                    impactArray[k] += tempImpact[k];
//            }
//        }
//        for(int i=1;i<=DataGatherManager.getInstance().finalMonth;i++){
//            DirectedGraph<Author,Edge> graphItem = GraphManager.getInstance().getGraphItem(DataGatherManager.getInstance().finalYear,i);
//            double[] tempImpact = getGraphItemImpact(graphItem);
//            for(int j=0;j<authorNum;j++)
//                impactArray[j] += tempImpact[j];
//        }
//    }

    public static double[] getGraphItemImpact(DirectedGraph<Author,Edge> graphItem){

        return null;
    }

    public static Vector<Double> getGraphImpact(DirectedGraph<Author, Edge> mGraph){

        int matrixSize = mGraph.vertexSet().size();
        //获得引用矩阵
        double [][] targetMatrix = getGraphMatrix(mGraph);
        //获得转移矩阵
        getTransitionMatrix(targetMatrix,matrixSize);
        //获得作者影响力数组
        double[] impactArray = getTargetVector(targetMatrix, matrixSize, 0.85);
        //转换成向量输出
        Vector<Double> graphImpact = new Vector<>();
        for(int i=0;i<matrixSize;i++)
            graphImpact.add(impactArray[i]);
        return graphImpact;
    }
}
