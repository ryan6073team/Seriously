package com.github.ryan6073.Seriously.src.BasicInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
public class FileInput {
    public static void init(DataGatherManager dataGatherManager) {
        // 文件路径
        String filePath = "D:\\Gitcode\\Seriously\\test.txt";
        Vector<Author> authors = new Vector<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
                // 解析作者信息
                String[] authorInfo = line.split("  ");
                String authorName = authorInfo[0];
                String orcid = authorInfo[1];
                String[] insInfo = authorInfo[2].split(" ");
                int insCount = Integer.parseInt(insInfo[0]);//机构数量
                Author author;
                if(insCount==0) {
                    author = new Author(authorName, orcid, "");
                } else if(insCount==1) {
                    String authorInstitution = insInfo[1];
                    author = new Author(authorName, orcid, authorInstitution);
                    Institution institution = new Institution();
                    institution.institutionName = authorInstitution;
                    institution.institutionAuthors.add(orcid);
                    dataGatherManager.addInstitution(institution);
                } else{
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

                int paperCount = Integer.parseInt(insInfo[insCount + 1]);//论文数量

                Vector<Paper> author_papers = new Vector<>();

                // 解析每篇论文信息
                for (int i = 0; i < paperCount; i++) {
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

                    Paper paper = new Paper();
                    Journal journal = new Journal();

                    paper.paperName = paperName;
                    paper.doi = paperDoi;
                    paper.journals.add(paperJournal);
                    paper.paperStatus = CitingStatusTypes.choiceTypes(paperStatus);
                    if (paper.paperStatus != null) {
                        paper.setYear(paperYear, paper.paperStatus);
                    }
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