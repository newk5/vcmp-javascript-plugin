package com.github.newk5.vcmp.javascript.plugin.resources;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.github.newk5.vcmp.javascript.plugin.output.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.pmw.tinylog.Logger;

public class FileLoader implements JavaCallback {

    public static ConcurrentHashMap<String, String> files = new ConcurrentHashMap<>();
    public String rootDir = "";
    private String beforeRetreat = null;
    private Console console;

    public FileLoader(Console console) {
        this.console = console;
    }

    @Override
    public Object invoke(V8Object vo, V8Array va) {
        try {
            String dir = va.getString(0).trim();
            String root = vo.getRuntime().getString("__dirname");

            if (beforeRetreat != null) {
                root = beforeRetreat;
            }
            if (dir.startsWith("../")) {
                beforeRetreat = root;
                dir = dir.replaceFirst(".", "");
                root = new File(root).getParent();
            } else {
                beforeRetreat = null;
            }
            File f = new File(root + dir);

            if (!dir.endsWith(".js")) {
                console.error("ERROR - Invalid File/Directory: " + dir);
                Logger.error("ERROR - Invalid File/Directory: " + dir);
            } else if (f.exists() && f.isFile()) {

                String src = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
                files.put(f.getName(), f.getAbsolutePath());
                String upperPath = trimDots(f);
                vo.getRuntime().add("__dirname", upperPath);
                vo.getRuntime().executeVoidScript(src, f.getName(), 0);

            } else {

                String upperPath = trimDots(f);
                String path = checkParentPath(upperPath, dir);
                if (path == null) {
                    console.error("ERROR - File not found " + dir);
                    Logger.error("ERROR - File not found " + dir);

                } else {
                    String src = new String(Files.readAllBytes(Paths.get(path)));
                    vo.getRuntime().add("__dirname", new File(path).getParent());
                    vo.getRuntime().executeVoidScript(src, dir, 0);
                }

            }
            vo.release();
            va.release();
        } catch (IOException e) {
            Logger.error(e);
        }
        return null;
    }

    private String checkParentPath(String path, String file) {
        File f = new File(path + file);
        if (f.exists() && f.isFile()) {
            return f.getAbsolutePath();
        }
        if (rootDir.startsWith(path)) {
            return null;
        } else {
            String upperPath = "";
            File dir = new File(path);
            if (dir.getParent().endsWith(".")) {
                upperPath = removeLastChar(dir.getParent());
            } else {
                upperPath = dir.getParent();
            }
            return checkParentPath(upperPath, file);
        }
    }

    private String removeLastChar(String s) {

        return Optional.ofNullable(s)
                .filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 1))
                .orElse(s);
    }

    private String trimDots(File f) {
        String upperPath = "";

        if (f.getParent().endsWith(".")) {
            upperPath = removeLastChar(f.getParent());
        } else {
            upperPath = f.getParent();
        }
        return upperPath;
    }
}
