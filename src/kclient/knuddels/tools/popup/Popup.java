package kclient.knuddels.tools.popup;

import java.util.ArrayList;
import java.util.List;
import kclient.knuddels.tools.popup.components.Button;
import kclient.knuddels.tools.popup.components.Checkbox;
import kclient.knuddels.tools.popup.components.Choice;
import kclient.knuddels.tools.popup.components.Label;
import kclient.knuddels.tools.popup.components.Panel;
import kclient.knuddels.tools.popup.components.TextArea;
import kclient.knuddels.tools.popup.components.TextField;
import kclient.knuddels.tools.popup.components.TextPanel;
import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;
import kclient.knuddels.tools.popup.tools.layout.BorderLayout;
import kclient.knuddels.tools.popup.tools.layout.FlowLayout;
import kclient.knuddels.tools.popup.tools.layout.GridLayout;

/**
 * 
 * @author SeBi
 */
public class Popup {
    private String title;
    private int width, height;
    private List<Component> components;
    private String opcode, parameter;
    private int[] background;

    public Popup(String title, int width, int height) {
        this.title = title;
	this.width = width;
	this.height = height;
	this.components = new ArrayList<>();
        this.background = new int[] { 0xBE, 0xBC, 0xFB };
    }

    public void addComponent(Component com) {
        this.components.add(com);
    }
    public void setOpcode(String opcode, String parameter) {
        this.opcode = opcode;
	this.parameter = parameter;
    }

    public void setBackground(int[] v) {
        this.background = v;
    }
    
    @Override
    public String toString() {
	PopupWriter buffer = new PopupWriter("k");
        buffer.writeNull();
	buffer.writePopupString(this.title);
        if (this.opcode != null) {
            buffer.write('s');
            buffer.writePopupString(this.opcode);
            buffer.writePopupString(this.parameter);
	}
        if (this.width > 0 && this.height > 0) {
            buffer.write('w');
            buffer.writeShort(this.width);
            buffer.writeShort(this.height);
        }

	buffer.writeForeground(new int[] { 0x00, 0x00, 0x00 });
	buffer.writeBackground(this.background);
	buffer.writeEnd();

        for (Component com : this.components)
            writeComponent(com, buffer);
	buffer.writeEnd();
	return buffer.toString();
    }
    private static void writeComponent(Component com, PopupWriter buffer) {
        if (com.getLocation() != Location.NONE)
            buffer.write(com.getLocation().getValue());
        buffer.write(com.getType().getValue());
        
        switch (com.getType()) {
            case LABEL:
                buffer.writePopupString(com.getText());
                buffer.writeFontStyle(((Label)com).getStyle(), ((Label)com).getSize());
                break;
            case PANEL:
                Panel panel = (Panel)com;
                if (panel.getBackgroundImage() != null) {
                    buffer.write('U');
                    buffer.writePopupString(panel.getBackgroundImage());
                    buffer.write('U');
                    buffer.writeShort(panel.getWidth());
                    buffer.writeShort(panel.getHeight());
                }
                buffer.writeLayout(panel.getLayout().getType().getValue());
                switch (panel.getLayout().getType()) {
                    case GRID_LAYOUT:
                        GridLayout grid = (GridLayout)panel.getLayout();
                        buffer.writeSize(grid.getRows());
                        buffer.writeSize(grid.getCols());
                        buffer.writeSize(grid.getHGap());
                        buffer.writeSize(grid.getVGap());
                        break;
                }
                
                List<Component> coms = panel.getComponents();
                for (Component c : coms)
                    writeComponent(c, buffer);
                break;
            case TEXT_PANEL:
                TextPanel tp = (TextPanel)com;
                buffer.writePopupString(com.getText());
                buffer.writeFrameSize(tp.getWidth(), tp.getHeight());
                buffer.writeBackgroundImage(tp.getBackgroundImage(), tp.getPosition());
                break;
            case BUTTON:
                buffer.writePopupString(com.getText());
                buffer.writeFontStyle('b', 16);
                Button button = (Button)com;
                
                if (button.isStyled()) {
                    buffer.write('c');
                    if (button.isColored())
                        buffer.write('e');
		}

		if (button.isClose())
                    buffer.write('d');
		if (button.isAction())
                    buffer.write('s');
		if (button.getCommand() != null) {
                    buffer.write('u');
                    buffer.writePopupString(button.getCommand());
                }
                break;
            case TEXT_FIELD:
                buffer.writePopupString(com.getText());
                buffer.writeSize(((TextField)com).getWidth());
                break;
            case TEXT_AREA:
                buffer.writePopupString(com.getText());
                TextArea textarea = (TextArea)com;
		buffer.writeSize(textarea.getRows());
		buffer.writeSize(textarea.getColumns());

		switch (textarea.getScrollbars()) {
                    case 0:
                        buffer.write('b');
			break;
                    case 1:
			buffer.write('s');
			break;
                    case 2:
			buffer.write('w');
			break;
		}
                if (textarea.isEditable())
                    buffer.write('e');
                break;
            case CHECKBOX:
                if (com.getText() != null) {
                    buffer.write('l');
                    buffer.writePopupString(com.getText());
		}

		buffer.writeFontStyle('p', 16);
		Checkbox checkbox = (Checkbox)com;

		if (checkbox.isDisabled())
                    buffer.write('d');
		
		if (checkbox.isChecked()) {
                    buffer.write('s');
                    buffer.write('t');
		}

		if (checkbox.getGroup() != 0) {
                    buffer.write('r');
                    buffer.writeSize(checkbox.getGroup());
		}
                break;
            case CHOICE:
                Choice choice = (Choice)com;
                if(choice.useIndex()) {
                    buffer.write('c');
                    buffer.write(choice.getSelectedIndex());
                } else {
                    buffer.write('C');
                    buffer.writePopupString(choice.getSelected());
                }

                if(choice.getFontsize() > 0)
                    buffer.writeFontStyle('p', choice.getFontsize());

                if(choice.isDisabled())
                    buffer.write('d');

                buffer.writeForeground(com.getForeground());
                buffer.writeBackground(com.getBackground());

                buffer.writeEnd();
                for(String item : choice.getItems()) {
                    buffer.writePopupString(item);
                }
                break;
        }
        if (com.getType() != ComponentType.PANEL && com.getType() != ComponentType.CHOICE) {
            buffer.writeBackground(com.getBackground());
            buffer.writeForeground(com.getForeground());
        }
            
        buffer.writeEnd();
    }

