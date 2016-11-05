package com.lgs.AppManage.AppManage.installFragment.Backup;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.lgs.AppManage.Utils.SDCardUtil;
import com.lgs.AppManage.Utils.ShellUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

 
public class BackUtil {
    static final String TAG = "backup";
    static final String NEW_FILE_BASE = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "AppManage" + File.separator;
    static final String NEW_FILE_BACKUP = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "AppManage" + File.separator + "ApkBackUp" + File.separator;
    Context context;
    SDCardUtil sdCardUtil;
    File mBaseFile = new File(NEW_FILE_BACKUP);
    boolean isBackData = true;
    List<String> SearchFileExt = null;

    public BackUtil(Context context) {
        this.context = context;
        sdCardUtil = new SDCardUtil(context);
        // if (!mBaseFile.exists()) mBaseFile.mkdirs();
        if (!new File(NEW_FILE_BACKUP).exists()) new File(NEW_FILE_BACKUP).mkdirs();
    }

    public boolean isUserApp(PackageInfo pInfo) {
        return (((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) && ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0));
    }

    public void backupAllUserApp() {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> allPackages = packageManager.getInstalledPackages(0);
        for (int i = 0; i < allPackages.size(); i++) {
            PackageInfo packageInfo = allPackages.get(i);
            String path = packageInfo.applicationInfo.sourceDir;
            String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            String dataPath = packageInfo.applicationInfo.dataDir;

            Log.i(TAG, path);
            Log.i(TAG, name);

            try {
                if (isUserApp(packageInfo)) {
                    //Log.i(TAG, name + " is not user app,skip...");
                    //  updateView(name + " is not user app,skip...");
                    //continue;
                }
                // updateView("Start backup:" + name + "...");
                backupApp(path, name);
                backupOdex(path, name);
                //  updateView("*****Succeed backup:" + name + "******");
                if (isBackData) {
                    File data = new File(dataPath);
                    File des = new File(NEW_FILE_BACKUP + name + ".zip");
                    if (data.exists()) {
                        String cmd = "tar -cvf  " + "-C " + NEW_FILE_BACKUP + " " + name + ".tar " + " " + dataPath + " -exclude " + dataPath + "/lib/" + " ";
                        //  String []string = new String[]{"chmod 7777 " + dataPath+" ",cmd,"chmod 7771 " + dataPath};
                        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(cmd, true);
                     //   System.out.println(cmd + " " + commandResult.errorMsg + " " + commandResult.result + " " + commandResult.successMsg);
                    }

                }
                // Looper.prepare();
                //  Toast.makeText(installActivity.this, "Succeed backup:" + path, Toast.LENGTH_SHORT).show();
                //   Looper.loop();
                Log.d(TAG, "Succeed backup:" + name);
            } catch (Exception e) {
                Log.i(TAG, path + "Failed backup  " + e.getMessage());
                // e.printStackTrace();
                //     continue;
            }
        }
    }

    public void backupApp(String path, String outname) throws IOException {
        File in = new File(path);
        File out = new File(mBaseFile + "/" + outname, outname + ".apk");
        if (out.exists()) out.delete();
        if (!out.exists()) out.createNewFile();
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        int count;
        byte[] buffer = new byte[256 * 1024];
        while ((count = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }
        if (out.exists() && out.length() == 0) {
            out.delete();
        }
        fis.close();
        fos.flush();
        fos.close();
    }

    public void backupOdex(String path, String outname) throws IOException {
        File in = new File(getOdexLoc(path));
     //   Log.e("8912", in.toString());
        if (!in.exists()) return;
        File out = new File(mBaseFile + "/" + outname, outname + ".odex");
        if (out.exists()) out.delete();
        if (!out.exists()) out.createNewFile();
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);

        int count;
        byte[] buffer = new byte[256 * 1024];
        while ((count = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }
        if (out.exists() && out.length() == 0) {
            out.delete();
        }

        fis.close();
        fos.flush();
        fos.close();
    }

    public String getOdexLoc(String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
     /*       System.out.println(new File(path).getParent().toString());
            List<String> list = GetFilesSuffix(String.valueOf(new File(path).getParent()), ".odex", true);
          for (String s:list){
              System.out.println(s);
          }
            return list.size() <= 0 ? "" : list.get(0);*/

            String setPath = new File(path).getParent() + "/";
            // System.out.println(path1);

            //  Log.d("op",searchFile(".odex"));

            File[] files = new File(setPath).listFiles();
            SearchFileExt = new ArrayList<>();
            SearchFileExt(files, ".odex");
            String string = SearchFileExt.size() > 0 ? SearchFileExt.get(0) : "";
            //Log.v("s", string);
            return string;
        } else {
            return path == "" | path == null ? "" : path.substring(0, path.lastIndexOf(".")) + ".odex";
        }
    }

    public void SearchFileExt(File[] files, String ext) {
        for (File file : files) {
            if (file.isDirectory())//若为目录则递归查找
            {
                SearchFileExt(file.listFiles(), ext);
            } else if (file.isFile()) {
                String path = file.getPath();
                //     Log.e("8912", path);

                if (path.endsWith(ext))//查找指定扩展名的文件
                {
                    //do someth
                    //        Log.e("89", path);
                    SearchFileExt.add(path);
                }
            }
        }
    }


}
