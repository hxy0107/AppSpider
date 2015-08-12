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
    public static ArrayList<String> pacPath=new ArrayList<String>();
    public static boolean HasMSP=false;
    public static void main(String[] args){
        String msp= getFile("E:\\msp\\������_com.global.hbc_15-enjarify");
        System.out.println("msp:"+msp);
    }
    public static String getMsp(String path){
        Pattern p = Pattern.compile("[9][//.][0-9][//.][0-9]");
        String txt=null;
        txt=readTxt(path);
        Matcher m = p.matcher(txt);
        String result=null;
        while(m.find()){
            result=m.group(0);
            break;
           // System.out.println(m.group(0));
        }
        return result;
    }
    public static String getFile(String path){
        HasMSP=false;
        pacPath.clear();
        getFileList(path);
        for(String s:pacPath){
            if(s.contains(BASE_FOLDER)){
                HasMSP=true;
            }
        }
        if(HasMSP==false){
            return null;
        }
        for(String s:pacPath){
          //  System.out.println(s);
            if(s.contains(BASE_FOLDER)){
                String pah=s+File.separator+"GlobalConstants.class";
                File mspFile=new File(pah);
                if(mspFile.exists()){
                    System.out.println(s+"\n"+pah);
                    javapFile(s, pah);
                    String msp= getMsp(pah);
                    return msp;
                }
                File[] listFiles=new File(s).listFiles();
                for(File f:listFiles) {
                    javapFile(s,f.getAbsolutePath());
                    String msp=getMsp(s+File.separator+"msp.txt");
                    if(msp!=null){
                        return msp;
                    }
                }

            }
        }
        return "Not Found";
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
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        if (f.exists() && f.isFile()) {
            try {
                //System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
                reader = new BufferedReader(new FileReader(f));
                String tempString = null;
                int line = 1;
                // һ�ζ���һ�У�ֱ������nullΪ�ļ�����
                while ((tempString = reader.readLine()) != null) {
                    // ��ʾ�к�
                    //System.out.println("line " + line + ": " + tempString);
                    buffer.append(tempString + "\n");
                    line++;
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