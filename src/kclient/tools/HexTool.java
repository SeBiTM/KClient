package kclient.tools;

/**
 *
 * @author SeBi
 */
public class HexTool {
    private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    public static String toHex(char c) {
        int i = c & 0xFF;
        char[] arrChar = { hexDigits[(i >> 4)], hexDigits[(i & 0xF)] };
        return String.valueOf(arrChar);
    }
    
    public static String toHexArray(String s) {
        char[] arrChar = s.toCharArray();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < arrChar.length; i++) {
            buffer.append(toHex(arrChar[i])).append(' ');
        }
        return buffer.substring(0, buffer.length() - 1);
    }
    
    public static String toString(String s) {
        char[] arrChar = s.toCharArray();
        char[] cpArr = new char[arrChar.length];
        for (int i = 0; i < arrChar.length; i++) {
            if ((arrChar[i] >= ' ') && (arrChar[i] <= '~'))
                cpArr[i] = arrChar[i];
            else
                cpArr[i] = '.';
        }
        return String.valueOf(cpArr);
    }
}
