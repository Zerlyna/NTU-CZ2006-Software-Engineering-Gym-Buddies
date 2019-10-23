package sg.edu.ntu.scse.cz2006.gymbuddies.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import sg.edu.ntu.scse.cz2006.gymbuddies.AppConstants;


/**
 * @author Chia Yu
 * @since 2019-10-23
 */
public class DiskIOHelper implements AppConstants {
    private static final String TAG = "gb.helper.IO";
    private static final String DIR_PIC = "pic";


    public static boolean hasImageCache(Context context, String fileName){
        // check if file exist
        File cacheDir = new File(context.getCacheDir(), DIR_PIC);
        if (!cacheDir.exists()){
            Log.d(TAG, cacheDir.getAbsolutePath()+" -> not exist");
            return false;
        }
        File cacheFile = new File(cacheDir, fileName+".png");
        if (!cacheFile.exists()){
            Log.d(TAG, cacheFile.getAbsolutePath()+" -> not exist");
            return false;
        }

        // file is exist, validation cacheFile.lastModified()
        long now = System.currentTimeMillis();
        long diff = now - cacheFile.lastModified();
        if (diff > MAX_CACHE_DURATION){
            // remove cache file
            if (cacheFile.delete()){
                Log.d(TAG, cacheFile.getName()+" is deleted");
            }
            return false;
        }
        return true;
    }

    public static void saveImageCache(Context context, Bitmap bmp, String name){
        Log.d(TAG, "saving");
        File cacheDir = new File(context.getCacheDir(), DIR_PIC);
        if (!cacheDir.exists()){
            cacheDir.mkdirs();
            Log.d(TAG, cacheDir.getAbsolutePath()+" -> created");
        }

        FileOutputStream fos=null;
        File file = new File(cacheDir, name+".png");

        try{
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 85, fos);
            Log.d(TAG, "saved");
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            try {
                if (fos != null) {
                    Log.d(TAG, "close fos");
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap readImageCache(Context context, String name){
        File cacheDir = new File(context.getCacheDir(), DIR_PIC);
        File file = new File(cacheDir, name+".png");
        if (!file.exists()){
            Log.d(TAG, file.getAbsolutePath()+" is not found");
            return null;
        }

        FileInputStream fis = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream( fis, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis!=null){
                try{
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }






}
