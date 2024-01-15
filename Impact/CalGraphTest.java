package com.github.ryan6073.Seriously.Impact;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Graph.GraphStore;
import com.github.ryan6073.Seriously.BasicInfo.Author;
import com.github.ryan6073.Seriously.BasicInfo.Edge;
import org.jgrapht.DirectedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalGraphTest {

    private CalGraph calGraph;
    private GraphStore storeGraph;

    @BeforeEach
    void setUp() {
        //calGraph = new CalGraph();
        //storeGraph = new GraphStore("http://localhost:7474/browser/","neo4j","1975LIliabili");
        // 在这里设置测试环境，例如初始化对象、创建测试数据等
    }

    @AfterEach
    void tearDown() {
        // 在这里清理测试环境，如果有必要的话
    }

    @Test
    void getTargetVector() {
        //getTargetVector传入的向量为行引用列[citing][cited]
        double[][] transitionMatrix = {
            {0.0, 0.5, 0.5},
            {0.0, 0.0, 1.0},
            {1.0, 0.0, 0.0}
        };
        int matrixSize = transitionMatrix.length;
        double D = 0.85;
        double[] result = CalGraph.getTargetVector(transitionMatrix, matrixSize, D);
        // 确认结果的长度
        assertEquals(matrixSize, result.length, "目标向量长度不正确");

        // 确认结果的归一化（各元素之和应接近1）
        double sum = 0;
        for (double v : result) {
            sum += v;
            System.out.println(v);
        }
        System.out.println();
        assertEquals(1.0, sum, 0.01, "目标向量未正确归一化");
    }

    @Test
    void getGraphImpact() {
        // 编写针对 getGraphImpact() 方法的测试代码

    }
}
