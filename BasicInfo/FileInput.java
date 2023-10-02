package com.github.ryan6073.Seriously.BasicInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
public class FileInput {
    public static void main(String[] args) {
        // 文件路径
        String filePath = "test.txt";
        Vector<Author> authors = new Vector<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
                // 解析作者信息
                String[] authorInfo = line.split("\t");
                String authorName = authorInfo[0];
                String orcid = authorInfo[1];
                int insCount = Integer.parseInt(authorInfo[2]);//机构数量
                Author author;
                if(insCount==0) {
                    author = new Author(authorName, orcid, "");
                } else if(insCount==1) {
                    String authorInstitution = authorInfo[3];
                    author = new Author(authorName, orcid, authorInstitution);
                    Institution institution = new Institution();
                    institution.institutionName = authorInstitution;
                    institution.institutionAuthors.add(orcid);
                    DataGatherManager.getInstance().addInstitution(institution);
                } else{
                    Vector<String> authorInstitution = new Vector<>(Arrays.asList(authorInfo).subList(3, insCount + 3));
                    author = new Author(authorName, orcid, authorInstitution);
                    for (String institutionName : authorInstitution) {
                        Institution institution = new Institution();
                        institution.institutionName = institutionName;
                        institution.institutionAuthors.add(orcid);
                        DataGatherManager.getInstance().addInstitution(institution);
                    }
                }
                authors.add(author);
                DataGatherManager.getInstance().addDicOA(author);

                int paperCount = Integer.parseInt(authorInfo[insCount + 3]);//论文数量

                Vector<Paper> papers = new Vector<>();

                // 解析每篇论文信息
                for (int i = 0; i < paperCount; i++) {
                    String paperLine = reader.readLine();
                    String[] paperInfo = paperLine.split(" ");
                    int citedPaperCount = Integer.parseInt(paperInfo[0]);
                    String paperName = paperInfo[1];
                    String paperDoi = paperInfo[2];
                    String paperJournal = paperInfo[3];
                    int paperYear = Integer.parseInt(paperInfo[4]);//published year
                    int paperStatus = Integer.parseInt(paperInfo[5]);//其实有点矛盾，不是published，其实别的没办法知道，现有published才有别的三个出现的可能
                    Vector<String> citedPapers = new Vector<>();
                    for (int j = 0; j < citedPaperCount; j++) {
                        String citedPaperDoi = reader.readLine();
                        citedPapers.add(citedPaperDoi);
                    }

                    // 创建Paper对象并添加到papers列表
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

                    DataGatherManager.getInstance().addDicDP(paper);
                    DataGatherManager.getInstance().addToPaper(paper);
                    DataGatherManager.getInstance().authorNum = authors.size();//如果分批读取再做修改，改成+=即可
                    DataGatherManager.getInstance().addJournal(journal);
                    papers.add(paper);
                }
                DataGatherManager.getInstance().addDicDA(author, papers);


                //journal和institution类的初始化先不写
            }
            reader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}