package com.github.ryan6073.Seriously.BasicInfo;

import com.github.ryan6073.Seriously.TimeInfo;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInput {
    public static void initJournalToIF(DataGatherManager dataGatherManager){
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
                //System.out.println(String.join(", ", columns));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static Author initAuthor(DataGatherManager dataGatherManager, String line, Vector<Author> authors) {
        // 解析作者信息
        // 正则表达式，匹配数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(line);

        // 找到第一个数字的位置（作者编号）
        int firstNumberIndex = 0;
        if (matcher.find()) {
            firstNumberIndex = matcher.start();
        }
        // 找到最后一个数字的位置（论文数）
        int lastNumberIndex = 0;
        while (matcher.find()) {
            lastNumberIndex = matcher.start();
        }
        // 根据数字的位置拆分字符串
        String authorName = line.substring(0, firstNumberIndex).trim();
        // System.out.println(authorName);
        String orcid = line.substring(firstNumberIndex, line.indexOf(" ", firstNumberIndex)).trim();
        String insCount = line.substring(line.indexOf(" ", firstNumberIndex) + 1, line.indexOf(" ", line.indexOf(" ", firstNumberIndex) + 1)).trim();
//                int insCountInt = Integer.parseInt(insCount);
        int insCountInt = Integer.parseInt(insCount);
        String institutionNames = line.substring(line.indexOf(" ", line.indexOf(" ", firstNumberIndex) + 1) + 1, lastNumberIndex).trim();
        String[] institutions = institutionNames.split(";;");
        Author author;
        if(insCountInt==0) {
            author = new Author(authorName, orcid, "");
        } else if(insCountInt==1) {
            String authorInstitution = institutions[0];
            author = new Author(authorName, orcid, authorInstitution);
            Institution institution = new Institution();
            institution.institutionName = authorInstitution;
            institution.institutionAuthors.add(orcid);
            dataGatherManager.addInstitution(institution);
        } else{
            Vector<String> authorInstitution = new Vector<>(Arrays.asList(institutions));
            author = new Author(authorName, orcid, authorInstitution);
            for (String institutionName : authorInstitution) {
                Institution institution = new Institution();
                institution.institutionName = institutionName;
                institution.institutionAuthors.add(orcid);
                dataGatherManager.addInstitution(institution);
            }
        }
        authors.add(author);
        dataGatherManager.addDicOA(author);

        dataGatherManager.authorNum += 1;//如果分批读取再做修改，改成+=即可    我觉得直接改成+1即可，这样可以直接处理分批读取问题
        return author;
    }
    //initPaperandJournal需要修改：
    //1.Paper中新增publishedMonth变量，需要在文件中初始化，month顶替了文件中原先status的位置，status指的是文章的出版状态，文件曾经用1 2 3 4指代published revised等四种出版状态
    //2.如你所见，Paper中的出版状态变量已经删去，CitingStatusType类应该删除，现在Paper中关于时间的变量只有publishedYear和publishedMonth，需要将函数initPaperandJournal中
    //跟xxxYear和CitingStatusType有关的代码进行删除调整，确保达到修改完代码之后删除CitingStatusType类此文件不会报错的程度
    public static void initPaperandJournal(BufferedReader reader, DataGatherManager dataGatherManager, String line, Author author) throws IOException {
        Vector<Paper> author_papers = new Vector<>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(line);
        // 找到第一个数字的位置（作者编号）
        int firstNumberIndex = 0;
        if (matcher.find()) {
            firstNumberIndex = matcher.start();
        }
        // 找到最后一个数字的位置（论文数）
        int lastNumberIndex = 0;
        while (matcher.find()) {
            lastNumberIndex = matcher.start();
        }
        String paperCount = line.substring(lastNumberIndex).trim().split(" ")[0];
        int paperCountInt = Integer.parseInt(paperCount);
        String orcid = line.substring(firstNumberIndex, line.indexOf(" ", firstNumberIndex)).trim();
        // 解析每篇论文信息
        for (int i = 0; i < paperCountInt; i++) {
            String CountLine = reader.readLine();
            String paperLine = reader.readLine();
            // 正则表达式匹配DOI，它以数字开始和结束
            Pattern doiPattern = Pattern.compile("\\b\\d+\\.\\d+/\\S+\\.\\d+\\.\\d+\\b");
            Matcher doiMatcher = doiPattern.matcher(paperLine);
            int citedPaperCount = Integer.parseInt(CountLine);
            // 找到DOI的位置
            String paperDoi = "";
            if (doiMatcher.find()) {
                paperDoi = doiMatcher.group();
            }
            // 使用DOI分割数据行，这样我们可以单独获取论文名和剩余部分
            String[] parts = paperLine.split(paperDoi);

            // 论文名在第一部分，去除首尾空格
            String paperName = parts[0].trim();

            // 期刊名称、年份和月份在第二部分
            // 假设年份和月份总是在字符串的末尾，并且是连续的数字
            Pattern yearMonthPattern = Pattern.compile("\\d{4} \\d{1,2}$");
            Matcher yearMonthMatcher = yearMonthPattern.matcher(parts[1]);

            // 找到年份和月份
            String year = "";
            String month = "";
            if (yearMonthMatcher.find()) {
                String[] yearMonth = yearMonthMatcher.group().split(" ");
                year = yearMonth[0];
                month = yearMonth[1];
            }

            // 获取期刊名，它位于DOI和年份之间
            String paperJournal = parts[1].substring(0, parts[1].length() - year.length() - month.length() - 2).trim(); // 减去2个空格

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
            paper.authorIDList.add(orcid);//考虑ID和名字都各有优劣
            journal.setJournalName(paperJournal);
            journal.journalPapers.add(paper.doi);

            // 判断paper是否已经存在于datagathermanager的papers列表中，如果存在则直接添加作者，否则创建新的paper对象
            if(dataGatherManager.paperFind(paperDoi)){

                paper = dataGatherManager.paperGet(paperDoi);

                paper.authorIDList.add(orcid);
            }

            else{
                dataGatherManager.addPaper(paper);
            }
            dataGatherManager.addDicDP(paper);
            dataGatherManager.addPaper(paper);
            if(dataGatherManager.journals.contains(journal)){
                journal = dataGatherManager.journals.get(dataGatherManager.journals.indexOf(journal));
                journal.journalPapers.add(paper.doi);
            }
            else{
                dataGatherManager.addJournal(journal);
            }
            author_papers.add(paper);
            reader.readLine();//读取空行
        }
        dataGatherManager.addDicDA(author, author_papers);
    }

//    public static void rankJournal(DataGatherManager dataGatherManager){
//        //  对期刊进行等级划分
//        for (Journal item : dataGatherManager.journals) {
//            item.setIF(0.0);//让其随机生成一个IF测试用
//        }
//        Collections.sort(dataGatherManager.journals);// 根据IF进行排序
//        for (int i = 0; i < dataGatherManager.journals.size(); ++i) {
//            if (i < dataGatherManager.journals.size() / 4) dataGatherManager.journals.get(i).setRank(1);
//            else if (i < dataGatherManager.journals.size() / 2) dataGatherManager.journals.get(i).setRank(2);
//            else if (i < dataGatherManager.journals.size() * 3 / 4) dataGatherManager.journals.get(i).setRank(3);
//            else dataGatherManager.journals.get(i).setRank(4);
//
////            System.out.println(dataGatherManager.journals.get(i).getIF());
////            System.out.println(dataGatherManager.journals.get(i).getRank());
//        }
//    }

    public static void initDicTimeInfoDoi(DataGatherManager dataGatherManager){
        dataGatherManager.dicTimeInfoDoi = new HashMap<>();
        for(Map.Entry<String,Paper> entry:dataGatherManager.dicDoiPaper.entrySet()){
            //提取论文的时间信息
            int year = entry.getValue().publishedYear;
            int month = entry.getValue().publishedMonth;
            TimeInfo timeInfo = new TimeInfo(year,month);
            String doi = entry.getKey();
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
    }

    public static void init(DataGatherManager dataGatherManager) {
        // 文件路径
        String filePath = ConfigReader.getFilePath1();
        Vector<Author> authors = new Vector<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                Author author = initAuthor(dataGatherManager, line, authors);
                initPaperandJournal(reader, dataGatherManager, line, author);
                initDicTimeInfoDoi(dataGatherManager);
                dataGatherManager.initMatrixOrder();
//                //规模控制
//                if(authors.size()>30)
//                    break;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Journal item:dataGatherManager.journals){
            item.setIF(dataGatherManager);
        }
    }
}