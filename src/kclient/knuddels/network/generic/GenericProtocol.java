package kclient.knuddels.network.generic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.tools.Logger;

/**
 *
 * @author SeBi
 */
public class GenericProtocol {
    private byte[] bytaData;
    private String tree;
    private int treeIndex; 
    private String hash;
    private int nodeIndex;
    private List<String> nodeNames;
    private List<List<Integer>> nodeIndices;
    private Map<String, Object> nodeValues;
    private Map<String, Object> nodes;
       
    //<editor-fold defaultstate="collapsed" desc="Konstruktor">
    private GenericProtocol() {
        this.bytaData = null;
    }
    private GenericProtocol(int index) {
        this.bytaData = null;
        this.nodeIndex = index;
        this.nodes = new HashMap<>();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Tree">
    public static GenericProtocol parseTree(String tree) {
        GenericProtocol node = new GenericProtocol();
        node.updateTree(tree);
        return node;
    }
    
    public synchronized void updateTree(String tree) {
        reset(tree);
        this.hash = next(";");
        this.nodeIndex = nextInt(";");
        
        for (int i1 = 0; i1 < this.nodeIndex; i1++) {
            this.nodeNames.add(null);
            this.nodeIndices.add(null);
        }
        while (!isEmpty(";")) {
            this.nodeNames.add(next(";"));
        }
        for (int i1 = this.nodeIndex; i1 < this.nodeNames.size(); i1++)
            if (!this.nodeValues.containsKey(this.nodeNames.get(i1)))
                this.nodeValues.put(this.nodeNames.get(i1), i1);
        
        while (this.nodeIndices.size() < this.nodeNames.size()) {
            List<Integer> indices = new ArrayList<>();
            while (!isEmpty(";"))
                indices.add(nextInt(";"));
            this.nodeIndices.add(indices);
        }
        next(":");
        for (int i1 = this.nodeIndex; i1 < this.nodeIndices.size(); i1++) {
            List<Integer> indices = this.nodeIndices.get(i1);
            for (int t : indices) {
                if (t != 0) continue;
                Map<String, Integer> values = new HashMap<>();
                for (int k = 0; !isEmpty(";"); k++)
                    values.put(next(";"), k);
                if (this.nodeValues.containsKey(this.nodeNames.get(i1)))
                    this.nodeValues.remove(this.nodeNames.get(i1));
                this.nodeValues.put(this.nodeNames.get(i1), values);
            }
        }
    }
    private void reset(String tree) {
        this.tree = tree;
        this.treeIndex = 0;
        this.nodeNames = new ArrayList<>();
        this.nodeIndices = new ArrayList<>();
        this.nodeValues = new HashMap<>();
        this.nodes = new HashMap<>();
    }
    private boolean isEmpty(String deli) {
        if (this.tree.indexOf(deli, this.treeIndex) != this.treeIndex)
            return false;
        this.treeIndex += deli.length();
        return true;
    }
    private String next(String deli) {
        int ind = this.tree.indexOf(deli, this.treeIndex);
        if (ind >= 0) {
            String val = this.tree.substring(this.treeIndex, ind);
            this.treeIndex = ind + deli.length();
            return val;
        }
        return null;
    }
    private int nextInt(String deli) {
        return Integer.parseInt(next(deli));
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Nodes">
    public void add(String key, Object value) {
        if (this.nodes.containsKey(key))
            this.nodes.remove(key);
        this.nodes.put(key, value);
    }
    public <T> T get(String key) {
        if (!this.nodes.containsKey(key))
            return null;
        return (T) this.nodes.get(key);
    }
    
    public boolean containsKey(String key) {
        return this.nodes.containsKey(key);
    }
    public int getValue(String node, String key) {
        if (!this.nodeValues.containsKey(node))
            return -1;
        Map<String, Integer> dic = (Map<String, Integer>) this.nodeValues.get(node);
        if (dic == null || !dic.containsKey(key))
            return -1;
        return dic.get(key);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="CopyRef">
    public GenericProtocol copyRef() {
        return copyRef(nodeIndex);
    }
    public GenericProtocol copyRef(int index) {
        GenericProtocol node = new GenericProtocol(index);
        node.nodeNames = this.nodeNames;
        node.nodeIndices = this.nodeIndices;
        node.nodeValues = this.nodeValues;
        node.bytaData = this.bytaData;
        node.hash = this.hash;
        return node;
    }
    public GenericProtocol copyRef(String name) {
        if (!this.nodeValues.containsKey(name))
            return null;
        return copyRef((int)this.nodeValues.get(name));
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Read">
    public GenericProtocol read(String str, int start) {
        try {
            return read(new GenericReader(str, start));
        } catch (Exception ex) {
            Logger.get().error(ex);
        }
        return null;
    }
    public GenericProtocol read(byte[] buffer) {
        try {
            return read(new GenericReader(buffer));
        } catch (Exception ex) {
            Logger.get().error(ex);
        }
        return null;
    }
    private GenericProtocol read(GenericReader reader) throws Exception {
        int ind = reader.readShort();
        GenericProtocol node = copyRef(ind);
        return (GenericProtocol) read(reader, ind, node);
    }
    private Object read(GenericReader reader, int ind, GenericProtocol node) throws Exception {
        if (node == null)
            node = copyRef(ind);
        List<Integer> indices = node.nodeIndices.get(ind);
        for (int i = 0; i < indices.size(); i++) {
            int lnInd = indices.get(i);
            String nName;
            switch (lnInd) {
                case 0:
                    return reader.readByte();
                case 1:
                    return reader.readBoolean();
                case 2:
                    return reader.readByte();
                case 3:
                    return reader.readShort();
                case 4:
                    return reader.readInt();
                case 5:
                    return reader.readLong();
                case 6:
                    return reader.readFloat();
                case 7:
                    return reader.readDouble();
                case 8:
                    return reader.readChar();
                case 9:
                    return reader.readUTF().replace('\u20AD', 'K');
                case 10:
                    break;
                case 11:
                    i++;
                    lnInd = indices.get(i);
                    nName = nodeNames.get(lnInd);
                    List<Object> arrList = new ArrayList<>();
                    node.add(nName, arrList);
                    while (reader.readByte() == 11)
                        arrList.add(read(reader, lnInd, null));
                    i++;
                    break;
                case 12:
                    break;
                case 13:
                    return readChars(reader);
                default:
                    nName = nodeNames.get(lnInd);
                    node.add(nName, read(reader, lnInd, null));
                    break;
            }
        }
        return node;
    }
    private String readChars(GenericReader reader) throws IOException {
        int length = reader.readUnsignedByte(); // b = index 2
        if (length == 255)
            return null;

        if (length >= 128)                 
            length = length - 128 << 16 | reader.readUnsignedByte() << 8 | reader.readUnsignedByte();
        
        StringBuilder stringBuilder = new StringBuilder(length + 2); // sb = index 3
        for (int i4 = 0; i4 < length; i4++) // offset 86                
            stringBuilder.append(reader.readChar());
        return stringBuilder.toString();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Write">
    public byte[] toByteArray(GenericProtocol node) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            GenericWriter writer = new GenericWriter(buffer);
            write(writer, node);
            return buffer.toByteArray();
        } catch (IOException e) {
            Logger.get().error(e);
        }
        return null;
    }
    public String toString(GenericProtocol node) {
        try {
            StringBuilder buffer = new StringBuilder();
            GenericWriter writer = new GenericWriter(buffer);
            write(writer, node);
            return buffer.toString();
        } catch (IOException e) {
            Logger.get().error(e);
        }
        return null;
    }
    private void write(GenericWriter writer, GenericProtocol node) throws IOException {
        int ind = node.getIndex();
        if (node.bytaData != null)
            writer.write(node.bytaData);
        writer.writeShort((short) ind);
        write(writer, ind, node);
    }
    private void write(GenericWriter writer, int ind, Object node) throws IOException {
        List<Integer> indices = this.nodeIndices.get(ind);
        for (int i = 0; i < indices.size(); i++) {
            int i6 = indices.get(i);
            switch (i6) {
                case 0:
                    writer.writeByte((byte) node);
                    break;
                case 1:
                    writer.writeBoolean((boolean) node);
                    break;
                case 2:
                    writer.writeByte((byte) node);
                    break;
                case 3:
                    writer.writeShort((short) node);
                    break;
                case 4:
                    writer.writeInt((int) node);
                    break;
                case 5:
                    writer.writeLong((long) node);
                    break;
                case 6:
                    writer.writeFloat((float) node);
                    break;
                case 7:
                    writer.writeDouble((double) node);
                    break;
                case 8:
                    writer.writeChar((char) node);
                    break;
                case 9:
                    writer.writeUTF((String) node);
                    break;
                case 10:
                    break;
                case 11:
                    i++;
                    i6 = indices.get(i);
                    String nName = nodeNames.get(i6);
                    ArrayList arrList = ((GenericProtocol) node).get(nName);
                    if (arrList == null) {
                        writer.writeByte(12);
                        i++;
                        break;
                    }
                    for (Object obj : arrList) {
                        writer.writeByte(11);
                        write(writer, i6, obj);
                    }
                    writer.writeByte(12);
                    i++;
                    break;
                case 12:
                    break;
                case 13:
                    writeChars((String) node, writer);
                    break;
                default:
                    write(writer, i6, ((GenericProtocol) node).get(nodeNames.get(i6)));
                    break;
            }
        }
    }
    private void writeChars(String str, GenericWriter writer) throws IOException {
        if (str == null) {
            writer.writeInt(255);
            return;
        }
        int length = str.length();
        if (length >= 128) {
            writer.writeByte(length >>> 16 | 0x80);
            writer.writeByte(length >>> 8 & 0xFF);
            writer.writeByte(length & 0xFF);
        }
        writer.writeByte(length);
        if (length > 0) {
            writer.writeChars(str);
        }
    }
    //</editor-fold>

    public void setByteData(byte[] data) {
        this.bytaData = data;
    }
    
    @Override
    public String toString() {
        StringBuilder elements = new StringBuilder();
        for (Map.Entry<String, Object> entry : this.nodes.entrySet())
            elements.append(entry.getKey()).append(" = ").append(entry.getValue()).append(",");
        if (!this.nodes.isEmpty())
            elements.delete(elements.length() - 1, elements.length());
        return String.format("[GenericProtocol - Name: %s, Index: %s, Size: %d, Elements: { %s }]",
                getName(),
                getIndex(),
                getSize(),
                elements.toString());
    }

    public static String printNode(GenericProtocol node) {
        StringBuilder buffer = new StringBuilder();
        printNode(node, buffer);
        return buffer.toString();
    }
    public static void printNode(GenericProtocol node, StringBuilder buffer) {
        buffer.append("[").append(node.getName()).append(" - Module] ( ");
        for(Map.Entry<String, Object> entry : node.nodes.entrySet()) {
            if (entry == null || entry.getValue() == null) {
            } else if (entry.getValue().getClass().equals(GenericProtocol.class)) {
                printNode((GenericProtocol)entry.getValue(), buffer);
            } else if (entry.getValue().getClass().equals(ArrayList.class)) {
                StringBuilder sb = new StringBuilder();
                for (Object o : (ArrayList)entry.getValue())
                    if (o.getClass().equals(GenericProtocol.class))
                        printNode((GenericProtocol)o, sb);
                    else
                        sb.append("[").append(o).append("] ");
                buffer.append("[").append(entry.getKey()).append(" - ArrayList] => ( ").append(sb.toString()).append(")  ");
            } else {
                String t = entry.getValue().getClass().getName();
                t = t.substring(t.lastIndexOf(".") + 1);
                buffer.append("[").append(entry.getKey()).append(" - ").append(t).append("] => ").append(entry.getValue()).append(" ");
            }
        }
        buffer.append(") ");
    }
    
    public GenericProtocol getNode(String k) {
        return (GenericProtocol) get(k);
    }
    
    public String getName() {
        return this.nodeNames.get(this.nodeIndex);
    }
    public int getIndex() {
        return this.nodeIndex;
    }
    public int getSize() {
        return this.nodes.size();
    }
    public Map<String, Object> getNodes() {
        return this.nodes;
    }
    
    public boolean equalsName(String name) {
        return this.getName().equals(name);
    }
}
