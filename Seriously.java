package com.github.ryan6073.Seriously;

import com.github.ryan6073.Seriously.BasicInfo.*;
import com.github.ryan6073.Seriously.Graph.GraphInit;
import com.github.ryan6073.Seriously.Graph.GraphManager;
import com.github.ryan6073.Seriously.Graph.GraphStore;
import com.github.ryan6073.Seriously.Impact.CalImpact;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

public class Seriously {

    public static void main(String[] args) {


        DataGatherManager dataGatherManager = DataGatherManager.getInstance();
        String file = "C:\\Users\\21333\\Documents\\WeChat Files\\wxid_n7kbhoubosek22\\FileStorage\\File\\2024-02\\author_net.txt";
        FileInput.tempInit(dataGatherManager, file);
        System.out.println("完成文件初始化");

        //新增的初始化datagather的startyear/month finalyear/month
        dataGatherManager.initYearMonth();
        System.out.println("完成时间初始化");

        GraphManager graphManager = GraphManager.getInstance();

        //负责获取论文之间的引用关系用以构成图并初始化：各论文的被引用列表、各论文的引用等级、矩阵
        GraphInit.initGraph(graphManager, dataGatherManager, dataGatherManager.firstYear, dataGatherManager.firstMonth);
        System.out.println("第一年：" + dataGatherManager.firstYear + " 第一个月：" + dataGatherManager.firstMonth);
        JSONObject obj = new JSONObject();
        obj.put("year", dataGatherManager.firstYear+10);
        obj.put("month", dataGatherManager.firstMonth);
        obj.put("graphName", dataGatherManager.firstYear+10 + "-" + dataGatherManager.firstMonth);
        obj.put("status", 1);

        // 将JSON对象写入文件
        try (FileWriter fileIn = new FileWriter("data.json")) {
            fileIn.write(obj.toJSONString());
            fileIn.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                // 等待10秒
                Thread.sleep(10000);
                JSONParser parser = new JSONParser();

                try (FileReader reader = new FileReader("data.json")) {
                    // 解析JSON文件
                    JSONObject objRead = (JSONObject) parser.parse(reader);
                    reader.close();
                    boolean scStatus = (Long) objRead.get("status") == 1;
                    if (!scStatus) {
                        continue;//此时status为0，说明还没有完成更新
                    }
                    // 提取年份和月份
                    long scYear = (Long) objRead.get("year");
                    long scMonth = (Long) objRead.get("month");

                    //更新年份月份，一般情况月份+1，如果月份为12，则年份+1，月份为1
                    if (scMonth == 12) {
                        scYear++;
                        scMonth = 1;
                    } else {
                        scMonth++;
                    }
                    objRead.put("year", scYear);
                    objRead.put("month", scMonth);
                    objRead.put("graphName", scYear + "-" + scMonth);
                    objRead.put("status", 0);

                    //直接从start的下一个月开始更新作者的影响力，当时间推移到published year published month时，相应时间发表的论文life为1
                    for (int i = dataGatherManager.firstYear * 12 + dataGatherManager.firstMonth + 1; i <= dataGatherManager.finalYear * 12 + dataGatherManager.finalMonth + 13; i++) {
                        //注意在startyear startmonth的时候就开始更新了
                        int year, month;
                        if (i % 12 == 0) {
                            month = 12;
                            year = i / 12 - 1;
                        } else {
                            month = i % 12;
                            year = i / 12;
                        }
                        //更新母图并获取论文集
                        Vector<Vector<String>> currentPapers = GraphManager.getInstance().updateGraph(year, month);
                        if (year == scYear && month == scMonth) {
                            GraphStore.storeGraph(year + "-" + month, GraphManager.getInstance().Graph);
                            break;
                        }
                        System.out.println();
                    }

                    // 将JSON对象写入文件
                    try (FileWriter fileIn = new FileWriter("data.json")) {
                        fileIn.write(objRead.toJSONString());
                        fileIn.flush();
                    }

                } catch (IOException | org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //彻底关闭driver
            GraphStore.getInstance().closeDriver();
            for (Map.Entry<String, Author> entry : dataGatherManager.dicOrcidAuthor.entrySet()) {
                System.out.println(entry.getValue().getAuthorName() + ' ' + entry.getValue().getAuthorImpact() + ' ' + entry.getValue().getLevel());
            }


        }
    }
}