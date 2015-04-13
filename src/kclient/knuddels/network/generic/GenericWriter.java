/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kclient.knuddels.network.generic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author SeBi
 */
public class GenericWriter {
    private final StringBuilder buffer;
    private final ByteArrayOutputStream byteBuffer;
    
    public GenericWriter(StringBuilder buffer) {
        this.buffer = buffer;
        this.byteBuffer = null;
    }
    public GenericWriter(ByteArrayOutputStream buffer) {
        this.byteBuffer = buffer;
        this.buffer = null;
    }
    
    public void write(byte[] arr) {
        write(arr, 0, arr.length);
    }
    
    public void write(byte[] arr, int start, int length) {
        for (int i = 0; i < length; i++) {
            write(arr[i + start]);
        }
    }
    
    public void write(int i) {
        if (this.buffer != null)
            this.buffer.append(i & 0xFF);
        if (this.byteBuffer != null)
            this.byteBuffer.write(i & 0xFF);
    }
    
    public void writeByte(int b) {
        if (this.buffer != null)
            this.buffer.append((char) (b & 0xff));
        if (this.byteBuffer != null)
            this.byteBuffer.write((char) (b & 0xff));
    }
    
    public void writeBytes(String s) {
        write(s.getBytes());
    }
    
    public void writeBoolean(boolean v) {
        write(v ? 1 : 0);
    }
    
    public void writeChar(int c) {
        buffer.append((char) c);
    }
    
    public void writeChars(String s) {
        buffer.append(s);
    }
    
    public void writeDouble(double d) {
        writeLong(Double.doubleToLongBits(d));
    }
    
    public void writeFloat(float f) {
        writeInt(Float.floatToIntBits(f));
    }
    
    public void writeInt(int i ) {
        writeByte(i >>> 24);
        writeByte(i >>> 16);
        writeByte(i >>> 8);
        writeByte(i);
    }
    
    public void writeLong(long l) {
        writeByte((int) (l >>> 56));
        writeByte((int) (l >>> 48));
        writeByte((int) (l >>> 40));
        writeByte((int) (l >>> 32));
        writeByte((int) (l >>> 24));
        writeByte((int) (l >>> 16));
        writeByte((int) (l >>> 8));
        writeByte((int) l);
    }
    
    public void writeShort(short s) {
        writeByte(s >>> 8);
        writeByte(s);
    }
    
    public void writeUTF(String s) throws IOException {
        if (s == null)
            s = "";
        int strlen = s.length();
        int utflen = 0;
        int c;
        
        for (int i = 0; i < strlen; i++) {
            c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        
        if (utflen > 65535)
            throw new IOException("encoded string too long: " + utflen + " bytes");
        writeShort((short) utflen);
        int i1 = 0;
        for (; i1 < strlen; i1++) {
            c = s.charAt(i1);
            if (!((c >= 0x0001) && (c <= 0x007F)))
                break;
            writeByte((byte) c);
        }
        for (; i1 < strlen; i1++) {
            c = s.charAt(i1);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                writeByte((byte) c);
            } else if (c > 0x07FF) {
                writeByte((byte) (0xe0 | ((c >>> 12) & 0xf)));
                writeByte((byte) (0x80 | ((c >>> 6) & 0x3f)));
                writeByte((byte) (0x80 | c & 0x3f));
            } else {
                writeByte((byte) (0xc0 | ((c >>> 6) & 0x1f)));
                writeByte((byte) (0x80 | ((c >>> 0) & 0x3f)));
            }
        }
    }
}
