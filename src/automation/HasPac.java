package automation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by xianyu.hxy on 2015/7/13.
 */
public class HasPac {
    public static final String FILE_BASE = "e:" + File.separator + "temp";
    public static final String PAC_NAME = "com" + File.separator + "alipay" + File.separator + "android" + File.separator + "app" + File.separator + "pay";
    public static final String PAC_NAME1 = "com" + File.separator + "alipay";

    public static final String BASE_FOLDER = "com" + File.separator + "alipay" + File.separator + "sdk" + File.separator + "cons";
    public static final String GLOBALCONSTANTS = BASE_FOLDER + File.separator + "GlobalConstants.class";
    public static final String OUTPUT_FILE = BASE_FOLDER + File.separator + "GlobalConstantsHXY.txt";
    public static ArrayList<String> appList;
    public static ArrayList<String> pacPath;
    public static boolean isContain = false;

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        InvokeBat invokeBat = new InvokeBat();
        pacPath = new ArrayList<String>();
        appList = new ArrayList<String>();


        Class.forName("org.gjt.mm.mysql.Driver");
        System.out.println("Success loading Mysql Driver!");
        Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1");
        System.out.println("Success connect Mysql server!");
        Statement stmt = connect.createStatement();
        //String sql=xx;
        // int result=stmt.executeUpdate(sql);


        File baseFile = new File(FILE_BASE);
        if (baseFile.exists() && baseFile.isDirectory()) {
            File[] aFiles = baseFile.listFiles();
            //aFile a,b,c...
            for (File aFile : aFiles) {
                File[] unjarFiles = aFile.listFiles();
                //unjarFile 支付宝,滴滴打车...
                for (File unjarFile : unjarFiles) {
                    //if(isContain)break;
                    if (unjarFile.exists() && unjarFile.isDirectory()) {
                        //进入各个app解压文件夹根目
                        String path = unjarFile.getAbsolutePath();
                        getFileList(path);
                        for (String s : pacPath) {
                            boolean b = s.contains(BASE_FOLDER);
                            if (b) {
                                //System.out.println(pacPath);
                                isContain = true;
                                if (!appList.contains(s)) {
                                    appList.add(s);
                                }

                                // System.out.println(s);
                                String tag[] = s.trim().split("\\\\");
                                String tags[] = tag[3].split("_");
                                String packName = tags[1];
                                System.out.println(packName);

                                String in=s+File.separator+"GlobalConstants.class";
                                String out = s + File.separator + "GlobalConstantsHXY.txt";
                                System.out.println("***in :"+in);
                                System.out.println("***in :"+out);
                                String cmdStr = "cmd /c javap -constants " + in + " > " + out;
                                invokeBat.runbat(cmdStr);



                                //读文件
                                String[] lines = readTXT(out);
                                for (String line : lines) {
                                    System.out.println(line);
                                    if (line.trim().startsWith(test.G)) {
                                        test.msp_version = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                                        break;
                                    }
                                }
                                if(test.msp_version==null||test.msp_version.length()>15){test.msp_version="999";}
                                System.out.println("msp_version: " + test.msp_version);
                                String sql="UPDATE app_info.`pack_only_copy_7.22` SET sdk_version='"+ test.msp_version.trim()+"'\n" +
                                        " WHERE package_name='"+packName+"'";
                                stmt.execute(sql);
                                test.msp_version=null;
                            }
                        }

                    }


                }
            }
        }
        for (String list : appList) {
            System.out.println("************" + list);
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
