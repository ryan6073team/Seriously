package com.github.ryan6073.Seriously.Coefficient;
import com.github.ryan6073.Seriously.BasicInfo.*;
import Jama.Matrix;
public class CoefficientStrategy {
    //我觉得既然已经用策略类把你的函数封装了，那么static标签删掉即可，这样不同的策略类对象
    //调用函数时处理的便都是自家的状态矩阵，安全点儿而且可以不同策略类
    //都是主观定义的
    //论文在高中低状态的影响力系数
    //这里的影响力系数如何获取其实也有待考量，所以按照矩阵的思路，也弄一个初始化函数会好一点/*？？？？1？？？？？*/
    // A method to initialize the impact coefficients
    public static double[] initImpactCoefficients() {
        double[] impactCoefficient = {1, 0.8, 0.5};
        return impactCoefficient;
    }

    //考虑到以后状态转移矩阵的设置肯定需要一些操作，所以这里把矩阵从函数里提出来变成成员变量，将矩阵的初始化过程和访问函数分开
    double[][][][] transitionMatrix;
    //...按你的说法就是：定义一个方法，初始化所有论文的状态转移矩阵？
    public double[][][][] initTransitionMatrix(){
        // 假设论文的引用量分为三个区间，分别是高，中，低
        // 假设论文的等级分为四个等级，分别是A，B，C，D
        transitionMatrix = new double[][][][]{
                // 高引用量区间的论文的状态转移概率矩阵
                {
                        // A级论文的状态转移概率矩阵
                        {
                                {0.9, 0.1, 0.0}, // 从高影响力转移到高影响力的概率是0.9，转移到中影响力的概率是0.1，转移到低影响力的概率是0.0
                                {0.2, 0.7, 0.1}, // 从中影响力转移到高影响力的概率是0.2，转移到中影响力的概率是0.7，转移到低影响力的概率是0.1
                                {0.0, 0.3, 0.7}  // 从低影响力转移到高影响力的概率是0.0，转移到中影响力的概率是0.3，转移到低影响力的概率是0.7
                        },
                        // B级论文的状态转移概率矩阵
                        {
                                {0.8, 0.2, 0.0},
                                {0.3, 0.6, 0.1},
                                {0.1, 0.4, 0.5}
                        },
                        // C级论文的状态转移概率矩阵
                        {
                                {0.7, 0.3, 0.0},
                                {0.4, 0.5, 0.1},
                                {0.2, 0.5, 0.3}
                        },
                        // D级论文的状态转移概率矩阵
                        {
                                {0.6, 0.4, 0.0},
                                {0.5, 0.4, 0.1},
                                {0.3, 0.6, 0.1}
                        }
                },
                // 中引用量区间的论文的状态转移概率矩阵
                {
                        // A级论文的状态转移概率矩阵
                        {
                                {0.8, 0.2, 0.0},
                                {0.3, 0.6, 0.1},
                                {0.1, 0.4, 0.5}
                        },
                        // B级论文的状态转移概率矩阵
                        {
                                {0.7, 0.3, 0.0},
                                {0.4, 0.5, 0.1},
                                {0.2, 0.5, 0.3}
                        },
                        // C级论文的状态转移概率矩阵
                        {
                                {0.6, 0.4, 0.0},
                                {0.5, 0.4, 0.1},
                                {0.3, 0.6, 0.1}
                        },
                        // D级论文的状态转移概率矩阵
                        {
                                {0.5, 0.5, 0.0},
                                {0.6, 0.3, 0.1},
                                {0.4, 0.7, 0.1}
                        }
                },
                // 低引用量区间的论文的状态转移概率矩阵
                {
                        // A级论文的状态转移概率矩阵
                        {
                                {0.7, 0.3, 0.0},
                                {0.4, 0.5, 0.1},
                                {0.2, 0.5, 0.3}
                        },
                        // B级论文的状态转移概率矩阵
                        {
                                {0.6, 0.4, 0.0},
                                {0.5, 0.4, 0.1},
                                {0.3, 0.6, 0.1}
                        },
                        // C级论文的状态转移概率矩阵
                        {
                                {0.5, 0.5, 0.0},
                                {0.6, 0.3, 0.1},
                                {0.4, 0.7, 0.1}
                        },
                        // D级论文的状态转移概率矩阵
                        {
                                {0.4, 0.6, 0.0},
                                {0.7, 0.2, 0.1},
                                {0.5, 0.8, 0.1}
                        }
                }
        };
        return transitionMatrix;
    }