    public static String create(String title, String subtitle, String message, int width, int height, boolean btn) {
	Popup popup = new Popup(title, width, height);
	
        Panel contentPanel = new Panel(new BorderLayout(), Location.CENTER);
        contentPanel.addComponent(new TextPanel(
                        message, width - 10, height - 10, 
                        Location.CENTER
                )); //KCode
        
        if (btn) {
            Panel buttonPanel = new Panel(new FlowLayout(), Location.SOUTH);
            buttonPanel.addComponent(new Button("   OK   "));

            addOldStyle(popup, subtitle, contentPanel, buttonPanel);
        } else {
            addOldStyle(popup, subtitle, contentPanel, null);
        }
        
	return popup.toString();
    }
    public static void addOldStyle(Popup popup, String subtitle, Panel contentPanel, Panel buttonPanel) {
        Label paddingRight = new Label("         ", Location.EAST, 5); //Padding Right
        popup.addComponent(paddingRight);
        
        Label paddingLeft = new Label("         ", Location.WEST, 5); //PAdding Left
        popup.addComponent(paddingLeft);
        
        Panel topPanel = new Panel(Location.CENTER);
        popup.addComponent(topPanel);
        
        Panel headerPanel = new Panel(Location.NORTH);
        topPanel.addComponent(headerPanel);
        
        headerPanel.addComponent(new Label(" ", Location.NORTH, 5)); //Padding Top
        if (subtitle != null)
            headerPanel.addComponent(new Label(subtitle, Location.CENTER, 16, 'b', new int[] { 0xE5, 0xE5, 0xFF })); //Subtitle
        headerPanel.addComponent(new Label(" ", Location.SOUTH, 5)); //Padding Header <-> Content
        
        topPanel.addComponent(contentPanel);
        if (buttonPanel != null)
            topPanel.addComponent(buttonPanel);
    }
    
