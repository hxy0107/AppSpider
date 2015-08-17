package threadcrawler;

import javax.swing.plaf.nimbus.State;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by xianyu.hxy on 2015/8/17.
 */
public class MspUtils {
    public static void InsertTable1(Statement stmt,String package_name,String app_name,String decode_app,String has_sdk,String has_sdk_pro,String app_version,String app_url,String app_icon){
        String sql="INSERT INTO app_info.`pack_only_copy_8.12_copy`(package_name,app_name,decode_app,has_sdk,has_sdk_pro,app_version,app_url,app_icon)\n" +
                "VALUES("+package_name+","+app_name+","+decode_app+","+has_sdk+","+has_sdk_pro+","+app_version+","+app_url+","+app_icon+")";
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void UpdateTable1(Statement stmt,String package_name,String app_name,String decode_app,String has_sdk,String has_sdk_pro,String app_version,String app_url,String app_icon){
        String sql="UPDATE app_info.`pack_only_copy_8.12_copy` SET package_name="+package_name+",decode_app="+decode_app+",has_sdk="+has_sdk+",has_sdk_pro="+has_sdk_pro+",app_version="+app_version+",app_url="+app_url+",app_icon="+app_icon+"\n" +
                "WHERE app_name=app_name";
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void InsertTable2(Statement stmt,String package_name,String app_name,String app_versioncode,String decode_app,String has_sdk,String msp_version,String msp_pro,String app_url,String app_icon){
        String sql="INSERT INTO app_info.`msp_table_8.12_copy`(package_name,app_name,app_versioncode,decode_app,has_sdk,msp_version,msp_pro,app_url,app_icon)\n" +
                "VALUES("+package_name+","+app_name+","+app_versioncode+","+decode_app+","+has_sdk+","+msp_version+","+msp_pro+","+app_url+","+app_icon+")";
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void UpdateTable2(Statement stmt,String package_name,String app_name,String app_versioncode,String decode_app,String has_sdk,String msp_version,String msp_pro,String app_url,String app_icon){
        String sql="UPDATE app_info.`msp_table_8.12_copy` SET package_name="+package_name+",app_versioncode="+app_versioncode+",decode_app="+decode_app+",has_sdk="+has_sdk+",msp_version="+msp_version+",msp_pro="+msp_pro+",app_url="+app_url+",app_icon="+app_icon+"\n" +
                "WHERE app_name=app_name";
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String QueryVcTable1(Statement stmt,String app_name){
        String sql_1="SELECT app_version FROM app_info.`pack_only_copy_8.12_copy` WHERE app_name='"+app_name+"'";
        ResultSet result= null;
        String v=null;
        try {
            result = stmt.executeQuery(sql_1);
            if (result.next()){
                v=result.getString(1);
            }
            if(v!=null)return v;
        } catch (SQLException e) {
            e.printStackTrace();
        }
       return null;
    }
    public static String QueryVcTable2(Statement stmt,String app_name){
        String sql_1="SELECT app_version FROM app_info.`msp_table_8.12_copy` WHERE app_name='"+app_name+"'";
        ResultSet result= null;
        String v=null;
        try {
            result = stmt.executeQuery(sql_1);
            if (result.next()){
                v=result.getString(1);
            }
            if(v!=null)return v;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
