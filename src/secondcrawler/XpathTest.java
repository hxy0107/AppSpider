package secondcrawler;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by xianyu.hxy on 2015/7/21.
 */
public class XpathTest {
    public static ArrayList<String> nameList;
    public static void main(String[] args) throws Exception
    {
        try {
            HtmlCleaner cleaner = new HtmlCleaner();
            nameList=new ArrayList<String>();
            URL url = new URL("http://apps.wandoujia.com/apps/com.eg.android.AlipayGphone/versions?pos=w/popup");
            TagNode node = cleaner.clean(url);
            Object[] tags=node.evaluateXPath("/body/div//div[@class='version-block']/div[position()<4]");
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
                System.out.println(((TagNode) tagDownload[0]).getAttributeByName("href"));
                String app_url=((TagNode) tagDownload[0]).getAttributeByName("href");
                String app_name=((TagNode) tagDownload[0]).getAttributeByName("download");
                i++;

                //***写入数据库 明天写***

            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }




    }

}
