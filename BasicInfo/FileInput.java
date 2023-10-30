package com.github.ryan6073.Seriously.BasicInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Vector;
public class FileInput {
    public static void initJournalToIF(DataGatherManager dataGatherManager){
        // 创建 reader
        try (BufferedReader br = Files.newBufferedReader(Paths.get("SuPZ_JCR_JournalResults_10_2023.csv"))) {
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
    public static Author initAuthor(DataGatherManager dataGatherManager, String firstLine, Vector<Author> authors) {
        // 解析作者信息
        //两个空格隔开从而将初始化信息分为三段
        String[] authorInfo = firstLine.split("  ");
        String authorName = authorInfo[0];
        String orcid = authorInfo[1];
        //将第三段信息以一个空格隔开
        String[] insInfo = authorInfo[2].split(" ");
        int insCount = Integer.parseInt(insInfo[0]);//机构数量
        Author author;
        if (insCount == 0) {
            author = new Author(authorName, orcid, "");
        } else if (insCount == 1) {
            String authorInstitution = insInfo[1];
            author = new Author(authorName, orcid, authorInstitution);
            Institution institution = new Institution();
            institution.institutionName = authorInstitution;
            institution.institutionAuthors.add(orcid);
            dataGatherManager.addInstitution(institution);
        } else {
            Vector<String> authorInstitution = new Vector<>(Arrays.asList(insInfo).subList(1, insCount + 1));
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
    public static void initPaperandJournal(BufferedReader reader, DataGatherManager dataGatherManager, String firstLine, Author author) throws IOException {
        String[] authorInfo = firstLine.split("  ");
        String authorName = authorInfo[0];
        String orcid = authorInfo[1];
        //将第三段信息以一个空格隔开
        String[] insInfo = authorInfo[2].split(" ");
        int insCount = Integer.parseInt(insInfo[0]);//机构数量
        int paperCount = Integer.parseInt(insInfo[insCount + 1]);//论文数量
        Vector<Paper> author_papers = new Vector<>();
        for (int i = 0; i < paperCount; i++) {

            // 解析每篇论文信息
            String CountLine = reader.readLine();
            String paperLine = reader.readLine();
            String[] paperInfo = paperLine.split(" ");
            int citedPaperCount = Integer.parseInt(CountLine);
            String paperName = paperInfo[0];
            String paperDoi = paperInfo[1];
            String paperJournal = paperInfo[2];
            int paperYear = Integer.parseInt(paperInfo[3]);//published year
            int paperStatus = Integer.parseInt(paperInfo[4]);//其实有点矛盾，不是published，其实别的没办法知道，现有published才有别的三个出现的可能
            Vector<String> citedPapers = new Vector<>();
            for (int j = 0; j < citedPaperCount; j++) {
                String citedPaperDoi = reader.readLine();
                citedPapers.add(citedPaperDoi);
            }

            //更新论文信息
            Paper paper = new Paper();
            paper.paperName = paperName;
            paper.doi = paperDoi;
            paper.journals.add(paperJournal);
            paper.paperStatus = CitingStatusTypes.choiceTypes(paperStatus);
            if (paper.paperStatus != null) {
                paper.setYear(paperYear, paper.paperStatus);
            }
            paper.citingList.addAll(citedPapers);
            paper.authorIDList.add(orcid);//考虑ID和名字都各有优劣
            // 判断paper是否已经存在于datagathermanager的papers列表中，如果存在则直接添加作者，否则创建新的paper对象
            if (dataGatherManager.paperFind(paperDoi)) {
                paper = dataGatherManager.paperGet(paperDoi);
                paper.authorIDList.add(orcid);
            } else {
                dataGatherManager.addPaper(paper);
            }
            dataGatherManager.addDicDP(paper);

            //更新论文所处的期刊信息
            Journal journal = new Journal();
            journal.journalName = paperJournal;
            journal.journalPapers.add(paper.doi);
            if (dataGatherManager.journals.contains(journal)) {
                journal = dataGatherManager.journals.get(dataGatherManager.journals.indexOf(journal));
                journal.journalPapers.add(paper.doi);
            } else {
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

    public static void init(DataGatherManager dataGatherManager) {
        // 文件路径
        String filePath = "D:\\Gitcode\\Seriously\\test.txt";
        Vector<Author> authors = new Vector<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                Author author = initAuthor(dataGatherManager, line, authors);
                initPaperandJournal(reader, dataGatherManager, line, author);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Journal item:dataGatherManager.journals){
            item.setIF(0.0);//让其随机生成一个IF测试用
        }

        KMeans.kMeans(dataGatherManager);//更新rank
    }
}