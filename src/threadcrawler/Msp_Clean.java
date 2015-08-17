package threadcrawler;

import automation.InvokeBat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xianyu.hxy on 2015/8/12.
 */
public class Msp_Clean {
    public static String path="e:"+ File.separator+"MspConfig.java";
    public static final String BASE_FOLDER = "com" + File.separator + "alipay" + File.separator + "sdk" + File.separator + "cons";
    public static final String BASE_FOLDER1 = "com" + File.separator + "alipay" + File.separator + "sdk" + File.separator + "data";
    public static final String BASE_FOLDER_pd = "com" + File.separator + "alipay" + File.separator + "sdk";
    public static ArrayList<String> pacPath=new ArrayList<String>();
    public static boolean HasMSP=false;
    public static boolean HasSDK=false;
    public static void main(String[] args){
        String msp= getFile("E:\\msp\\海豹村_com.global.hbc_15-enjarify");
        System.out.println("msp:"+msp);
    }
    public static String getMsp(String path){
        Pattern p = Pattern.compile("[1][0][//.][0-9][//.][0-9]|[9][//.][0-9][//.][0-9]");
        String txt=null;
        String result = null;
        txt = readTxt(path);
            Matcher m = p.matcher(txt);
            if (m.find()) {
                result = m.group(0);
            }
        return result;
    }
    public static String getMsp_d(String path){
        Pattern p = Pattern.compile("[1][0][//.][0-9][//.][0-9]|[9][//.][0-9][//.][0-9]");
        String txt=null;
        String result = null;
        txt = readTxt_d(path);
        Matcher m = p.matcher(txt);
        if (m.find()) {
            result = m.group(0);
        }
        return result;
    }
    public static String getFile(String path){
        HasMSP=false;
        pacPath.clear();
        getFileList(path);
        //判断有无sdk包
        for(String s:pacPath){
            if(s.contains(BASE_FOLDER_pd)){
                HasMSP=true;
            }
        }
        if(HasMSP==false){
            return null;
        }
        pacPath.clear();
        getFileList(path+File.separator+BASE_FOLDER_pd);
        if(pacPath.size()==0)return null;

        for(String s:pacPath){
          //  System.out.println(s);
            if(s.contains(BASE_FOLDER)){
                String pah=s+File.separator+"GlobalConstants.class";
                File mspFile=new File(pah);
                if(mspFile.exists()){
                    System.out.println(s+"\n"+pah);
                   javapFile(s, pah);
                    String msp= getMsp(pah);
                    if(msp!=null){
                    return msp;
                    }
                }
                File[] listFiles=new File(s).listFiles();
                for(File f:listFiles) {
                    if(f.getAbsolutePath().endsWith("class")) {
                       javapFile(s, f.getAbsolutePath());
                        String msp = getMsp(s + File.separator + "msp.txt");
                        if (msp != null) {
                            return msp;
                        }
                    }
                }

            }
        }
        for(String s:pacPath){
            //  System.out.println(s);
            if(s.contains(BASE_FOLDER1)){
                String pah=s+File.separator+"MspConfig.class";
                File mspFile=new File(pah);
                if(mspFile.exists()){
                    System.out.println(s+"\n"+pah);
                    javapFile(s, pah);
                    String msp= getMsp(pah);
                    if(msp!=null) {
                        return msp;
                    }
                }
                File[] listFiles=new File(s).listFiles();
                for(File f:listFiles) {
                    if(f.getAbsolutePath().endsWith("class")) {
                       // javapFile(s, f.getAbsolutePath());
                        String msp = getMsp_d(f.getAbsolutePath());
                        if (msp != null) {
                            return msp;
                        }
                    }
                }

            }
        }
        for(String s:pacPath){
            File file=new File(s);
            if(file.exists()&&file.isDirectory()){
                File[] files=file.listFiles();
                if(files.length==6){
                  for(File f:files){
                      String msp = getMsp_d(f.getAbsolutePath());
                      if (msp != null) {
                          return msp;
                      }
                  }
                }
            }
        }


        return "Not Found";
    }
    public static Boolean hasSdk(String path){
        HasSDK=false;
        pacPath.clear();
        getFileList(path);
        for(String s:pacPath){
            if(s.contains(BASE_FOLDER)){
                HasMSP=true;
            }
        }
        return false;
    }
    public static void javapFile(String FoldPath,String FilePath){
        InvokeBat invokeBat=new InvokeBat();
        String out = FoldPath + File.separator + "msp.txt";
        File outFile=new File(out);
        if(outFile.exists()){
            outFile.delete();
        }
        String cmdStr = "cmd /c javap -constants " + FilePath + " > " + out;
        invokeBat.runbat(cmdStr);
    }
    public static void getFileList(String directory) {
        File f = new File(directory);
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
            } else {
                pacPath.add(files[i].getAbsolutePath());
                getFileList(files[i].getAbsolutePath());
            }
        }
    }

    public static String readTxt(String path) {
        File f = new File(path);
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = null;
        if (f.exists() && f.isFile()) {
            try {
                //System.out.println("以行为单位读取文件内容，一次读一整行：");
                reader = new BufferedReader(new FileReader(f));
                String tempString = null;
                int line = 1;
                // 一次读入一行，直到读入null为文件结束
                while ((tempString = reader.readLine()) != null) {
                   // System.out.println("line " + line + ": " + tempString);
                    buffer.append(tempString + "\n");
                    line++;
                    //为什么会重复读入
                    if(line>40)break;
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }
        String s = buffer.toString();
        return s;
    }
    public static String readTxt_d(String path) {
        File f = new File(path);
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = null;
        if (f.exists() && f.isFile()) {
            try {
                //System.out.println("以行为单位读取文件内容，一次读一整行：");
                reader = new BufferedReader(new FileReader(f));
                String tempString = null;
                int line = 1;
                // 一次读入一行，直到读入null为文件结束
                while ((tempString = reader.readLine()) != null) {
                    // System.out.println("line " + line + ": " + tempString);
                    buffer.append(tempString + "\n");
                    line++;
                    //为什么会重复读入
                    if(line>300)break;
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }
        String s = buffer.toString();
        return s;
    }
}
