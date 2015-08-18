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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by xianyu.hxy on 2015/8/17.
 */
public class AliMsp {
    public static ArrayList<DownloadItem1> itemList;
    public final static String FileDirBase="e:"+File.separator+"msp";

    public static int PAGENUM;
    public static ArrayList<String> array ;
    public final static String PAGE="&page=";
    public static void main(String[] args){
        getCrawlerItem("支付");
        crawler(itemList);

    }
    public static void crawler(ArrayList<DownloadItem1> itemList){
        if(itemList.size()<2)return;
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            System.out.println("Success loading Mysql Driver!");
            Connection connect= DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1");
            System.out.println("Success connect Mysql server!");
            Statement stmt=connect.createStatement();

            for(DownloadItem1 downloadItems:itemList) {
                String package_name = downloadItems.getPn();
                String app_name = downloadItems.getDownload_name();
                String app_versioncode = downloadItems.getVc();
                String app_url = downloadItems.getDownload_url();
                String app_icon = downloadItems.getIcon();

                String vc=MspUtils.QueryVcTable2(stmt,app_name);
                if(vc==app_versioncode){continue;}
                else{
                    //没有记录 则插入
                    File baseFile = new File(FileDirBase);
                    if (!baseFile.exists()) {
                        baseFile.mkdir();
                    }
                    File app = new File(FileDirBase + File.separator + app_name + "_" + package_name + "_" + app_versioncode + ".apk");
                    File jarFile = new File(FileDirBase + File.separator + app_name + "_" + package_name + "_" + app_versioncode + "-enjarify" + ".jar");
                    File file = new File(FileDirBase + File.separator + app_name + "_" + package_name + "_" + app_versioncode + "-enjarify");
                    if(app.exists()&&app.length()>10000){}else {
                        DownloadUtils.download(app_url, app_name + "_" + package_name + "_" + app_versioncode + ".apk", FileDirBase, 1);
                        Thread.sleep(30000);
                    }

                    if(jarFile.exists()&&jarFile.length()>500){}
                    else {
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
                    }
                    //如果反编译失败 再次下载编译一次
                    if(jarFile.exists()&&jarFile.length()<10000||!jarFile.exists()){
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
                    }

                    if(file.exists()&&file.isDirectory()) {}else {
                        //unjar
                        if (jarFile.exists() && jarFile.isFile() && jarFile.length() > 10000) {
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
                    if(!file.exists()){
                        MspUtils.InsertTable2(stmt,package_name,app_name,app_versioncode,"false",null,null,null,app_url,app_icon);
                        continue;
                    }
                    boolean b1=Msp_Clean.hasSdk(file.getAbsolutePath());
                    boolean b2=Msp_Clean.hasMspPro(file.getAbsolutePath());
                    String hasSdk=b1?"true":"false";
                    String hasMspPro=b2?"true":"false";
                    //没有记录则插入，有记录则更新
                    if(vc==null) {
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
                    }else {
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

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public static ArrayList<DownloadItem1> getCrawlerItem(String RequestApp){
        String RequestApp1= null;
        try {
            RequestApp1 = URLEncoder.encode(RequestApp, "utf-8");
            String requestUrl="http://apps.wandoujia.com/search?key="+RequestApp1+"&source=search";
            //**最后再加上 如果统计表中的是最新的话 直接跳过
            URL url=new URL(requestUrl);
            HtmlCleaner cleaner=new HtmlCleaner();
            //判断关键词有多少页
            TagNode node=cleaner.clean(url);
            TagNode[] title=node.getElementsByName("a", true);
            itemList=new ArrayList<DownloadItem1>();
//查询app页数
            for(TagNode page:title){
                String s1=page.getText().toString().trim();
                if(s1.equals("下一页")){
                    array=new ArrayList<String>();
                    TagNode par=page.getParent();
                    TagNode[] lists= par.getElementsByName("a",true);
                    for(TagNode x:lists){
                        String xx= x.getAttributeByName("href");
                        if(xx!=null&&xx.length()!=0){
                            array.add(xx);
                        }
                    }
                }
            }
            String pageNums=array.get(array.size()-2);
            String pageNum=pageNums.substring(pageNums.lastIndexOf("=")+1);
            PAGENUM=Integer.parseInt(pageNum);
            System.out.println(PAGENUM);
            //分析网页

            itemList.clear();
            for(int page=1;page<=PAGENUM;page++){
                URL newURL=new URL(requestUrl+PAGE+page);
                System.out.println(requestUrl+PAGE+page);
                spiderUrl_1(cleaner,itemList,newURL);
            }
            return itemList;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
                if(s.equals( new String("安装".getBytes("gbk"),"gbk"))) {
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
                    String icon=mesg[8].substring(mesg[8].indexOf("=") + 1, mesg[8].indexOf("&"));
                    downloadItem.setApkid(id);
                    downloadItem.setSize(size);
                    downloadItem.setIcon(icon);
                    itemList.add(downloadItem);

                    System.out.println(download_name + "," + download_detail + "    \n" + download_url + "\n");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
    public static void spiderUrl_1(HtmlCleaner cleaner,ArrayList<DownloadItem1> itemList,URL url){
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
                if(s.equals( new String("安装".getBytes("gbk"),"gbk"))) {
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

                    System.out.println(download_name + "," + download_detail + "    \n" + download_url + "\n");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
