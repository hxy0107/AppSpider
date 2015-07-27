package automation;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by xianyu.hxy on 2015/7/23.
 */
public class AutoDetack {
    public static String RequestApp="支付宝";
    public static String RequestPac="com.jumi";
    public static String requestUrl="http://apps.wandoujia.com/search?key="+RequestApp+"&source=search";
    public static String choose_url;
    public static String app_verison=null;
    public static String sql_version=null;
    public static String  app_url=null;
    public static String app_name=null;
    public static boolean IsNewest=true;
    public static void main(String[] args){
        // 检查数据库
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            System.out.println("Success loading Mysql Driver!");
            Connection connect= DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1");
            System.out.println("Success connect Mysql server!");
            Statement stmt=connect.createStatement();
            String sql_in="SELECT app_version FROM app_info.`msp_table_copy` WHERE package_name='"+RequestPac+"'";
            ResultSet result=stmt.executeQuery(sql_in);

            if(result.next()){
                sql_version=result.getString(1);
            }
            System.out.println("sql_version: "+sql_version);


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        if(RequestPac!=null){
           choose_url="http://apps.wandoujia.com/apps/"+RequestPac+"/versions?pos=w/popup";
        }
        HtmlCleaner cleaner = new HtmlCleaner();

        URL url = null;
        try {
            url = new URL(choose_url);
            TagNode node = cleaner.clean(url);
            Object[] tagVersion=node.evaluateXPath("/body/div//div[@class='version-block']/div["+"1"+"]//i[@itemprop='softwareVersion']");
            app_verison=((TagNode)tagVersion[0]).getText()+"";
            System.out.println(((TagNode)tagVersion[0]).getText()+"");

            Object[] tagVersionCode=node.evaluateXPath("/body/div//div[@class='version-block']/div["+"1"+"]//span[@class='version-code']");
            String app_versioncode=((TagNode)tagVersionCode[0]).getText()+"";
            System.out.println(((TagNode)tagVersionCode[0]).getText()+"");
            Object[] tagFileSize=node.evaluateXPath("/body/div//div[@class='version-block']/div["+"1"+"]//span[@class='apk-size']");
            String app_size=((TagNode)tagFileSize[0]).getText()+"";
            System.out.println(((TagNode)tagFileSize[0]).getText()+"");
            Object[] tagDownload=node.evaluateXPath("/body/div//div[@class='version-block']/div[" + "1" + "]//a[@download]");
            String app_url1=((TagNode) tagDownload[0]).getAttributeByName("href");
            app_url=app_url1.replaceAll("&amp;","&");
            System.out.println("下载地址: "+app_url+"\n");
            app_name=((TagNode) tagDownload[0]).getAttributeByName("download");




        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPatherException e) {
            e.printStackTrace();
        }

        //对比数据库 判断app是否最新
         if(app_verison!=sql_version){
            IsNewest=false;

         }



    }
}
