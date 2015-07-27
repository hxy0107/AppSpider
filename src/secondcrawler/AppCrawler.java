package secondcrawler;

/**
 * Created by xianyu.hxy on 2015/7/21.
 */
import download.DownloadUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by xianyu.hxy on 2015/7/21.
 */
public class AppCrawler {
    public static final String URL_START="http://apps.wandoujia.com/apps/";
    public static final String URL_END="/versions?pos=w/popup";

    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String USER = "root";
    public static final String PASS = "1";
    public static final String URL = "jdbc:mysql://localhost:3306";
    public static final int PAGESIZE = 5;
    static int pageCount;
    static int curPage = 1;
    public static ArrayList<String> arrayList;
    public static ArrayList<String> nameList;

    public static String package_name;
    public static String app_name;
    public static String app_version;
    public static String app_size;
    public static String msp_version=null;
    public final static String FileDir="e:"+File.separator+"temp1";
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.gjt.mm.mysql.Driver");
        System.out.println("Success loading Mysql Driver!");
        Connection connect= DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1");
        System.out.println("Success connect Mysql server!");
        Statement stmt=connect.createStatement();


        String url_list;
        //获得包名
        arrayList=new ArrayList<String>();
        Class.forName(DRIVER);
        Connection con = DriverManager.getConnection(URL, USER, PASS);
        String sql = "SELECT * FROM app_info.`msp_final`;";
        PreparedStatement stat = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stat.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString(2));
            arrayList.add(rs.getString(2));
        }

        for(String pac:arrayList){
           // url=URL_START+mid+URL_END;
            url_list=URL_START+pac+URL_END;
           // crawlerWeb(url);

            try {
                HtmlCleaner cleaner = new HtmlCleaner();
                nameList=new ArrayList<String>();
                URL url = new URL(url_list);
                TagNode node = cleaner.clean(url);
                Object[] tags=node.evaluateXPath("/body/div//div[@class='version-block']/div[position()<4]");
                Object[] tagIcon=node.evaluateXPath("/body/div//img");
                String app_icon=((TagNode)tagIcon[0]).getAttributeByName("src");
                System.out.println(app_icon);
                int i=1;
                for(Object tag:tags){
                    // System.out.println(((TagNode)tagSize).getText()+"");
                    Object[] tagVersion=node.evaluateXPath("/body/div//div[@class='version-block']/div["+i+"]//i[@itemprop='softwareVersion']");
                    String app_verison=((TagNode)tagVersion[0]).getText()+"";
                    System.out.println(((TagNode)tagVersion[0]).getText()+"");

                    Object[] tagVersionCode=node.evaluateXPath("/body/div//div[@class='version-block']/div["+i+"]//span[@class='version-code']");
                    String app_versioncode=((TagNode)tagVersionCode[0]).getText()+"";
                    System.out.println(((TagNode)tagVersionCode[0]).getText()+"");
                    Object[] tagFileSize=node.evaluateXPath("/body/div//div[@class='version-block']/div["+i+"]//span[@class='apk-size']");
                    String app_size=((TagNode)tagFileSize[0]).getText()+"";
                    System.out.println(((TagNode)tagFileSize[0]).getText()+"");
                    Object[] tagDownload=node.evaluateXPath("/body/div//div[@class='version-block']/div["+i+"]//a[@download]");
                    String app_url1=((TagNode) tagDownload[0]).getAttributeByName("href");
                    String app_url=app_url1.replaceAll("&amp;","&");
                    System.out.println("下载地址: "+app_url+"\n");
                    String app_name=((TagNode) tagDownload[0]).getAttributeByName("download");
                    i++;

                    //***写入数据库 明天写***
                    /*
                    INSERT INTO app_info.`msp_test`(package_name,app_name,app_version,app_versioncode,app_size,msp_version,app_url,app_icon)
                    VALUE('','','','','','','');
                     */
                    String sql_in="INSERT INTO app_info.`msp_table_copy`(package_name,app_name,app_version,app_versioncode,app_size,msp_version,app_url,app_icon)\n" +
                            "VALUE('"+pac+"','"+app_name+"','"+app_verison+"','"+app_versioncode+"','"+app_size+"','"+msp_version+"','"+app_url+"','"+app_icon+"');";
                    int result=stmt.executeUpdate(sql_in);



                    DownloadUtils.download(app_url
                            , app_name.trim() + "_" + pac.trim() + "_" + app_verison.trim() + ".apk", FileDir, 1);

                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            /*
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }



    }
    public static void crawlerWeb(String s){
        URL url= null;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HtmlCleaner cleaner=new HtmlCleaner();


        TagNode node= null;
        try {
            node = cleaner.clean(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TagNode[] title=node.getElementsByName("i",true);
        for(TagNode t:title){
            String txt=t.getText().toString().trim();

            System.out.println(txt);
        }
        /*
        TagNode[] title=node.getElementsByName("a", true);
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
        }*/

    }
}
