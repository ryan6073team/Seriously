package com.github.ryan6073.Seriously.Impact;
import com.github.ryan6073.Seriously.BasicInfo.Author;
import com.github.ryan6073.Seriously.BasicInfo.DataGatherManager;
import com.github.ryan6073.Seriously.BasicInfo.Edge;
import Jama.Matrix;
import org.jgrapht.graph.DirectedPseudograph;
import com.github.ryan6073.Seriously.BasicInfo.Paper;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

public class CalGraph {
    //对于转移矩阵做如下理解：
    //transition的第j列都代表作者j的学术来源分布，做归一化处理后transition[i][j]代表作者j的所有学术成果从作者i获得的启发性帮助占其在科研过程中获得的
    //全部帮助(即第j列数值和，已归一化为1.0)的比例，但这种设想是建立在
    // 1.作者的每一篇论文所获取的启发性帮助相同（一篇论文的发出的边的citingkey之和皆为1）
    // 2.被引用论文对作者提供的启发性帮助被被引论文的作者平均分配（因为没有考虑论文作者的贡献大小，因此只能平均分配）（一条Edge的citingkey=1/被引论文和引用论文作者数量的乘积）
    // 的基础上
    // 在这些假设的基础上可以得出初步判断：若发现转移矩阵中很多列的数值（即获得的启发性帮助占比）都向某或者某几行（以i行为例）聚集，则代表很多作者
    // 都从作者i的论文中获得了启发性帮助，因此其影响力会相应的高
    // 而这种影响力数值的大小会在后续的转移矩阵的计算中体现
    static Map<Integer,String> tempMap;
    private static double[][] getGraphMatrix(DirectedPseudograph<Author, Edge> mGraph){
        //传入的图中存在孤立点
        //这种情况是可能存在的：例如当某些作者只有一篇论文而程序恰好需要删去这篇论文来对比前后相关作者的影响力变化时，
        // 与论文相关的边都被删去该作者即变成孤立点
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
                DataGatherManager dataGatherManager = DataGatherManager.getInstance();
                int nowAuthorOrder = _tempMap2.get(nowAuthor.getOrcid());
                int citedAuthorOrder = _tempMap2.get(citedAuthor.getOrcid());
                tempMatrix[citedAuthorOrder][nowAuthorOrder]+=edgeItem.getCitingKey();
            }
            //再处理作者引用列表为空的论文,将其设置为自引,即代表学术来源其本身（待定）
//            for (Paper paper:DataGatherManager.getInstance().dicAuthorPaper.get(nowAuthor)){
//                if(paper.getCitingList().size()==0){
//                    int nowAuthorOrder = _tempMap2.get(nowAuthor.getOrcid());
//                    tempMatrix[nowAuthorOrder][nowAuthorOrder]+=1.0/paper.getAuthorIDList().size();
//                }
//            }
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
        }
    }
    //会存在于转移矩阵的都是有成熟作品的作者，但同时还存在只有不成熟作品和暂未出现的作者，对于他们ans没有对应值，归一化亦与他们无关，但是TargetVector应该包含他们
    //其中前者初始值设置为0 后者初始值设置为-1
    private static double [] getTargetVector( double [][] transitionMatrix, int matrixSize, double D){
        int currentAuthorNum = transitionMatrix.length;
        int[] authorMark = new int[currentAuthorNum];
        int singlePointNum = 0;
        for(int i=0;i<currentAuthorNum;i++){
            //line是竖行和 row是横行和
            double lineSum=0.0,rowSum=0.0;
            authorMark[i]=0;
            //若引用矩阵中的某一行与某一列都为0，则证明该点为孤立点
            for(int j=0;j<currentAuthorNum;j++){
                rowSum+=transitionMatrix[i][j];
                lineSum+=transitionMatrix[j][i];
            }
            //如果作者的论文及没有被别人引用也没有引用别人则直接将这个孤立点从矩阵中删去
            if(lineSum+rowSum==0.0){
                //代表此作者在图中为孤立点
                authorMark[i]=-1;
                singlePointNum++;
            }
            //如果作者论文没有引用别人但是被别人引用了，将其设置为自引，
            // 因为即使是将权重平均出去，也应当是将权重平分给写这篇论文时相关领域已经存在的所有作者，
            // 这太难
            // 反正不好！因为转移矩阵的1.0代表的是权重，一个本身被他人引用的影响力就比较高的作者如果把贡献权重付给自己那么他的影响力会他妈高的吓人
            // 但是你的论文有没有引用别人的理应不能对自身影响力造成很大波动
            else if(lineSum == 0.0){
                for(int j=0;j<currentAuthorNum;j++){
                    transitionMatrix[j][i] = 1.0/currentAuthorNum;
                }
            }
            //寻常情况下检查归一化即可
            else if(Math.abs(lineSum-1.0)>0.00001) {
                System.out.println("矩阵归一化异常");
                return null;
            }
        }
        if(currentAuthorNum==0){
            double[] ret = new double[matrixSize];
            for(int i=0;i<matrixSize;i++)
                ret[i]=-1.0;
            //存在的作者初始化影响力值应该为0 不存在的作者影响力值非法应该为-1.0
            for(String orcid:DataGatherManager.getInstance().dicOrcidAuthor.keySet())
                if(DataGatherManager.getInstance().dicOrcidAuthor.get(orcid).getIfExist()==1)
                    ret[DataGatherManager.getInstance().dicOrcidMatrixOrder.get(orcid)]=0.0;
            return ret;
        }
        double [][] tureTransitionMatrix;
        int trueSize=currentAuthorNum - singlePointNum;
        // 当存在孤立点时重构转移矩阵
        if (singlePointNum != 0) {
            tureTransitionMatrix = new double[trueSize][trueSize];
            int row = 0; // 新矩阵的行索引
            for (int i = 0; i < currentAuthorNum; i++) {
                if (authorMark[i] == 0) { // 如果authorMark[i]为0，说明不是孤立点
                    int col = 0; // 新矩阵的列索引
                    for (int j = 0; j < currentAuthorNum; j++) {
                        if (authorMark[j] == 0) { // 同样检查列是否为孤立点
                            tureTransitionMatrix[row][col] = transitionMatrix[i][j];
                            col++;
                        }
                    }
                    row++;
                }
            }
        } else {
            tureTransitionMatrix = transitionMatrix;
        }

        double [][] authorVector = new double[1][trueSize];
        double [][] tempVector = new double[1][trueSize];
        for(int i=0;i<trueSize;i++){
            authorVector[0][i] = 1.0/trueSize;
            tempVector[0][i] = (1.0-D)/trueSize;
        }
        Matrix transpose = new Matrix(tureTransitionMatrix);
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
        for(int i=0;i<trueSize;i++)
            sum+=author.getArray()[i][0];
        double[] ans = new double[trueSize];
        for(int i=0;i<trueSize;i++)
            ans[i]=author.getArray()[i][0]/sum;

        double[] ret = new double[matrixSize];
        for(int i=0;i<matrixSize;i++)
            ret[i]=-1.0;
        //存在的作者初始化影响力值应该为0 不存在的作者影响力值非法应该为-1.0
        for(String orcid:DataGatherManager.getInstance().dicOrcidAuthor.keySet())
            if(DataGatherManager.getInstance().dicOrcidAuthor.get(orcid).getIfExist()==1)
                ret[DataGatherManager.getInstance().dicOrcidMatrixOrder.get(orcid)]=0.0;
        int currentAuthorID=0;
        for(int i=0;i<currentAuthorNum;i++) {
            if (authorMark[i] == 0) {
                ret[DataGatherManager.getInstance().dicOrcidMatrixOrder.get(tempMap.get(i))] = ans[currentAuthorID];
                currentAuthorID++;
            }
        }
        return ret;
    }
    //未出现的作者影响力值为-1
    public static Vector<Double> getGraphImpact(DirectedPseudograph<Author, Edge> mGraph){
        int matrixSize = DataGatherManager.getInstance().authorNum;

//        //打印图的信息
//        System.out.println("图的信息如下：");
//        System.out.println("图的顶点数目为："+mGraph.vertexSet().size());
//        System.out.println("图的边数目为："+mGraph.edgeSet().size());
//        System.out.println("图的顶点集合为：");
//        for(Author author:mGraph.vertexSet()){
//            System.out.println(author.getOrcid());
//        }
//        System.out.println("图的边集合为：");
//        for(Edge edge:mGraph.edgeSet()){
//            System.out.println(edge.getDoi());
//            System.out.println(mGraph.getEdgeSource(edge).getOrcid()+"->"+mGraph.getEdgeTarget(edge).getOrcid());
//            System.out.println("引用次数为："+edge.getCitingKey());
//        }

        //获得引用矩阵
        double [][] targetMatrix = getGraphMatrix(mGraph);
//        if(targetMatrix.length!=0){
//            System.out.println();
//        }
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
