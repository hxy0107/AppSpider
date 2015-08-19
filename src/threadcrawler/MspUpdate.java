package threadcrawler;

import automation.DownloadItem1;
import automation.InvokeBat;
import automation.ZipUtil;
import download.DownloadUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xianyu.hxy on 2015/8/12.
 */
public class MspUpdate {

    public static final String BASE_FOLDER = "com" + File.separator + "alipay" + File.separator + "sdk" + File.separator + "cons";
    public final static String FileDirBase="e:"+File.separator+"msp";
    public static  ArrayList<DownloadItem1> itemList;
    public static Boolean DownloadErr=false;
    public static void main(String[] args){
        HashMap<String,String> map=new HashMap<String, String>();
        itemList=new ArrayList<DownloadItem1>();
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            System.out.println("Success loading Mysql Driver!");
            Connection connect= DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1");
            System.out.println("Success connect Mysql server!");
            Statement stmt=connect.createStatement();
            String sql="SELECT app_name, app_versioncode FROM app_info.`msp_table_8.12_copy` ";
            ResultSet result=stmt.executeQuery(sql);
            while (result.next()){
                String n=result.getString(1);
                String v=result.getString(2);
                map.put(n, v);
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String n = entry.getKey();
                String v = entry.getValue();
                System.out.println(n + ":" + v);
                String RequestApp1 = URLEncoder.encode(n, "utf-8");
                String requestUrl = "http://apps.wandoujia.com/search?key=" + RequestApp1 + "&source=search";

                URL url = new URL(requestUrl);
                HtmlCleaner cleaner = new HtmlCleaner();
                TagNode node = cleaner.clean(url);
                itemList.clear();
                spiderUrl(cleaner, itemList, url);
                int i=1;
                if (itemList.size() > 0) {
                    System.out.println("***********************" + "正在下载更新第" + i + "个应用" + "***********************"+"\n\n");
                    i++;
                    DownloadItem1 downloadItems = itemList.get(0);
                    String package_name = downloadItems.getPn();
                    String app_name = downloadItems.getDownload_name();
                    String app_versioncode = downloadItems.getVc();
                    String app_url = downloadItems.getDownload_url();
                    String app_icon = downloadItems.getIcon();

                    String vc = MspUtils.QueryVcTable2(stmt, app_name);
                  //  System.out.println("应用名:" + app_name + ",数据库版本号:" + vc + ",最新版本号:" + app_versioncode);
                    if (vc!=null&&vc.equals(app_versioncode)) {
                        continue;
                    } else {
                        //没有记录 则插入
                        File baseFile = new File(FileDirBase);
                        if (!baseFile.exists()) {
                            baseFile.mkdir();
                        }
                        File app = new File(FileDirBase + File.separator + app_name + "_" + package_name + "_" + app_versioncode + ".apk");
                        File positionFile=new File(FileDirBase + File.separator + app_name + "_" + package_name + "_" + app_versioncode + ".apk.position");
                        File jarFile = new File(FileDirBase + File.separator + app_name + "_" + package_name + "_" + app_versioncode + "-enjarify" + ".jar");
                        File file = new File(FileDirBase + File.separator + app_name + "_" + package_name + "_" + app_versioncode + "-enjarify");

                        if(app.exists()&&app.length()<100000){
                            if(positionFile.exists()){positionFile.delete();}
                            app.delete();
                            if(jarFile.exists()){jarFile.delete();}
                        }
                        if (app.exists() && app.length() > 100000) {
                        } else {
                            DownloadUtils.download(app_url, app_name + "_" + package_name + "_" + app_versioncode + ".apk", FileDirBase, 1);
                            Thread.sleep(60000);
                        }
                        if(app.exists()&&app.length()<100000){continue;}

                        if (jarFile.exists() && jarFile.length() > 100000) {
                        } else {
                            if (app.exists() && app.isFile() && app.length() > 100000) {
                                String Path = app.getAbsolutePath();
                                String cmdStr = "cmd /c enjarify " + Path;
                                System.out.println(cmdStr);
                                long start = System.currentTimeMillis();
                                InvokeBat invokeBat = new InvokeBat();
                                invokeBat.runbat(cmdStr);
                                long end = System.currentTimeMillis();
                                System.out.println("finish:" + (end - start) / 1000 + " s");
                            }
                        }
                    /*
                    //如果反编译失败 再次下载编译一次
                    if(jarFile.exists()&&jarFile.length()<100000||!jarFile.exists()){
                        if(app.exists())app.delete();
                        if(jarFile.exists())jarFile.delete();
                        DownloadUtils.download(app_url, app_name + "_" + package_name + "_" + app_versioncode + ".apk", FileDirBase, 1);
                        Thread.sleep(80000);
                            if (app.exists() && app.isFile() && app.length() > 5000) {
                                String Path = app.getAbsolutePath();
                                String cmdStr = "cmd /c enjarify " + Path;
                                System.out.println(cmdStr);
                                long start = System.currentTimeMillis();
                                InvokeBat invokeBat = new InvokeBat();
                                invokeBat.runbat(cmdStr);
                                long end = System.currentTimeMillis();
                                System.out.println("finish:" + (end - start) / 1000 + " s");
                            }
                    }*/

                        if (file.exists() && file.isDirectory()) {
                        } else {
                            //unjar
                            if (jarFile.exists() && jarFile.isFile() && jarFile.length() > 100000) {
                                String filePath = jarFile.getAbsolutePath();
                                String folerName = filePath.substring(0, filePath.lastIndexOf("."));
                                System.out.println("filePath:" + folerName);
                                long start = System.currentTimeMillis();
                                ZipUtil.unzip(filePath, folerName);
                                long end = System.currentTimeMillis();
                                System.out.println("untar time:" + (end - start) / 1000 + " s");
                            }
                        }
                        //decode err
                        if (!file.exists()) {
                            if (vc == null) {
                                MspUtils.InsertTable2(stmt, package_name, app_name, app_versioncode, "false", null, null, null, app_url, app_icon);
                                continue;
                            } else {
                                if(app_name.equals(n)) {
                                    MspUtils.UpdateTable2(stmt, package_name, app_name, app_versioncode, "false", null, null, null, app_url, app_icon);
                                    continue;
                                }else {
                                    MspUtils.InsertTable2(stmt, package_name, app_name, app_versioncode, "false", null, null, null, app_url, app_icon);
                                    continue;
                                }
                            }
                        }
                        boolean b1 = Msp_Clean.hasSdk(file.getAbsolutePath());
                        boolean b2 = Msp_Clean.hasMspPro(file.getAbsolutePath());
                        String hasSdk = b1 ? "true" : "false";
                        String hasMspPro = b2 ? "true" : "false";
                        //没有记录则插入，有记录则更新
                        if (vc == null) {
                            if (!b1 && !b2) {
                                MspUtils.InsertTable2(stmt, package_name, app_name, app_versioncode, "true", "false", null, null, app_url, app_icon);
                                continue;
                            }
                            if (b1) {
                                String msp = Msp_Clean.getFile(file.getAbsolutePath());
                                System.out.println(app_name + " msp:" + msp);
                                MspUtils.InsertTable2(stmt, package_name, app_name, app_versioncode, "true", "true", msp, null, app_url, app_icon);
                                continue;
                            }
                            if (b2) {
                                MspUtils.InsertTable2(stmt, package_name, app_name, app_versioncode, "true", "true", null, "true", app_url, app_icon);
                                continue;
                            }
                        } else {
                            if (!b1 && !b2) {
                                MspUtils.UpdateTable2(stmt, package_name, app_name, app_versioncode, "true", "false", null, null, app_url, app_icon);
                                continue;
                            }
                            if (b1) {
                                String msp = Msp_Clean.getFile(file.getAbsolutePath());
                                System.out.println(app_name + " msp:" + msp);
                                MspUtils.UpdateTable2(stmt, package_name, app_name, app_versioncode, "true", "true", msp, null, app_url, app_icon);
                                continue;
                            }
                            if (b2) {
                                MspUtils.UpdateTable2(stmt, package_name, app_name, app_versioncode, "true", "true", null, "true", app_url, app_icon);
                                continue;
                            }
                        }

                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public static void spiderUrl(HtmlCleaner cleaner,ArrayList<DownloadItem1> itemList,URL url){
        TagNode node= null;
        try {
            node = cleaner.clean(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TagNode[] title=node.getElementsByName("a", true);
        for(TagNode t:title) {
            String s=t.getText().toString();
            try {
                if(s.equals( new String("安装".getBytes("gbk"),"utf-8"))) {
                    String download_name=t.getAttributeByName("download");
                    String download_detail= t.getAttributeByName("href");
                    String download_url=download_detail.replace(";", "&");
                    String download_url1=download_url.replace("&amp","&");

                    DownloadItem1 downloadItem=new DownloadItem1();
                    downloadItem.setDownload_detail(download_detail);
                    downloadItem.setDownload_name(download_name);
                    downloadItem.setDownload_url(download_url1);
                    String[] mesg=download_detail.split(";");
                    String md5=mesg[3].substring(mesg[3].indexOf("=") + 1, mesg[3].indexOf("&"));
                    String pn=mesg[2].substring(mesg[2].indexOf("=") + 1, mesg[2].indexOf("&"));
                    String vc=mesg[5].substring(mesg[5].indexOf("=")+1,mesg[5].indexOf("&"));
                    String folderName= String.valueOf(pn.charAt(pn.lastIndexOf(".")+1)).toLowerCase();
                    downloadItem.setMd5(md5);
                    downloadItem.setPn(pn);
                    downloadItem.setVc(vc);
                    downloadItem.setFolderName(folderName);
                    //add
                    String id=mesg[4].substring(mesg[4].indexOf("=") + 1, mesg[4].indexOf("&"));
                    String size=mesg[6].substring(mesg[6].indexOf("=") + 1, mesg[6].indexOf("&"));
                    String icon=mesg[7].substring(mesg[7].indexOf("=") + 1, mesg[7].indexOf("&"));
                    downloadItem.setApkid(id);
                    downloadItem.setSize(size);
                    downloadItem.setIcon(icon);
                    itemList.add(downloadItem);

                    System.out.println(download_name + ","  + vc +" Newest Online");
                    break;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
