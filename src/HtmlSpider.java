import download.DownloadUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
    public final static String SEARCH_NAME="支付宝";
    public final static String URL_APPEND="&source=search";
    public final static String PAGE="&page=";
    public static int PAGENUM;
    public static ArrayList<DownloadItem> itemList;
    public final static String FileDirBase="d:"+File.separator+"temp";

    static ArrayList<String> array ;
    public static void main(String[] args) throws IOException, XPatherException {
       // String url=URL_BASE+SEARCH_NAME;
        URL url=new URL("http://apps.wandoujia.com/search?key=%E6%94%AF%E4%BB%98%E5%8C%85&source=search");
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
            URL newURL=new URL(URL_BASE+PAGE+page);
            spiderUrl(cleaner,itemList,newURL);
        }


        //download
        for(DownloadItem downloadItems:itemList){
            String appName=downloadItems.getDownload_name();
            String appUrl=downloadItems.getDownload_url();
            String pn=downloadItems.getPn();
            String md5=downloadItems.getMd5();
            String folerName=downloadItems.getFolderName();
            System.out.println("folerName:"+folerName);


            File file=new File(FileDirBase);
            if(!file.exists()){file.mkdir();}
            String childName=FileDirBase+File.separator+folerName;
            File childFile=new File(childName);
            if(!childFile.exists()){childFile.mkdir();}
            DownloadUtils.download(appUrl
                    , appName+"_"+pn+"_"+md5+".apk", FileDirBase+File.separator+folerName, 1);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }



/*
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
                downloadItem.setMd5(md5);
                downloadItem.setPn(pn);
                downloadItem.setVc(vc);
                itemList.add(downloadItem);


                System.out.println(download_name + "," + download_detail + "    \n" + download_url + "\n");
            }
        }*/



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
                itemList.add(downloadItem);

                System.out.println(download_name + "," + download_detail + "    \n" + download_url + "\n");
            }
        }
    }

}