    // 定义一个方法，根据论文的引用数据，论文等级，以及其他的相关因素，计算论文的状态转移概率矩阵，目前先从简单情况考虑，根据paper的等级和引用量区间得到状态矩阵即可
    public double[][] getTransitionMatrix(Paper paper, DataGatherManager dataGatherManager, String doi){
        // 根据论文的引用量和论文等级，获取对应的状态转移概率矩阵
        LevelManager.Level paperLevel = paper.getLevel(); // 获取论文的等级，返回0，1，2，3分别表示A，B，C，D
        LevelManager.CitationLevel citationLevel = paper.getCitationLevel(); // 获取论文的引用量区间，返回0，1，2分别表示高，中，低
        double[][] paperTransitionMatrix = transitionMatrix[citationLevel.getIndex()][paperLevel.getIndex()]; // 获取论文的状态转移概率矩阵
        return paperTransitionMatrix;
    }
    // 定义一个方法，根据论文的状态转移概率矩阵，计算论文在不同时间点的状态分布，这里就先认为不同时间点的状态转移矩阵相同嘛，不过这里应该是
    // 缺少了一个paper的函数参数在里面，这样就是一个很好的计算paper在time的状态分布的方法了
    public double[][] getStateDistribution(Paper paper){
        //还有就是你看现在你的代码里面又新增了个表示level的变量对吧，之前就是因为考虑到如果每个level都用数字去表示的话，一不小心就会出现含义混淆的情况
        //因此新增了一个叫做levelmanager的类用来集中管理各个等级和数字之间的转换关系，你可以看到等级转数字用到了index，数字转等级用到了函数RanktoLevel
        //具体情况你把代码丢到gpt里让人家分析一下就好，不难的
        //这个类的理想情况是能够将所有leve变量容纳进去，并设定出统一的数字和等级之间的转换关系，从这里你就可以看到现在这个类还不完善，至少有两个点需要改善：
        //1.RanktoLevel函数的数字和index数字之间相隔1，也就是说正反向转换函数的数字含义有偏差，这当然不行，不过先用着再说
        //2.journal的rank还是integer而不是level枚举类，这才让你小子钻了空子可以直接根据论文的期刊等级的int得到论文的int等级，这也不好，不过之后再改
        //所以希望你能够把citationLevel放到LevelManager里面，具体来说就是往里加个enum类嘛，可以根据你的具体需求去定义数字和level之间的转换函数
        //不过请把名字定义的清晰一点，不要定义个level2这种蠢爆的名字蟹蟹，所以其实manager现在的那个level名字也要改，不过以后再说
        //然后定义完了之后就把你的代码改成匹配的样子就好，具体怎么用我已经在getTransitionMatrix和getStateDistribution展示给你看/*？？？？？？2？？？？？？？？*/
        double[] initialState = getInitialState(paper);
        int citationLevel = getCitationLevel(paper); // 获取论文的引用量区间，返回0，1，2分别表示高，中，低
        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        LevelManager.Level paperLevel = paper.getLevel();
        // 注意看，level的能力是这样用的，之后它会使用getIndex函数将等级转换为数字然后读取数组
        // 虽然这样子做你可能会觉得很繁琐，但是它保证了安全性
        int timeLength = 12;// 定义论文的保护期长度
        Matrix[] stateDistribution = new Matrix[timeLength]; // 定义一个Matrix数组，表示论文在不同时间点的状态分布
        stateDistribution[0] = new Matrix(initialState, 1); // 将论文的初始状态分布转换为一个1行的Matrix对象，赋值给第一个时间点的状态分布
        // 用一个循环，根据论文的状态转移概率矩阵，计算论文在后续时间点的状态分布
        for(int i = 1; i < timeLength; i++){
            // 用times方法，计算论文在当前时间点的状态分布
            stateDistribution[i] = stateDistribution[i-1].times(Matrix.constructWithCopy(transitionMatrix[citationLevel][paperLevel.getIndex()])); //传入论文在前一个时间点的状态分布和论文的状态转移概率矩阵，返回论文在当前时间点的状态分布
        }
        double[][] stateDistributionArray = new double[timeLength][3]; // 定义一个二维数组，用来存储stateDistribution的二维数组表示
        // 用一个循环，把stateDistribution的每个元素都转换成二维数组，并赋值给stateDistributionArray
        for(int i = 0; i < timeLength; i++){
            stateDistributionArray[i] = stateDistribution[i].getArray()[0]; // 用getArray方法获取stateDistribution[i]的二维数组表示，然后取第0行，赋值给stateDistributionArray[i]
        }
        return stateDistributionArray; // 返回stateDistribution的二维数组表示
    }
    //唔..这里的矩阵相乘操作其实我们的项目里面有一个叫做jama的java包，它是我用来做矩阵运算用的，你以后想用的时候网上查查相关用法即可，就不用再定义函数了
    //这个包我给你的压缩包里面应该有，叫做jama-1.0.3.jar
    //所以你的第三个任务就是把代码弄成使用jama的Matrix变量的形式，关于矩阵的运算统一使用相同的包/*？？？？？？3？？？？？？？？*/
    public double[] matrixMultiply(double[] vector, double[][] matrix) {
        int m = vector.length;
        int n = matrix[0].length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                result[i] += vector[j] * matrix[j][i];
            }
        }
        return result;
    }
    private double[] getInitialState(Paper paper) {// 获取论文的初始状态分布，返回一个一维数组，表示论文在高，中，低影响力状态的概率
        //这里我在datamanager里面新增了一个叫做dicDoiStateCoefficient的成员变量，它是从doi到论文当前状态数组的映射，所以前三个字母是dic(tionary)
        //datamanager里面的其它变量也是这样命名的
        //所以我增加了一个paper参数在函数里
        return DataGatherManager.getInstance().dicDoiStateCoefficient.get(paper.getDoi());
    }
    // 定义一个方法，根据论文的状态分布，计算论文的影响力系数的期望值
    public double getPaperImpactCoefficientExpectation(double[] stateDistribution){
        double paperImpactCoefficientExpectation = 0; // 定义一个变量，表示论文的影响力系数的期望值
        // 用一个循环，计算论文的影响力系数的期望值
        double[] impactCoefficient=initImpactCoefficients();
        for(int i = 0; i < 3; i++){
            // 论文的影响力系数的期望值等于它的状态分布和它的状态影响力系数的加权平均值
            paperImpactCoefficientExpectation += stateDistribution[i] * impactCoefficient[i]; // 将论文在当前状态的概率乘以论文在当前状态的影响力系数，累加到论文的影响力系数的期望值上
        }
        return paperImpactCoefficientExpectation; // 返回论文的影响力系数的期望值
    }

    // 定义一个方法，根据论文的被引排名，划分论文的引用量区间
    public int getCitationLevel(Paper paper){
        // 假设论文的引用量区间是根据论文在同一领域，同一年份，同一期刊的被引排名来划分的
        // 假设论文的引用量区间有三个，分别是高，中，低
        // 假设论文的被引排名在前10%的属于高引用量区间，返回0
        // 假设论文的被引排名在10%到50%之间的属于中引用量区间，返回1
        // 假设论文的被引排名在50%以下的属于低引用量区间，返回2
        int citationRank = getCitationRank(paper);
        int citationLevel = 0; // 定义一个变量，表示论文的引用量区间
        if(citationRank <= 0.1){
            // 论文的被引排名在前10%的属于高引用量区间，返回0
            citationLevel = 0;
        }else if(citationRank > 0.1 && citationRank <= 0.5){
            // 论文的被引排名在10%到50%之间的属于中引用量区间，返回1
            citationLevel = 1;
        }else{
            // 论文的被引排名在50%以下的属于低引用量区间，返回2
            citationLevel = 2;
        }
        return citationLevel; // 返回论文的引用量区间
    }
    private int getCitationRank(Paper paper) {// 获取论文的被引排名，返回一个整数，表示论文在同一领域，同一年份，同一期刊的被引排名
        return 0;
    }
    // 定义一个方法，根据论文的期刊等级，划分论文的等级，因为已经有level，所以这个函数删掉了
    //
//    public int getPaperLevel(DataGatherManager dataGatherManager, String doi) {
//        Paper paper = dataGatherManager.dicDoiPaper.get(doi);
//        int paperLevel = 1;
//        if (paper != null) {
//            Journal journal = dataGatherManager.dicNameJournal.get(paper.getJournal());
//
//            if (journal != null) {
//                Integer rank = journal.getRank();
//
//                if (rank != null) {
//                    paperLevel = rank;
//                }
//            }
//        }
//        return paperLevel-1;
//    }

}

/*
1.把impactCoefficient给初始化了
2.新增citationLevel的相关类
3.将代码与jama适配
 */