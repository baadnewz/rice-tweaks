package com.ice.box.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.ice.box.SplashActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Adrian on 02.05.2017.
 */


public class Tools {
    /**
     * Debugging TAG
     */
    public final static String TAG = "ICEPlugin";

    /**
     * Get path of external storage
     *
     * @return path of external storage
     */
    public static String getExternalStorage() {
        String path = RootUtils.runCommand("echo ${SECONDARY_STORAGE%%:*}");
        return path.contains("/") ? path : null;
    }

    /**
     * Get path of internal storage
     *
     * @return path of internal storage
     */
    public static String getInternalStorage() {
        String dataPath = existFile("/data/media/0", true) ? "/data/media/0" : "/data/media";
        if (!new RootFile(dataPath).isEmpty()) return dataPath;
        if (existFile("/sdcard", true)) return "/sdcard";
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * Check if a file exists
     *
     * @param file   path to file
     * @param asRoot check as root
     * @return true = file exists, false = file does not exist
     */
    public static boolean existFile(String file, boolean asRoot) {
        if (RootUtils.rootAccess()) {
            if (asRoot) return new RootFile(file).exists();
            return new File(file).exists();
        } else {
            return new File(file).exists();
        }
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Write a string to any file
     *
     * @param path   path to file
     * @param text   your text
     * @param append append your text to file
     * @param asRoot write as root
     */
    public static void writeFile(String path, String text, boolean append, boolean asRoot) {
        if (asRoot) {
            new RootFile(path).write(text, append);
            return;
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(path, append);
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to write " + path);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read any file from storage
     *
     * @param file   path to the file
     * @param asRoot read as root
     * @return content of file
     */
    public static String readFile(String file, boolean asRoot) {
        if (asRoot) return new RootFile(file).readFile();
        StringBuilder s = null;
        FileReader fileReader = null;
        BufferedReader buf = null;
        try {
            fileReader = new FileReader(file);
            buf = new BufferedReader(fileReader);
            String line;
            s = new StringBuilder();
            while ((line = buf.readLine()) != null) s.append(line).append("\n");
        } catch (FileNotFoundException ignored) {
            Log.e(TAG, "File does not exist " + file);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read " + file);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s == null ? null : s.toString().trim();
    }

    /**
     * Read one line from a file, ideal for checking sysfs values
     */
    public static String readOneLine(String fname) {
        BufferedReader br;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "IO Exception when reading sys file", e);
            // attempt to do magic!
            try {
                return getStringFromFile(fname);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return line;
    }

    public static String readCommandOutput(String consolecommand) {
        try {
            Process p = Runtime.getRuntime().exec(consolecommand);
            //InputStream is = null;
            if (p.waitFor() == 0) {
                p.getInputStream();
            } else {
                p.getErrorStream();
            }
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line = br.readLine();
            br.close();
            return line;
        } catch (Exception ex) {
            return "ERROR: " + ex.getMessage();
        }
    }

    public static void runFromShell(String command) {
        StringBuilder output = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line).append("n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyAssetsFile(String filename) {
        Context context = SplashActivity.splashActivity.getApplicationContext();
        String appFileDirectory = context.getFilesDir().getPath();
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        Log.d(TAG, "Attempting to copy this file: " + filename); // + " to: " +       assetCopyDestination);
        try {
            in = assetManager.open(filename);
            Log.d(TAG, "outDir: " + appFileDirectory);
            File outFile = new File(appFileDirectory, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + filename, e);
        }
        Log.d(TAG, "Copy success: " + filename);
    }

    public static boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}