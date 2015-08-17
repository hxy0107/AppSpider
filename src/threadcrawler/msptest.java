package threadcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xianyu.hxy on 2015/8/14.
 */
public class msptest {
    public static void main(String[] args){
        String s="9.0.0dfgsdfg9.1.1";

       // String result=getMsp("E:\\msp\\一米鲜_com.yimixian.app_168-enjarify\\com\\alipay\\sdk\\data\\b.class");
        String result=getMsp1(s);
        System.out.println(result);

    }
    public static String getMsp1(String path){
        Pattern p = Pattern.compile("[1][0][//.][0-9][//.][0-9]|[9][//.][0-9][//.][0-9]");
        String result = null;
        Matcher m = p.matcher(path);
        if (m.find()) {
            result = m.group(0);
        }
        return result;
    }
    public static String getMsp(String path){
        Pattern p = Pattern.compile("[9][//.][0-9][//.][0-9]");
        String txt=null;
        String result = null;
        txt = readTxt(path);
        Matcher m = p.matcher(txt);
        if (m.find()) {
            result = m.group(0);
        }
        return result;
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
}
