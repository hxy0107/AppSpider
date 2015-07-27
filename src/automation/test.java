package automation;

/**
 * Created by xianyu.hxy on 2015/7/21.
 */
public class test {
    public static final String test="C:\\Users\\xianyu.hxy\\Desktop\\jar\\1-enjarify\\com\\alipay\\sdk\\cons\\GlobalConstantsHXY.txt";
    public static final String G="public static final java.lang.String g = \"";
    public static String msp_version;
    public static void main(String[] args){

        String[] s=HasPac.readTXT(test);
        for(String line:s){
            System.out.println(line);
            if(line.trim().startsWith(G)){
                msp_version=line.substring(line.indexOf("\"")+1,line.lastIndexOf("\""));
                break;
            }
        }
        System.out.println("msp_version: "+msp_version);
    }
}

