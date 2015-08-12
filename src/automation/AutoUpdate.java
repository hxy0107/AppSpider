package automation;

import download.DownloadUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by xianyu.hxy on 2015/7/28.
 */
public class AutoUpdate {
    public static String RequestPac = "com.sankuai.meituan";
    public static String sql_version = "";
    public final static String FileDirBase="e:"+ File.separator+"temp2";
    public static ArrayList<String> pacPath;
    public static String decode_app="";
    public static String has_sdk="";
    public static final String BASE_FOLDER = "com" + File.separator + "alipay" + File.separator + "sdk" + File.separator + "cons";
    public static boolean isContain = false;

    public static void main(String[] args) {
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            System.out.println("Success loading Mysql Driver!");
            Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1");
            System.out.println("Success connect Mysql server!");
            Statement stmt = connect.createStatement();
            String sql = "SELECT app_version FROM app_info.`msp_table_copy_7.27_copy` WHERE package_name='" + RequestPac + "'"+"order by id desc";
            ResultSet result = stmt.executeQuery(sql);


            int j=1;
            while (result.next()) {

                String max;
                max = result.getString(1);
                j++;
                if(sql_version.compareTo(max)<0){
                    sql_version=max;
                }
            }
            System.out.println("sql_version: " + sql_version);

            String url_list = "http://apps.wandoujia.com/apps/" + RequestPac + "/versions?pos=w/popup";
            HtmlCleaner cleaner = new HtmlCleaner();
            URL url = new URL(url_list);
            TagNode node = cleaner.clean(url);
            Object[] tags_s = node.evaluateXPath("/body/div//div[@class='version-block']/div[position()<2]");
            Object[] tagIcon = node.evaluateXPath("/body/div//img");
            String app_icon = ((TagNode) tagIcon[0]).getAttributeByName("src");
            System.out.println(app_icon);
            int i = 1;
            for (Object tag_s : tags_s) {
                // System.out.println(((TagNode)tagSize).getText()+"");
                Object[] tagVersion = node.evaluateXPath("/body/div//div[@class='version-block']/div[" + i + "]//i[@itemprop='softwareVersion']");
                String app_verison = ((TagNode) tagVersion[0]).getText() + "";
                System.out.println(((TagNode) tagVersion[0]).getText() + "");

                Object[] tagVersionCode = node.evaluateXPath("/body/div//div[@class='version-block']/div[" + i + "]//span[@class='version-code']");
                String app_versioncode = ((TagNode) tagVersionCode[0]).getText() + "";
                String vc=app_versioncode.substring(app_versioncode.indexOf(":")+1,app_versioncode.lastIndexOf(")")).trim();
                System.out.println(((TagNode) tagVersionCode[0]).getText() + "");
                System.out.println("vc: "+vc);
                Object[] tagFileSize = node.evaluateXPath("/body/div//div[@class='version-block']/div[" + i + "]//span[@class='apk-size']");
                String app_size = ((TagNode) tagFileSize[0]).getText() + "";
                System.out.println(((TagNode) tagFileSize[0]).getText() + "");
                Object[] tagDownload = node.evaluateXPath("/body/div//div[@class='version-block']/div[" + i + "]//a[@download]");
                String app_url1 = ((TagNode) tagDownload[0]).getAttributeByName("href");
                String app_url = app_url1.replaceAll("&amp;", "&");
                System.out.println("下载地址: " + app_url + "\n");
                String app_name = ((TagNode) tagDownload[0]).getAttributeByName("download");
                i++;

                if(app_verison.equals(sql_version)){
                    System.out.println("This Version is already the Newest");
                   // break;
                }

                File app=new File(FileDirBase+File.separator+ app_name + "_" + RequestPac + "_" + vc + ".apk");
                File jarFile=new File(FileDirBase+File.separator+app_name + "_" + RequestPac + "_" + vc+"-enjarify"+".jar");
                File file=new File(FileDirBase+File.separator+ app_name + "_" + RequestPac + "_" + vc+"-enjarify");
                DownloadUtils.download(app_url, app_name + "_" + RequestPac + "_" + vc + ".apk", FileDirBase, 1);


                Thread.sleep(10000);
                while(app.length()<100){
                    Thread.sleep(10000);
                }
                //反编译
                InvokeBat invokeBat=new InvokeBat();
                if(app.exists()&&app.isFile()&&app.length()>5000){
                    String Path=app.getAbsolutePath();
                    String cmdStr="cmd /c enjarify "+Path;
                    long start=System.currentTimeMillis();
                    invokeBat.runbat(cmdStr);
                    long end=System.currentTimeMillis();
                    System.out.println("finish:"+(end-start)/1000+" s");
                }
                if(jarFile.length()<10000){decode_app="false";
                    String sql4="UPDATE app_info.`pack_only_copy_7.22` \n" +
                            "SET decode_app='false' "+
                            "WHERE package_name='"+RequestPac+"'\n"
                            ;
                    stmt.executeUpdate(sql4);
                }else{decode_app="true";};
                //解压
                if(jarFile.exists()&&jarFile.isFile()&&jarFile.length()>10000){
                    String filePath=jarFile.getAbsolutePath();
                    String folerName=filePath.substring(0, filePath.lastIndexOf("."));
                    System.out.println("filePath:" + folerName);
                    long start=System.currentTimeMillis();
                    ZipUtil.unzip(filePath,folerName);
                    long end=System.currentTimeMillis();
                    System.out.println("untar time:"+(end-start)/1000+" s");
                }

                pacPath=new ArrayList<String>();
                if (file.exists() && file.isDirectory()) {
                    //进入各个app解压文件夹根目
                    String path = file.getAbsolutePath();

                    pacPath.clear();
                    getFileList(path);
                    for (String s : pacPath) {
                        boolean b = s.contains(BASE_FOLDER);
                        if (b) {
                            has_sdk="true";
                            System.out.println(pacPath);
                            isContain = true;
                            String sql5="UPDATE app_info.`pack_only_copy_7.22` \n" +
                                    "SET decode_app='true',has_sdk='true' "+
                                    "WHERE package_name='"+RequestPac+"'\n"
                                    ;
                            stmt.executeUpdate(sql5);

                            // System.out.println(s);
                            String tag[] = s.trim().split("\\\\");
                            String tags[] = tag[2].split("_");
                            String packName = tags[1];
                            String version_enjar=tags[2];
                            String versionCode=version_enjar.substring(0,version_enjar.indexOf("-"));
                            System.out.println(packName);
                            System.out.println(versionCode);

                            String in=s+File.separator+"GlobalConstants.class";
                            String out = s + File.separator + "GlobalConstantsHXY.txt";
                            System.out.println("***in :"+in);
                            System.out.println("***in :" + out);
                            String cmdStr = "cmd /c javap -constants " + in + " > " + out;
                            invokeBat.runbat(cmdStr);

                            File outFile=new File(out);

                            //读文件
                            String[] lines = readTXT(out);
                            for (String line : lines) {
                                System.out.println(line);
                                if (line.trim().startsWith(test.G)) {
                                    test.msp_version = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                                    break;
                                }
                            }
                            if(test.msp_version==null||test.msp_version.length()>15){test.msp_version="not found";}
                            System.out.println("msp_version: " + test.msp_version);
                            String sql3="UPDATE app_info.`pack_only_copy_7.22` \n" +
                                    "SET sdk_version='"+test.msp_version.trim()+"'," +"decode_app='true',has_sdk='true' "+
                                    "WHERE package_name='"+packName+"'\n" +
                                    "AND app_version='"+versionCode+"'";
                            stmt.executeUpdate(sql3);
                            test.msp_version=null;
                        }
                    }
                }
                if(isContain){
                    isContain=false;
                }else {
                    String sql6="UPDATE app_info.`pack_only_copy_7.22` \n" +
                            "SET decode_app='true',has_sdk='false' "+
                            "WHERE package_name='"+RequestPac+"'\n"
                            ;
                    stmt.executeUpdate(sql6);
                }






            }

            has_sdk="";
            decode_app="";
            //  pacPath.clear();
            System.out.println("This app finish deteck msp_version");


            //***写入数据库 明天写***
                    /*
                    INSERT INTO app_info.`msp_test`(package_name,app_name,app_version,app_versioncode,app_size,msp_version,app_url,app_icon)
                    VALUE('','','','','','','');
                     */
                //     String sql_in="INSERT INTO app_info.`msp_table_copy`(package_name,app_name,app_version,app_versioncode,app_size,msp_version,app_url,app_icon)\n" +
                //             "VALUE('"+pac+"','"+app_name+"','"+app_verison+"','"+app_versioncode+"','"+app_size+"','"+msp_version+"','"+app_url+"','"+app_icon+"');";
                //       int result1=stmt.executeUpdate(sql_in);

            } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (XPatherException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
    }



    public static void getFileList(String directory) {
        File f = new File(directory);
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //  System.out.println("文件：" + files[i]);
            } else {
                //System.out.println("目录：" + files[i].getAbsolutePath());
                pacPath.add(files[i].getAbsolutePath());
                getFileList(files[i].getAbsolutePath());
            }
        }
    }

    public static String[] readTXT(String path) {
        File f = new File(path);
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        if (f.exists() && f.isFile()) {
            try {
                //System.out.println("以行为单位读取文件内容，一次读一整行：");
                reader = new BufferedReader(new FileReader(f));
                String tempString = null;
                int line = 1;
                // 一次读入一行，直到读入null为文件结束
                while ((tempString = reader.readLine()) != null) {
                    // 显示行号
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
        String[] s = buffer.toString().split("\n");
        return s;
    }
}