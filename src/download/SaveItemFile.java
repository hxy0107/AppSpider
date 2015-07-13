package download;

/**
 * Created by xianyu.hxy on 2015/7/9.
 */
import java.io.IOException;
import java.io.RandomAccessFile;


public class SaveItemFile {
    //存储文件
    private RandomAccessFile itemFile;

    public SaveItemFile() throws IOException {
        this("", 0);
    }

    /**
     * @param name 文件路径、名称
     * @param pos 写入点位置 position
     * @throws IOException
     */
    public SaveItemFile(String name, long pos) throws IOException {
        itemFile = new RandomAccessFile(name, "rw");
        //在指定的pos位置开始写入数据
        itemFile.seek(pos);
    }

    /**
     * <b>function:</b> 同步方法写入文件
     * @author hoojo
     * @createDate 2011-9-26 下午12:21:22
     * @param buff 缓冲数组
     * @param start 起始位置
     * @param length 长度
     * @return
     */
    public synchronized int write(byte[] buff, int start, int length) {
        int i = -1;
        try {
            itemFile.write(buff, start, length);
            i = length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }

    public void close() throws IOException {
        if (itemFile != null) {
            itemFile.close();
        }
    }
}
