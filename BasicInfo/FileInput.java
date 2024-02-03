package com.github.ryan6073.Seriously.BasicInfo;

import com.github.ryan6073.Seriously.TimeInfo;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInput {
    public static void initJournalToIF(DataGatherManager dataGatherManager) {
        // 创建 reader
        try (BufferedReader br = Files.newBufferedReader(Paths.get(Objects.requireNonNull(ConfigReader.getFilePath2())))) {
            // CSV文件的分隔符
            String DELIMITER = ",";
            // 按行读取
            String line;
            while ((line = br.readLine()) != null) {
                // 分割
                String[] columns = line.split(DELIMITER);
                dataGatherManager.dicJournalIF.put(columns[0], Double.valueOf(columns[1]));
                // 打印行
                System.out.println(String.join(", ", columns));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Author initAuthor(DataGatherManager dataGatherManager, String line, Vector<Author> authors, BufferedReader reader) {
        // 解析作者信息
        // 正则表达式，匹配数字
        String[] parts = line.split("\\s+");
        // 获取最后一个数字，即文章数

        int paperCount = Integer.parseInt(parts[parts.length - 1]);
        // 获取导数第二个数字，即机构数
        int institutionCount = Integer.parseInt(parts[parts.length - 2]);
        String authorId = parts[parts.length - 3];
        StringBuilder authorNameBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 3; i++) {
            authorNameBuilder.append(parts[i]).append(" ");
        }
        String authorName = authorNameBuilder.toString().trim();
        Author author = new Author(authorName, authorId);



        Vector<String> institutions = new Vector<>();
        for (int i = 0; i < institutionCount; i++) {
            try {
                String institution = reader.readLine();
                if(!Objects.equals(institution, "")){
                    institutions.add(institution);
                    author.addAuthorInstitution(institution);
                    }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (String institutionName : institutions) {

            if (dataGatherManager.institutionFind(institutionName)) {
                Institution institution = dataGatherManager.institutionGet(institutionName);
                institution.institutionAuthors.add(authorId);
            } else {
                Institution institution = new Institution();
                institution.institutionName = institutionName;
                institution.institutionAuthors.add(authorId);
                dataGatherManager.addInstitution(institution.institutionName,institution);
            }

        }
        authors.add(author);
        dataGatherManager.addDicOA(author);

        dataGatherManager.authorNum += 1;//如果分批读取再做修改，改成+=即可    我觉得直接改成+1即可，这样可以直接处理分批读取问题
        return author;
    }

    public static void initPaperandJournal(BufferedReader reader, DataGatherManager dataGatherManager, String line, Author author) throws IOException {
        Vector<Paper> author_papers = new Vector<>();

        String[] parts = line.split("\\s+");
        // 获取最后一个数字，即文章数
        int paperCountInt = Integer.parseInt(parts[parts.length - 1]);
        // 获取导数第二个数字，即机构数
        int institutionCount = Integer.parseInt(parts[parts.length - 2]);
        String authorId = parts[parts.length - 3];
        StringBuilder authorNameBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 3; i++) {
            authorNameBuilder.append(parts[i]).append(" ");
        }
        String authorName = authorNameBuilder.toString().trim();


        // 解析每篇论文信息
        for (int i = 0; i < paperCountInt; i++) {
            String CountLine = reader.readLine();
            String paperLine = reader.readLine();

            // 正则表达式匹配DOI，它以数字开始和结束
            Pattern doiPattern = Pattern.compile("\\b(10\\S{6,})\\b");
            Matcher doiMatcher = doiPattern.matcher(paperLine);
            int citedPaperCount = Integer.parseInt(CountLine);
            // 找到DOI的位置
            String paperDoi = "";
            if (doiMatcher.find()) {
                paperDoi = doiMatcher.group();
            }

            // 使用DOI分割数据行，这样我们可以单独获取论文名和剩余部分
            String[] parts1 = paperLine.split(Pattern.quote(paperDoi));

            // 论文名在第一部分，去除首尾空格
            String paperName = parts1[0].trim();

            // 期刊名称、年份和月份在第二部分
            // 假设年份和月份总是在字符串的末尾，并且是连续的数字
            Pattern yearMonthPattern = Pattern.compile("\\d{4} \\d{1,2}$");
            Matcher yearMonthMatcher = yearMonthPattern.matcher(parts1[1]);

            // 找到年份和月份
            String year = "";
            String month = "";
            if (yearMonthMatcher.find()) {
                String[] yearMonth = yearMonthMatcher.group().split(" ");
                year = yearMonth[0];
                month = yearMonth[1];
            }

            // 获取期刊名，它位于DOI和年份之间
            String paperJournal = parts1[1].substring(0, parts1[1].length() - year.length() - month.length() - 2).trim(); // 减去2个空格

            Vector<String> citedPapers = new Vector<>();
            for (int j = 0; j < citedPaperCount; j++) {
                String citedPaperDoi = reader.readLine();

                citedPapers.add(citedPaperDoi);
            }

            Paper paper = new Paper();
            Journal journal = new Journal();

            paper.paperName = paperName;
            paper.doi = paperDoi;
            paper.setJournal(paperJournal);
            paper.setYear(Integer.parseInt(year));
            paper.setMonth(Integer.parseInt(month));
//                    paper.paperStatus = CitingStatusTypes.choiceTypes(paperStatus);
//                    if (paper.paperStatus != null) {
//                        paper.setYear(paperYear, paper.paperStatus);
//                    }
            paper.citingList.addAll(citedPapers);
            paper.authorIDList.add(authorId);//考虑ID和名字都各有优劣

            journal.setJournalName(paperJournal);
            journal.journalPapers.add(paper.doi);


            // 判断paper是否已经存在于datagathermanager的papers列表中，如果存在则直接添加作者，否则创建新的paper对象
            if (dataGatherManager.paperFind(paperDoi)) {
                paper = dataGatherManager.paperGet(paperDoi);
                paper.authorIDList.add(authorId);
            } else {
                dataGatherManager.addPaper(paper);


            }
            dataGatherManager.addDicDP(paper);
            dataGatherManager.addPaper(paper);//应该可以删掉

            initDicTimeInfoDoi(dataGatherManager,paper);

            if (dataGatherManager.journalFind(paperJournal)) {

                journal = dataGatherManager.journalGet(paperJournal);

                journal.journalPapers.add(paper.doi);
            } else {
                dataGatherManager.addJournal(journal);
                dataGatherManager.addDicNJ(paperJournal, journal);
            }
            author_papers.add(paper);

        }
        //将论文交给其作者
        dataGatherManager.addDicAP(author, author_papers);
    }

    public static void initDicTimeInfoDoi(DataGatherManager dataGatherManager,Paper paper){
        //提取论文的时间信息
        int year = paper.publishedYear;
        int month = paper.publishedMonth;
        TimeInfo timeInfo = new TimeInfo(year,month);
        String doi = paper.getDoi();
        Vector<String> dois;
        if(dataGatherManager.dicTimeInfoDoi.containsKey(timeInfo)) {
            dois = dataGatherManager.dicTimeInfoDoi.get(timeInfo);
            dois.add(doi);
            dataGatherManager.dicTimeInfoDoi.put(timeInfo,dois);
        }else{
            dois = new Vector<>();
            dois.add(doi);
            dataGatherManager.dicTimeInfoDoi.put(timeInfo,dois);
        }
    }


    public static void init(DataGatherManager dataGatherManager) {
        // 文件路径
        String filePath = ConfigReader.getFilePath1();
        Vector<Author> authors = new Vector<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {

                Author author = initAuthor(dataGatherManager, line, authors, reader);


                initPaperandJournal(reader, dataGatherManager, line, author);



            }
            dataGatherManager.initMatrixOrder();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Journal item : dataGatherManager.journals) {
            item.setIF(dataGatherManager);
        }
    }


    public static void tempInit(DataGatherManager dataGatherManager, String filePath) {
        Vector<Author> authors = new Vector<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            Integer i=0;
            while ((line = reader.readLine()) != null) {

                Author author = initAuthor(dataGatherManager, line, authors, reader);

                i+=1;
                System.out.println(i);

                initPaperandJournal(reader, dataGatherManager, line, author);

            }
            dataGatherManager.initMatrixOrder();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Journal item : dataGatherManager.journals) {
            item.setIF(dataGatherManager);
        }
    }




}


