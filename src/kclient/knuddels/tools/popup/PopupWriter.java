package kclient.knuddels.tools.popup;

import kclient.knuddels.tools.PacketBuilder;

/**
 *
 * @author SeBi
 */
public class PopupWriter extends PacketBuilder {
    public PopupWriter(String opcode) {
        super(opcode);
    }
    
    public void writeSize(int size) {
        write('A' + size);
    }

    public void writePopupString(String str) {
        writeString(str);
	write(0xF5);
    }

    public void writeFontStyle(int weight, int size) {
        if (weight != 'p') {
            write(weight);
	}

	write('g');
	writeSize(size);
    }

    public void writeLayout(int layout) {
        write(layout);
    }

    public void writeFrameSize(int width, int height) {
        write('s');
	writeShort(width);
	writeShort(height);
    }

    public void writeForeground(int[] color) {
        write('f');
	write(color);
    }
	
    public void writeBackground(int[] color) {
        write('h');
	write(color);
    }

    public void writeBackgroundImage(String image, int position) {
	write('i');
	writePopupString(image);
	writeShort(position);
    }
    
    public void writeEnd() {
        write(0xE3);
    }
}
