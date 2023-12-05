package com.github.ryan6073.Seriously.BasicInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    public static String getFilePath1() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:\\Users\\21333\\Desktop\\mywork\\Java_work\\src\\com\\github\\ryan6073\\Seriously\\config.properties")) {
            prop.load(input);
            return prop.getProperty("filePath1");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static String getFilePath2() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:\\Users\\21333\\Desktop\\mywork\\Java_work\\src\\com\\github\\ryan6073\\Seriously\\config.properties")) {
            prop.load(input);
            return prop.getProperty("filePath2");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
