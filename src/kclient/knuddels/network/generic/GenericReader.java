package kclient.knuddels.network.generic;

import java.io.IOException;

/**
 *
 * @author SeBi
 */
public class GenericReader {
    private String strData;
    private byte[] byteData;
    private int offset;
    private final int length;
    
    public GenericReader(String packet, int start) {
        this.offset = start;
        this.strData = packet;
        this.length = packet.length();
    }
    public GenericReader(byte[] buffer) {
        this.offset = 0;
        this.byteData = buffer;
        this.length = buffer.length;
    }
    
    public int read() {
        return offset != length ? readByte() & 0xFF : -1;
    }
    public byte readByte() {
        return byteData != null ? byteData[offset++] : (byte)strData.charAt(offset++);
    }
    public char readChar() {
        return byteData != null ? (char)readShort() : strData.charAt(offset++);
    }
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }
    public boolean readBoolean() {
        return this.readByte() != 0;
    }
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }
    public void readFully(byte[] buffer, int start, int length) {
        for (int i = 0; i < length; i++)
            buffer[i + start] = readByte();
    }
    public int readInt() {
        int l = readByte() & 0xFF;
        int i1 = readByte() & 0xFF;
        int j1 = readByte() & 0xFF;
        int k1 = readByte() & 0xFF;
        return (l << 24) + (i1 << 16) + (j1 << 8) + k1;
    }
    public long readLong() {
        int i1 = readByte() & 0xFF;
        int i2 = readByte() & 0xFF;
        int i3 = readByte() & 0xFF;
        int i4 = readByte() & 0xFF;
        int i5 = readByte() & 0xFF;
        int i6 = readByte() & 0xFF;
        int i7 = readByte() & 0xFF;
        int i8 = readByte() & 0xFF;
        return (long)(i1 << 56) + 
                (long)(i2 << 48) + 
                (long)(i3 << 40) + 
                (long)(i4 << 32) + 
                (long)(i5 << 24) + 
                (long)(i6 << 16) + 
                (long)(i7 << 8) + 
                (long)i8;
    }
    public short readShort() {
        return (short)(
                (short)((readByte() & 0xFF) << 8) + 
                (short)(readByte() & 0xFF));
    }
    public String readUTF() throws IOException {
        int utfLength = readUnsignedShort();
        byte[] byteArray = new byte[utfLength];
        char[] charArray = new char[utfLength];
        int c;
        int count = 0;
        int chararrCount = 0;
        readFully(byteArray, 0, utfLength);
        while (count < utfLength) {
            c = byteArray[count] & 0xff;
            if (c > 127)
                break;
            count++;
            charArray[chararrCount++] = (char) c;
        }
        while (count < utfLength) {
            c = byteArray[count] & 0xff;
            switch (c >> 4)
            {
                case 0:case 1:case 2:case 3:case 4:case 5:case 6:
                case 7:
                    count++;
                    charArray[chararrCount++] = (char) c;
                    break;
                case 12:
                case 13:
                    count += 2;
                    if (count > utfLength)
                        throw new IOException("[Error] malformed input: partial character at end");
                    int char2 = byteArray[count - 1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new IOException("[Error]  malformed input around byte " + count);
                    charArray[chararrCount++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    count += 3;
                    if (count > utfLength)
                        throw new IOException("[Error] malformed input: partial character at end");
                    char2 = byteArray[count - 2];
                    int char3 = byteArray[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new IOException("[Error] malformed input around byte " + (count - 1));
                    charArray[chararrCount++] = (char) (((c & 0x0F) << 12) |
                                                      ((char2 & 0x3F) << 6) |
                                                      ((char3 & 0x3F) << 0));
                    break;
                default:
                    throw new IOException("[Error] malformed input around byte " + count);
            }
        }
        return new String(charArray);
    }
    public int readUnsignedByte() {
        return read();
    }
    public int readUnsignedShort() {
        return readShort() & 0xffff;
    }
}
