package com.ice.box.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian on 02.05.2017.
 */


public class RootFile {
    private final String file;

    public RootFile(String file) {
        this.file = file;
    }

    public String getName() {
        return RootUtils.runCommand("basename '" + file + "'");
    }

    public void mkdir() {
        RootUtils.runCommand("mkdir -p '" + file + "'");
    }

    public void mv(String newPath) {
        RootUtils.runCommand("mv -f '" + file + "' '" + newPath + "'");
    }

    public void write(String text, boolean append) {
        String[] textarray = text.split("\\r?\\n");
        RootUtils.runCommand(append ? "echo '" + textarray[0] + "' >> " + file : "echo '" + textarray[0] + "' > " + file);
        if (textarray.length > 1) for (int i = 1; i < textarray.length; i++)
            RootUtils.runCommand("echo '" + textarray[i] + "' >> " + file);
    }

    public void delete() {
        RootUtils.runCommand("rm -r '" + file + "'");
    }

    public List<String> list() {
        List<String> list = new ArrayList<>();
        String files = RootUtils.runCommand("ls '" + file + "'");
        if (files != null)
            // Make sure the file exists
            for (String file : files.split("\\r?\\n"))
                if (file != null && !file.isEmpty() && Tools.existFile(this.file + "/" + file, true))
                    list.add(file);
        return list;
    }

    public List<RootFile> listFiles() {
        List<RootFile> list = new ArrayList<>();
        String files = RootUtils.runCommand("ls '" + file + "'");
        if (files != null)
            // Make sure the file exists
            for (String file : files.split("\\r?\\n"))
                if (file != null && !file.isEmpty() && Tools.existFile(this.file + "/" + file, true))
                    list.add(new RootFile(this.file + "/" + file));
        return list;
    }

    public float length() {
        try {
            return Float.parseFloat(RootUtils.runCommand("du '" + file + "'").split(file)[0].trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    public String getParent() {
        return RootUtils.runCommand("dirname '" + file + "'");
    }

    public boolean isEmpty() {
        return RootUtils.runCommand("find '" + file + "' -mindepth 1 | read || echo false").equals("false");
    }

    public boolean exists() {
        String output = RootUtils.runCommand("[ -e '" + file + "' ] && echo true");
        return output != null && output.contains("true");
    }

    public String readFile() {
        return RootUtils.runCommand("cat '" + file + "'");
    }

    public String toString() {
        return file;
    }
}
