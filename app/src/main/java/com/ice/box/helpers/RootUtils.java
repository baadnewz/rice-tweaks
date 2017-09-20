package com.ice.box.helpers;

import android.content.Context;
import android.util.Log;

import com.ice.box.SplashActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import static com.ice.box.helpers.Constants.DEBUGTAG;

/**
 * Created by Adrian on 02.05.2017.
 */


public class RootUtils {
    private static SU su;


    public static boolean isRootPresent() {
        return existBinary("su");
    }

    public static boolean rootAccess() {
        SU su = getSU();
        su.runCommand("echo /testRoot/");
        return !su.denied;
    }

    public static boolean isRootGranted() {
        if (isRootPresent()) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = in.readLine();
                if (output != null && output.toLowerCase().contains("uid=0"))
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (process != null)
                    process.destroy();
            }
        }
        return false;
    }

    public static boolean busyboxInstalled() {
        return existBinary("busybox");// || existBinary("toybox");
    }

    private static boolean existBinary(String binary) {
        for (String path : System.getenv("PATH").split(":")) {
            if (!path.endsWith("/")) path += "/";
            if (new File(path + binary).exists() || Tools.existFile(path + binary, true))
                return true;
        }
        return false;
    }

/*    public static void mountSystemRW(boolean bool) {
        runCommand(bool ? TweaksSplash.BUSYBOX_PATH + " mount -o rw,remount " + TweaksSplash.ANDROID_SYSTEM_PATH*//* + ";sleep 1"*//* :
                TweaksSplash.BUSYBOX_PATH + " mount -o ro,remount " + TweaksSplash.ANDROID_SYSTEM_PATH*//* + ";sleep 1"*//*);
    }*/

    public static void mountSystemRW(boolean bool) {
        runCommand(bool ? "mount -o rw,remount /system"/* + ";sleep 1"*/ :
                "mount -o rw,remount /system"/* + ";sleep 1"*/);
    }

    public static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }


    public static void closeSU() {
        if (su != null) su.close();
        su = null;
    }


    public static String runCommand(String command) {
        if (!RootUtils.rootAccess()) {
            return "";
        } else {
            return getSU().runCommand(command);
        }
    }

    private static SU getSU() {
        if (su == null) su = new SU();
        else if (su.closed || su.denied) su = new SU();
        return su;
    }


    public static class SU {
        private final boolean root;
        private Process process;
        private BufferedWriter bufferedWriter;
        private BufferedReader bufferedReader;
        private boolean closed;
        private boolean denied;
        private boolean firstTry;

        public SU() {
            this(true);
        }

        public SU(boolean root) {
            this.root = root;
            try {
                Log.i(Tools.TAG, root ? "SU initialized" : "SH initialized");
                firstTry = true;
                process = Runtime.getRuntime().exec(root ? "su" : "sh");
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            } catch (IOException e) {
                Log.e(Tools.TAG, root ? "Failed to run shell as su" : "Failed to run shell as sh");
                denied = true;
                closed = true;
            }
        }

        public synchronized String runCommand(final String command) {
            try {
                StringBuilder sb = new StringBuilder();
                String callback = "/shellCallback/";
                bufferedWriter.write(command + "\necho " + callback + "\n");
                bufferedWriter.flush();
                int i;
                char[] buffer = new char[256];
                while (true) {
                    sb.append(buffer, 0, bufferedReader.read(buffer));
                    if ((i = sb.indexOf(callback)) > -1) {
                        sb.delete(i, i + callback.length());
                        break;
                    }
                }
                firstTry = false;
                return sb.toString().trim();
            } catch (IOException e) {
                closed = true;
                e.printStackTrace();
                if (firstTry) denied = true;
            } catch (ArrayIndexOutOfBoundsException e) {
                denied = true;
            } catch (Exception e) {
                e.printStackTrace();
                denied = true;
            }
            return null;
        }

        public void close() {
            try {
                bufferedWriter.write("exit\n");
                bufferedWriter.flush();
                process.waitFor();
                Log.i(Tools.TAG, root ? "SU closed: " + process.exitValue() : "SH closed: " + process.exitValue());
                closed = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void enforcePermissionAsRoot(String permission, Context context) {
        runCommand("pm grant " + context.getPackageName() + " " + permission);
    }

    //Scripts functions
    public static void setProp(Context context, String prop, String value) {
        File set_build_prop = new File(context.getFilesDir().getPath() + "/set_build_prop.sh");
        mountSystemRW(true);
        RootUtils.runCommand("chmod 755 " + set_build_prop);
        RootUtils.runCommand("sh " + set_build_prop + " " + prop + " " + value);
        mountSystemRW(false);

    }

    public static void backupColors(Context context, String databasePath, String filePath) {
        File colorsBackup = new File(context.getFilesDir().getPath() + "/backup_colors.sh");
        mountSystemRW(true);
        RootUtils.runCommand("chmod 755 " + colorsBackup);
        RootUtils.runCommand("sh " + colorsBackup + " " + databasePath + " > " + filePath);
        mountSystemRW(false);

    }

    public static void restoreColors(Context context, String filePath) {
        File colorsRestore = new File(context.getFilesDir().getPath() + "/restore_colors.sh");
        mountSystemRW(true);
        RootUtils.runCommand("chmod 755 " + colorsRestore);
        RootUtils.runCommand("sh " + colorsRestore + " " + filePath);
        mountSystemRW(false);

    }


}