    public static String createNew(String title, String subtitle, String message, int width, int height) {
	Popup popup = new Popup(title, width, height);
        
        Panel buttonPanel = new Panel(new BorderLayout(), Location.NONE);
        Button btnOK = new Button("   OK   ");
        btnOK.setStyled(true);
        buttonPanel.addComponent(btnOK);
        
        Panel kcPanel = new Panel(new BorderLayout(), Location.CENTER);
        kcPanel.addComponent(
            new TextPanel(
                message, width - 10, height - 10, 
                new int[] { 0, 0, 0},
                new int[] { 0xBE, 0xBC, 0xFB },
                "pics/layout/bg_trend.png", -1,
                Location.CENTER
            ));
        
        addNewStyle(popup, subtitle, kcPanel, buttonPanel);                        
        return popup.toString();
    }    
    public static void addNewStyle(Popup popup, String subtitle, Panel kcPanel, Panel buttonPanel) {
        popup.setBackground(new int[] { 255,255,255 });
                
        Panel centerPanel = new Panel(new BorderLayout(), Location.CENTER);
        popup.addComponent(centerPanel);
        
            centerPanel.addComponent(new Panel(new BorderLayout(), Location.NORTH, "-", 10, 10));
            centerPanel.addComponent(new Panel(new BorderLayout(), Location.SOUTH, "-", 0, 10));
            
            Panel middlePanel = new Panel(new BorderLayout(), Location.CENTER);
            centerPanel.addComponent(middlePanel);
        
                Panel msPanel = new Panel(new GridLayout(1, 1, 1, 1), Location.SOUTH);
                middlePanel.addComponent(msPanel);
                
                    Panel flowPanel = new Panel(new FlowLayout(), Location.NONE, "pics/layout/boxS_b.png", 0, 0);
                    msPanel.addComponent(flowPanel);
                        
                        Panel gridPanel = new Panel(new GridLayout(0,1,1,1), Location.NONE);
                        flowPanel.addComponent(gridPanel);
                        
                            gridPanel.addComponent(buttonPanel);
                            
                Panel layoutPanel = new Panel(new BorderLayout(), Location.CENTER);
                middlePanel.addComponent(layoutPanel);
                
                    Panel northPanel = new Panel(new BorderLayout(), Location.NORTH);
                    layoutPanel.addComponent(northPanel);
                    
                        Panel headerPanel = new Panel(new BorderLayout(), Location.NORTH);
                        northPanel.addComponent(headerPanel);
                        
                            Panel westPanel = new Panel(new BorderLayout(), Location.WEST, "pics/layout/boxS_tl.png", 16, 16);
                            headerPanel.addComponent(westPanel);
                            Panel cPanel = new Panel(new BorderLayout(), Location.CENTER, "pics/layout/boxS_tc.png", 16, 16);
                            headerPanel.addComponent(cPanel);
                            Panel ePanel = new Panel(new BorderLayout(), Location.EAST, "pics/layout/boxS_tr.png", 16, 16);
                            headerPanel.addComponent(ePanel);
                            
                    Panel styleWest = new Panel(new BorderLayout(), Location.WEST, "pics/layout/boxS_cl.png", 16, 16);
                    northPanel.addComponent(styleWest);
                    Panel styleEast = new Panel(new BorderLayout(), Location.EAST, "pics/layout/boxS_cr.png", 16, 16);
                    northPanel.addComponent(styleEast);
                    
                    Panel styleSouth = new Panel(new BorderLayout(), Location.SOUTH);
                    northPanel.addComponent(styleSouth);
                    
                    styleSouth.addComponent(new Panel(new BorderLayout(), Location.WEST, "pics/layout/boxS_bl.png", 16, 16));
                    styleSouth.addComponent(new Panel(new BorderLayout(), Location.CENTER, "pics/layout/boxS_bc.png", 0, 0));
                    styleSouth.addComponent(new Panel(new BorderLayout(), Location.EAST, "pics/layout/boxS_br.png", 16, 16));
                    
                    Panel gridCenter = new Panel(new GridLayout(1,1,1,1), Location.CENTER);
                    northPanel.addComponent(gridCenter);
                        gridCenter.addComponent(new Label(subtitle, Location.NONE, 18, 'b', new int[] {222,222,255}));
                    
                   
                    Panel contentPanel = new Panel(new BorderLayout(), Location.CENTER);
                    layoutPanel.addComponent(contentPanel);
                        
                        Panel cenPanel = new Panel(new BorderLayout(), Location.CENTER);
                        contentPanel.addComponent(cenPanel);
                        
                            Panel ccenPanel = new Panel(new BorderLayout(), Location.CENTER);
                            cenPanel.addComponent(ccenPanel);
                                
                                ccenPanel.addComponent(new Panel(new BorderLayout(), Location.NORTH, "-", 5, 10));
                                
                                ccenPanel.addComponent(kcPanel);
                                
                                ccenPanel.addComponent(new Panel(new BorderLayout(), Location.EAST, "pics/layout/bg_trend.png", 0, 10));
                                ccenPanel.addComponent(new Panel(new BorderLayout(), Location.WEST, "pics/layout/bg_trend.png", 0, 10));
            centerPanel.addComponent(new Panel(new BorderLayout(), Location.EAST, "pics/layout/bg_trend.png", 10, 10));
            centerPanel.addComponent(new Panel(new BorderLayout(), Location.WEST, "pics/layout/bg_trend.png", 10, 10));                
    }

}
