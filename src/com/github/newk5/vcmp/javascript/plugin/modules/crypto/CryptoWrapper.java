package com.github.newk5.vcmp.javascript.plugin.modules.crypto;

import com.eclipsesource.v8.V8Function;
import static com.github.newk5.vcmp.javascript.plugin.core.Context.console;
import com.github.newk5.vcmp.javascript.plugin.core.EventLoop;
import com.github.newk5.vcmp.javascript.plugin.core.common.AsyncResult;
import com.github.newk5.vcmp.javascript.plugin.core.common.CommonResult;
import com.google.common.hash.Hashing;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import org.pmw.tinylog.Logger;

public class CryptoWrapper {

    private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private EventLoop eventLoop;

    public CryptoWrapper(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    public String SHA512(String s, V8Function callback) {
        try {
            if (callback == null) {
                return Hashing.sha512().hashString(s, StandardCharsets.UTF_8).toString();
            }
            pool.submit(() -> {
                String val = Hashing.sha512().hashString(s, StandardCharsets.UTF_8).toString();
                AsyncResult res = new CommonResult(callback, new Object[]{val});
                eventLoop.queue.add(res);
            });

        } catch (Exception e) {
            console.error(e.getCause().toString());
            Logger.error(e);
        }
        return null;
    }

    public String SHA256(String s, V8Function callback) {
        try {
            if (callback == null) {
                return Hashing.sha256().hashString(s, StandardCharsets.UTF_8).toString();
            }

            pool.submit(() -> {
                String val = Hashing.sha256().hashString(s, StandardCharsets.UTF_8).toString();
                AsyncResult res = new CommonResult(callback, new Object[]{val});
                eventLoop.queue.add(res);
            });
        } catch (Exception e) {
            console.error(e.getCause().toString());
            Logger.error(e);
        }
        return null;
    }

    public String SHA1(String s, V8Function callback) {
        try {
            if (callback == null) {
                return Hashing.sha1().hashString(s, StandardCharsets.UTF_8).toString();
            }

            pool.submit(() -> {
                String val = Hashing.sha1().hashString(s, StandardCharsets.UTF_8).toString();
                AsyncResult res = new CommonResult(callback, new Object[]{val});
                eventLoop.queue.add(res);
            });
        } catch (Exception e) {
            console.error(e.getCause().toString());
            Logger.error(e);
        }
        return null;
    }

    public String MD5(String s, V8Function callback) {
        try {
            if (callback == null) {
                return Hashing.md5().hashString(s, StandardCharsets.UTF_8).toString();
            }
            pool.submit(() -> {
                String val = Hashing.md5().hashString(s, StandardCharsets.UTF_8).toString();
                AsyncResult res = new CommonResult(callback, new Object[]{val});
                eventLoop.queue.add(res);
            });
        } catch (Exception e) {
            console.error(e.getCause().toString());
            Logger.error(e);
        }
        return null;
    }

    private String sha384(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("SHA-384");
        byte[] digest = md5.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; ++i) {
            sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public String SHA384(String input, V8Function callback) {
        if (callback == null) {
            try {
                return sha384(input);
            } catch (Exception ex) {
                Logger.error(ex);
                console.error(ex.getCause().toString());
            }
        } else {
            pool.submit(() -> {
                String val;
                try {
                    val = sha384(input);
                    AsyncResult res = new CommonResult(callback, new Object[]{val});
                    eventLoop.queue.add(res);
                } catch (Exception ex) {
                    console.error(ex.getCause().toString());
                    Logger.error(ex);
                }

            });
        }
        return null;
    }

    private String sha224(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("SHA-224");
        byte[] digest = md5.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; ++i) {
            sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public String SHA224(String input, V8Function callback) {
        if (callback == null) {
            try {
                return sha224(input);
            } catch (Exception ex) {
                Logger.error(ex);
                console.error(ex.getCause().toString());
            }
        } else {
            pool.submit(() -> {
                String val;
                try {
                    val = sha224(input);
                    AsyncResult res = new CommonResult(callback, new Object[]{val});
                    eventLoop.queue.add(res);
                } catch (Exception ex) {
                    console.error(ex.getCause().toString());
                    Logger.error(ex);
                }

            });
        }
        return null;
    }

    public String encodeBase64(String originalInput, V8Function callback) {
        try {
            if (callback == null) {
                String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
                return encodedString;
            }

            pool.submit(() -> {
                String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
                AsyncResult res = new CommonResult(callback, new Object[]{encodedString});
                eventLoop.queue.add(res);
            });

        } catch (Exception e) {
            Logger.error(e);
            console.error(e.getCause().toString());
        }
        return null;
    }

    public String decodeBase64(String encodedString, V8Function callback) {
        try {
            if (callback == null) {
                byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
                String decodedString = new String(decodedBytes);
                return decodedString;
            }

            pool.submit(() -> {
                byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
                String decodedString = new String(decodedBytes);
                AsyncResult res = new CommonResult(callback, new Object[]{encodedString});
                eventLoop.queue.add(res);
            });
        } catch (Exception e) {
            Logger.error(e);
            console.error(e.getCause().toString());
        }
        return null;
    }

}
