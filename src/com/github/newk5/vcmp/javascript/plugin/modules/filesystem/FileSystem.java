package com.github.newk5.vcmp.javascript.plugin.modules.filesystem;

import com.eclipsesource.v8.V8Function;
import com.github.newk5.vcmp.javascript.plugin.core.Context;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.console;
import com.github.newk5.vcmp.javascript.plugin.core.EventLoop;
import com.github.newk5.vcmp.javascript.plugin.core.common.AsyncResult;
import com.github.newk5.vcmp.javascript.plugin.core.common.CommonResult;
import io.alicorn.v8.V8JavaAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.pmw.tinylog.Logger;

public class FileSystem {

    private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private EventLoop eventLoop;

    public FileSystem(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    public static void injectClass() {
        V8JavaAdapter.injectClass(File.class, Context.v8);
    }

    public Path appendToFile(String filePath, String toAppend, V8Function callback) {
        if (callback == null) {
            try {
                Path p = Paths.get(filePath);
                if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
                    Files.createFile(p);
                }

                Files.write(p, toAppend.getBytes(), StandardOpenOption.APPEND);
                return p;

            } catch (IOException ex) {
                Logger.error(ex);
                console.error(ex.getCause().toString());
                return null;

            }

        } else {
            pool.submit(() -> {
                try {
                    Path p = Paths.get(filePath);
                    if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
                        Files.createFile(p);
                    }

                    Files.write(p, toAppend.getBytes(), StandardOpenOption.APPEND);
                    AsyncResult res = new CommonResult(callback, new Object[]{p, null});
                    eventLoop.queue.add(res);

                } catch (IOException ex) {
                    AsyncResult res = new CommonResult(callback, new Object[]{null, "Error: " + ex.getMessage()});
                    eventLoop.queue.add(res);
                    Logger.error(ex);
                    console.error(ex.getCause().toString());

                }
                return null;

            });
        }
        return null;
    }

    public String readFile(String filePath, V8Function callback) {
        if (callback == null) {
            File f = new File(filePath);
            if (f.exists() && f.isFile()) {
                try {
                    String contents = new String(Files.readAllBytes(Paths.get(filePath)));
                    return contents;
                } catch (IOException ex) {
                    Logger.error(ex);
                    console.error(ex.getCause().toString());
                    return null;

                }

            }

        } else {
            pool.submit(() -> {
                File f = new File(filePath);
                if (f.exists() && f.isFile()) {
                    try {
                        String contents = new String(Files.readAllBytes(Paths.get(filePath)));
                        AsyncResult res = new CommonResult(callback, new Object[]{contents, null});
                        eventLoop.queue.add(res);

                    } catch (IOException ex) {
                        AsyncResult res = new CommonResult(callback, new Object[]{null, "Error: " + ex.getMessage()});
                        eventLoop.queue.add(res);
                        Logger.error(ex);
                        console.error(ex.getCause().toString());
                    }

                } else {
                    AsyncResult res = new CommonResult(callback, new Object[]{null, "Error: File does not exist!"});
                    eventLoop.queue.add(res);
                    Logger.error("Error: File does not exist!");
                }
            });
        }
        return null;
    }

    public Boolean deleteFolder(String filePath, V8Function callback) {
        if (callback == null) {
            try {
                Path p = Paths.get(filePath);
                if (p.toFile().exists() && p.toFile().isDirectory()) {
                    Files.walk(p).sorted(Comparator.reverseOrder())
                            .forEach(t -> {
                                try {
                                    Files.delete(t);
                                } catch (IOException ex) {
                                    Logger.error(ex);
                                    console.error(ex.getCause().toString());
                                }
                            });
                    return true;
                }
                return false;
            } catch (IOException ex) {
                Logger.error(ex);
                console.error(ex.getCause().toString());
                return false;
            }

        } else {
            pool.submit(() -> {
                try {
                    Path p = Paths.get(filePath);
                    if (p.toFile().exists() && p.toFile().isDirectory()) {
                        Files.walk(p).sorted(Comparator.reverseOrder())
                                .forEach(t -> {
                                    try {
                                        Files.delete(t);
                                    } catch (IOException ex) {
                                        Logger.error(ex);
                                        console.error(ex.getCause().toString());
                                    }
                                });
                        AsyncResult res = new CommonResult(callback, new Object[]{null});
                        eventLoop.queue.add(res);
                    } else {
                        AsyncResult res = new CommonResult(callback, new Object[]{"Error: Failed to delete folder, " + p.getFileName().toString() + " does not exist or is not a directory!"});
                        eventLoop.queue.add(res);
                        Logger.error("Error: Failed to delete folder, " + p.getFileName().toString() + " does not exist or is not a directory!");
                    }
                } catch (IOException ex) {
                    AsyncResult res = new CommonResult(callback, new Object[]{"Error: Failed to delete folder, " + ex.getMessage()});
                    eventLoop.queue.add(res);
                    Logger.error("Error: Failed to delete folder, " + ex.getMessage());

                }

            });
        }
        return null;
    }

}
