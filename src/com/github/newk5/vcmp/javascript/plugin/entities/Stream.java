package com.github.newk5.vcmp.javascript.plugin.entities;

import com.google.common.io.LittleEndianDataOutputStream;
import com.maxorator.vcmp.java.plugin.integration.player.Player;
import com.maxorator.vcmp.java.plugin.integration.server.Server;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Stream {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final LittleEndianDataOutputStream dataOutputStream;

    public Stream(int code) throws IOException {
        byteArrayOutputStream = new ByteArrayOutputStream();
        dataOutputStream = new LittleEndianDataOutputStream(byteArrayOutputStream);

        writeInt(code);
    }

    public void writeString(String data) throws IOException {
        dataOutputStream.writeUTF(data);
    }

    public void writeByte(int data) throws IOException { 
        dataOutputStream.write(data);
    }

    public void writeInt(int data) throws IOException {
        dataOutputStream.writeInt(data);
    }

    public void writeFloat(float data) throws IOException {
        dataOutputStream.writeFloat(data);
    }

    public void send(Server server, Player player) throws IOException {
        dataOutputStream.close();

        byte[] bytes = byteArrayOutputStream.toByteArray();
        server.sendScriptData(player, bytes);
    }
}
