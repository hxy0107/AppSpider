import download.DownloadUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.sun.org.apache.xerces.internal.util.DOMUtil.getDocument;

/**
 * Created by xianyu.hxy on 2015/7/9.
 */
public class HtmlSpider {
    public final static String URL_BASE="http://apps.wandoujia.com/search?key=%E6%94%AF%E4%BB%98%E5%8C%85&source=search";
    //支付宝
    public final static String URL_ZHIFUBAO="http://apps.wandoujia.com/search?key=%E6%94%AF%E4%BB%98%E5%AE%9D&source=search";
    //支付
    public final static String URL_ZHIFU="http://apps.wandoujia.com/search?key=%E6%94%AF%E4%BB%98&source=search";
    //购买
    public final static String URL_GOUMAI="http://apps.wandoujia.com/search?key=%E8%B4%AD%E4%B9%B0&source=search";
    //商品
    public final static String URL_SHANGPING="http://apps.wandoujia.com/search?key=%E5%95%86%E5%93%81&source=search";
    //游戏
    public final static String URL_YOUXI="http://apps.wandoujia.com/search?key=%E6%B8%B8%E6%88%8F&source=search";

    public final static String URL_ELSE="http://apps.wandoujia.com/search?key=%E9%92%B1&source=search";

    public final static String SEARCH_NAME="支付宝";
    public final static String URL_APPEND="&source=search";
    public final static String PAGE="&page=";
    public static int PAGENUM;
    public static ArrayList<DownloadItem> itemList;
    public final static String FileDirBase="e:"+File.separator+"temp";

    static ArrayList<String> array ;
    public static void main(String[] args) throws IOException, XPatherException {
       // String url=URL_BASE+SEARCH_NAME;
       // URL url=new URL("http://apps.wandoujia.com/search?key=%E6%94%AF%E4%BB%98%E5%8C%85&source=search");

            URL url=new URL(URL_SHANGPING);
        itemList=new ArrayList<DownloadItem>();
        HtmlCleaner cleaner=new HtmlCleaner();
        itemList=new ArrayList<DownloadItem>();

        TagNode node=cleaner.clean(url);
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
        }
        String pageNums=array.get(array.size()-2);
        String pageNum=pageNums.substring(pageNums.lastIndexOf("=")+1);
        PAGENUM=Integer.parseInt(pageNum);

        for(int page=1;page<=PAGENUM;page++){
            URL newURL=new URL(URL_ELSE+PAGE+page);
            spiderUrl(cleaner,itemList,newURL);
        }

/*
sql语句 插入不重复元素 根据包名和版本号区别
INSERT INTO app_info.`info`
(package_name,app_name,app_version)
SELECT 'com.hxy.code','xx',32
FROM DUAL
WHERE NOT EXISTS(
SELECT * FROM app_info.`info`
WHERE info.`package_name`='com.hxy.code'
AND info.`app_version`=32
);
 */
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            System.out.println("Success loading Mysql Driver!");
            Connection connect= DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1");
            System.out.println("Success connect Mysql server!");
            Statement stmt=connect.createStatement();

            for(DownloadItem downloadItems:itemList){
                String appName=downloadItems.getDownload_name();
                String appUrl=downloadItems.getDownload_url();
                String pn=downloadItems.getPn();
                String md5=downloadItems.getMd5();
                String vc=downloadItems.getVc();
                String id=downloadItems.getApkid();
                String size=downloadItems.getSize();
                String icon=downloadItems.getIcon();

                String sql="insert into app_info.info(package_name,app_name,app_version,app_md5,app_url,sdk_version,alipay_version)value("+"'"+
                        pn+"'"+",'"+appName+"',"+vc+",'"+md5+"','"+appUrl+"',2.1"+","+"9.1"+");";
                String sql1="INSERT INTO app_info.`pack_only`\n" +
                        "(package_name,app_name,app_version,app_md5,app_url,app_id,app_size,app_icon)\n" +
                        "SELECT "+"'"+pn+"','"+appName+"','"+vc+"','"+md5+"','"+appUrl+"','"+id+"','"+size+"','"+icon+"'"+"\n" +
                        "FROM DUAL\n" +
                        "WHERE NOT EXISTS(\n" +
                        "SELECT * FROM app_info.`pack_only`\n" +
                        "WHERE pack_only.`package_name`='" +pn+
                        "');";
                int result=stmt.executeUpdate(sql1);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }









        //download
        for(DownloadItem downloadItems:itemList){
            String appName=downloadItems.getDownload_name();
            String appUrl=downloadItems.getDownload_url();
            String pn=downloadItems.getPn();
            String md5=downloadItems.getMd5();
            String vc=downloadItems.getVc();
            String folerName=downloadItems.getFolderName();
            System.out.println("folerName:"+folerName);


            File file=new File(FileDirBase);
            if(!file.exists()){file.mkdir();}
            String childName=FileDirBase+File.separator+folerName;
            File childFile=new File(childName);
            if(!childFile.exists()){childFile.mkdir();}

            File app=new File(FileDirBase+File.separator+folerName+File.separator+appName+"_"+pn+"_"+vc+".apk");
            if(!app.exists()){
            DownloadUtils.download(appUrl
                    , appName+"_"+pn+"_"+vc+".apk", FileDirBase+File.separator+folerName, 1);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }


        }




    }


    public static void spiderUrl(HtmlCleaner cleaner,ArrayList<DownloadItem> itemList,URL url){
       // HtmlCleaner cleaner=new HtmlCleaner();
       // itemList=new ArrayList<DownloadItem>();

        TagNode node= null;
        try {
            node = cleaner.clean(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TagNode[] title=node.getElementsByName("a", true);
        for(TagNode t:title) {
            String s=t.getText().toString();
            if(s.equals("安装")) {
                String download_name=t.getAttributeByName("download");
                String download_detail= t.getAttributeByName("href");
                String download_url=download_detail.replace(";", "&");

                DownloadItem downloadItem=new DownloadItem();
                downloadItem.setDownload_detail(download_detail);
                downloadItem.setDownload_name(download_name);
                downloadItem.setDownload_url(download_url);
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
        }
    }

}
