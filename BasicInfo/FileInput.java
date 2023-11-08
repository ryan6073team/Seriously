package com.github.ryan6073.Seriously.BasicInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class FileInput {
    public static void init(DataGatherManager dataGatherManager) {
        // 文件路径
        String filePath = "C:\\Users\\21333\\Desktop\\mywork\\Java_work\\src\\com\\github\\ryan6073\\Seriously\\author_net.txt";
        Vector<Author> authors = new Vector<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
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
                String orcid = line.substring(firstNumberIndex, line.indexOf(" ", firstNumberIndex)).trim();
                String insCount = line.substring(line.indexOf(" ", firstNumberIndex) + 1, line.indexOf(" ", line.indexOf(" ", firstNumberIndex) + 1)).trim();
//                int insCountInt = Integer.parseInt(insCount);
                int insCountInt = 0;
                try {
                    insCountInt = Integer.parseInt(insCount);
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing insCount as int on line: " + line);
                    continue; // 跳过当前迭代，继续下一行
                }

                String institutionNames = line.substring(line.indexOf(" ", line.indexOf(" ", firstNumberIndex) + 1) + 1, lastNumberIndex).trim();
                String[] institutions = institutionNames.split(";;");
                String paperCount = line.substring(lastNumberIndex).trim().split(" ")[0];
                int paperCountInt = Integer.parseInt(paperCount);
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
                Vector<Paper> author_papers = new Vector<>();


                // 解析每篇论文信息
                for (int i = 0; i < paperCountInt; i++) {
                    String CountLine = reader.readLine();
                    String paperLine = reader.readLine();
                    // 正则表达式匹配DOI，它以数字开始和结束
                    Pattern doiPattern = Pattern.compile("\\b\\d+\\.\\d+/\\S+\\.\\d+\\.\\d+\\b");
                    Matcher doiMatcher = doiPattern.matcher(paperLine);
                    String[] paperInfo = paperLine.split(" ");
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
                    paper.journals.add(paperJournal);
                    paper.setYear(Integer.parseInt(year), CitingStatusTypes.PUBLISHED);
                    paper.setMonth(Integer.parseInt(month));
//                    paper.paperStatus = CitingStatusTypes.choiceTypes(paperStatus);
//                    if (paper.paperStatus != null) {
//                        paper.setYear(paperYear, paper.paperStatus);
//                    }
                    paper.paperStatus = CitingStatusTypes.PUBLISHED;
                    paper.citingList.addAll(citedPapers);
                    paper.authorIDList.add(orcid);//考虑ID和名字都各有优劣
                    journal.journalName = paperJournal;
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
                    dataGatherManager.addToPaper(paper);
                    dataGatherManager.authorNum = authors.size();//如果分批读取再做修改，改成+=即可
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
                reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}