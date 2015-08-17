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

/**
 * Created by xianyu.hxy on 2015/8/13.
 */
public class Msp_Crawler {
    public static final String BASE_FOLDER = "com" + File.separator + "alipay" + File.separator + "sdk" + File.separator + "cons";
    public final static String FileDirBase="e:"+File.separator+"msp";
    public static Boolean DownloadErr=false;
    public static Boolean Fanbianyi=false;
    public static Boolean HasMsp=false;
    public static Boolean HasMspPro=false;

    public static ArrayList<DownloadItem1> itemList;

    public static int PAGENUM;
    public static ArrayList<String> array ;
    public final static String PAGE="&page=";
    public static void main(String[] args){
        crawler("支付宝");
    }
    public static void crawler(String RequestApp){
        String RequestApp1= null;
        try {
            RequestApp1 = URLEncoder.encode(RequestApp, "utf-8");
            String requestUrl="http://apps.wandoujia.com/search?key="+RequestApp1+"&source=search";

            Class.forName("org.gjt.mm.mysql.Driver");
            System.out.println("Success loading Mysql Driver!");
            Connection connect= DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1");
            System.out.println("Success connect Mysql server!");
            Statement stmt=connect.createStatement();
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
                spiderUrl(cleaner,itemList,newURL);
            }


            if(itemList.size()<2)return;
            for(DownloadItem1 downloadItems:itemList){
                String appName=downloadItems.getDownload_name();
                String appUrl=downloadItems.getDownload_url();
                String pn=downloadItems.getPn();
                String md5=downloadItems.getMd5();
                String vc=downloadItems.getVc();
                String id=downloadItems.getApkid();
                String size=downloadItems.getSize();
                String icon=downloadItems.getIcon();
                Fanbianyi=false;
                HasMsp=false;
                HasMspPro=false;

                String sql="SELECT app_name, app_versioncode FROM app_info.`msp_table_8.12_copy` WHERE app_name='"+appName+"' ";
                ResultSet result=stmt.executeQuery(sql);
                String n=null;
                String v=null;
                if (result.next()){
                    n=result.getString(1);
                    v=result.getString(2);
                }
                if(n==null){
                    String sql_1="SELECT app_name,app_version FROM app_info.`pack_only_copy_8.12_copy` WHERE app_name='"+appName+"'";
                    Statement stmt1=connect.createStatement();
                    ResultSet result_1=stmt1.executeQuery(sql_1);
                    String n_1=null;
                    String v_1=null;
                    if (result_1.next()){
                        n_1=result.getString(1);
                        v_1=result.getString(2);
                    }
                   // if(n_1==appName&&v_1==vc){continue;}***更新完表1再打开
                    if(n_1==appName){
                        //下载更新
                    }else{
                        //下载插入
                    }
                    //继续下载分析
                    //download and update msp
                    System.out.println(appName + " need update" );
                    File baseFile = new File(FileDirBase);
                    if (!baseFile.exists()) {
                        baseFile.mkdir();
                    }
                    File app = new File(FileDirBase + File.separator + appName + "_" + pn + "_" + vc + ".apk");
                    File jarFile = new File(FileDirBase + File.separator + appName + "_" + pn + "_" + vc + "-enjarify" + ".jar");
                    File file = new File(FileDirBase + File.separator + appName + "_" + pn + "_" + vc + "-enjarify");


                    //download
                    if(!app.exists()||app.length()==0) {
                        // System.out.println("appUrl *****:"+appUrl);
                        DownloadErr=false;
                        DownloadUtils.download(appUrl, appName + "_" + pn + "_" + vc + ".apk", FileDirBase, 4);
                        Thread.sleep(20000);
                        int count=0;
                        while (app.length() < 1000) {
                            Thread.sleep(10000);
                            count++;
                            if(count>4){
                                DownloadErr=true;
                                break;
                            }
                        }
                        if(DownloadErr==true)continue;
                    }


                    if(jarFile.exists()&&jarFile.length()>500){}
                    else {
                        if (app.exists() && app.isFile() && app.length() > 5000) {
                            String Path = app.getAbsolutePath();
                            String cmdStr = "cmd /c enjarify " + Path;
                            long start = System.currentTimeMillis();
                            InvokeBat invokeBat = new InvokeBat();
                            invokeBat.runbat(cmdStr);
                            long end = System.currentTimeMillis();
                            System.out.println("finish:" + (end - start) / 1000 + " s");
                        }
                    }
                    // 反编译失败
                    if(!jarFile.exists()||jarFile.length()<10000){
                        Fanbianyi=false;
                        if(n_1==null){
                            //插入表一
                            String sql_3="INSERT INTO app_info.`pack_only_copy_8.12_copy` (package_name,app_name,decode_app,app_version,app_url,app_icon)\n" +
                                    "VALUES('"+pn+"','"+appName+"','false','"+vc+"','"+appUrl+"','"+icon+"')";
                            stmt.executeQuery(sql_3);
                        }else {
                            //更新数据
                            String sql_4="UPDATE app_info.`pack_only_copy_8.12_copy` SET decode_app='false',app_version='"+vc+"',app_url='"+appUrl+"',app_icon='"+icon+"' \n" +
                                    "WHERE app_name='"+appName+"'";
                            stmt.executeQuery(sql_4);
                        }
                        continue;
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

                    if(!file.exists())continue;

                    //检测是否含有包




                }else if(n!=null&&v!=vc){
                    //下载更新表

                }else if(n!=null&&v==vc){
                    continue;
                }



            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
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
}
