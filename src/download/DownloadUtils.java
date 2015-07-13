package download;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by xianyu.hxy on 2015/7/9.
 */
public abstract class DownloadUtils {

    public static void download(String url) {
        DownloadInfo bean = new DownloadInfo(url);
        LogUtils.info(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        new Thread(down).start();
    }

    public static void download(String url, int threadNum) {
        DownloadInfo bean = new DownloadInfo(url, threadNum);
        LogUtils.info(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        new Thread(down).start();
    }

    public static void download(String url, String fileName, String filePath, int threadNum) {
        DownloadInfo bean = new DownloadInfo(url, fileName, filePath, threadNum);
        LogUtils.info(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        Thread t=new Thread(down);
        t.start();
       boolean b= down.isFinish;

    }
}