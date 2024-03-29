package com.github.ryan6073.Seriously.BasicInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    public static String getFilePath1() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:\\Users\\21333\\Desktop\\Seriously\\Seriously\\config.properties")) {
            prop.load(input);
            return prop.getProperty("filePath1");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static String getFilePath2() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:\\Users\\21333\\Desktop\\Seriously\\Seriously\\config.properties")) {
            prop.load(input);
            return prop.getProperty("filePath2");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static String getUser() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:\\Users\\21333\\Desktop\\Seriously\\Seriously\\config.properties")) {
            prop.load(input);
            return prop.getProperty("name");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static String getPassword() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:\\Users\\21333\\Desktop\\Seriously\\Seriously\\config.properties")) {
            prop.load(input);
            return prop.getProperty("psw");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
