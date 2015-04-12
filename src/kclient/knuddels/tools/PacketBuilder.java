package kclient.knuddels.tools;

/**
 *
 * @author SeBi
 */
public class PacketBuilder {
    private StringBuilder buffer;
    
    public PacketBuilder() {
        this("");
    }
    public PacketBuilder(String opcode) {
        this.buffer = new StringBuilder(opcode);
    }
    
    public void writeChars(char[] v) {
        for (char c : v)
            this.write(c);
    }
    public void writeInt(int v) {
        this.buffer.append(v);
    }
    public void write(int v) {
        this.buffer.append((char)v);
    }
    public void write(int[] v) {
        for (int i : v)
            write(i);
    }
    public void write(byte[] v) {
        for (byte b : v)
            write(b);
    }
    public void writeNull() {
        this.write(0x00);
    }
    public void writeShort(int v) {
        write((v >> 8) & 0xFF);
        write(v & 0xFF);
    }
    public void writeString(String v) {
        this.writeChars(v.toCharArray());
    }
    
    @Override
    public String toString() {
        return this.buffer.toString();
    }
}
