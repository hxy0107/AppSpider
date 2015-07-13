package download;

/**
 * Created by xianyu.hxy on 2015/7/9.
 */
import download.DownloadUtils;


public class TestDownloadMain {

    public static void main(String[] args) {
        /*DownloadInfo bean = new DownloadInfo("http://i7.meishichina.com/Health/UploadFiles/201109/2011092116224363.jpg");
        System.out.println(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        new Thread(down).start();*/

        //DownloadUtils.download("http://i7.meishichina.com/Health/UploadFiles/201109/2011092116224363.jpg");
        DownloadUtils.download("http://apps.wandoujia.com/redirect?signature=6be23c4&amp&url=http%3A%2F%2Fdownload.eoemarket.com%2Fapp%3Fchannel_id%3D100%26client_id%26id%3D27060&amp&pn=com.tenpay.android&amp&md5=8cf78a264613b3f565d77908d1b66bd9&amp&apkid=13963130&amp&vc=40&amp&size=2343796&amp&pos=w/search/list//%E6%94%AF%E4%BB%98%E5%8C%85/15#name=²Æ¸¶Í¨&amp&icon=http://img.wdjimg.com/mms/icon/v1/b/2e/362383b484ba390397bf50fee09e42eb_68_68.png&amp&content-type=application\n"
                , "bb.apk", "d:/temp", 5);
    }
}
