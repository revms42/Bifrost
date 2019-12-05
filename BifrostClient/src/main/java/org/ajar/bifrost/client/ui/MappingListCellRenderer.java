package org.ajar.bifrost.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.ListCellRenderer;
import javax.swing.JLabel;

public class MappingListCellRenderer extends JLabel implements ListCellRenderer<MappingElement> {
	
	private static final long serialVersionUID = 1479740798066542287L;
	
	private static final HashMap<MappingElement.Status, Color> foregrounds = new HashMap<>();
	private static final HashMap<MappingElement.Status, Color> backgrounds = new HashMap<>();
	
	static {
		foregrounds.put(MappingElement.Status.CHECKED_OUT_LOCAL, Color.GRAY);
		backgrounds.put(MappingElement.Status.CHECKED_OUT_LOCAL, Color.GREEN);
		
		foregrounds.put(MappingElement.Status.CHECKED_OUT_REMOTE, Color.GRAY);
		backgrounds.put(MappingElement.Status.CHECKED_OUT_REMOTE, Color.CYAN);
		
		foregrounds.put(MappingElement.Status.ERROR, Color.BLACK);
		backgrounds.put(MappingElement.Status.ERROR, Color.RED);
		
		foregrounds.put(MappingElement.Status.LOCAL_REMOTE_SYNC, Color.GRAY);
		backgrounds.put(MappingElement.Status.LOCAL_REMOTE_SYNC, Color.BLUE);
		
		foregrounds.put(MappingElement.Status.LOCAL_ONLY, Color.BLACK);
		backgrounds.put(MappingElement.Status.LOCAL_ONLY, Color.YELLOW);
		
		foregrounds.put(MappingElement.Status.LOCAL_AHEAD_REMOTE, Color.BLACK);
		backgrounds.put(MappingElement.Status.LOCAL_AHEAD_REMOTE, Color.YELLOW);
		
		foregrounds.put(MappingElement.Status.REMOTE_ONLY, Color.BLACK);
		backgrounds.put(MappingElement.Status.REMOTE_ONLY, Color.ORANGE);
		
		foregrounds.put(MappingElement.Status.LOCAL_BEHIND_REMOTE, Color.BLACK);
		backgrounds.put(MappingElement.Status.LOCAL_BEHIND_REMOTE, Color.ORANGE);
		
		foregrounds.put(MappingElement.Status.NOT_UPDATED, Color.DARK_GRAY);
		backgrounds.put(MappingElement.Status.NOT_UPDATED, Color.GRAY);
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends MappingElement> list, MappingElement value, int index, boolean isSelected, boolean cellHasFocus) {
		if(!this.isOpaque()) this.setOpaque(true);
		
        setComponentOrientation(list.getComponentOrientation());

        //value.determineStatus();
        MappingElement.Status status = value.getStatus();
        
        setBackground(backgrounds.get(status));
        setForeground(foregrounds.get(status));
        
        setIconOnStatus(status);
        setEnabled(list.isEnabled());
        
        setFontOnStatus(list.getFont(), status);
        setBorder(status, isSelected, cellHasFocus);
        
        setText(value.getName());

        return this;
	}
	
	private void setIconOnStatus(MappingElement.Status status) {
//        //TODO: Tinker with icon
//        if (value instanceof Icon) {
//            setIcon((Icon)value);
//            setText("");
//        }
//        else {
//            setIcon(null);
//            setText((value == null) ? "" : value.toString());
//        }
	}

	private void setFontOnStatus(Font font, MappingElement.Status status) {
		switch(status) {
		case ERROR:
		case CHECKED_OUT_LOCAL:
			setFont(font.deriveFont(Font.BOLD));
			break;
		case NOT_UPDATED:
		case REMOTE_ONLY:
		case LOCAL_ONLY:
			setFont(font.deriveFont(Font.ITALIC));
			break;
		default:
			setFont(font);
			break;
		
		}
	}
	
	private void setBorder(MappingElement.Status status, boolean isSelected, boolean cellHasFocus) {
        Border border = null;
        Color defaultColor = foregrounds.get(status);
        switch(status) {
        case ERROR:
        case CHECKED_OUT_REMOTE:
		case CHECKED_OUT_LOCAL:
			border = BorderFactory.createLineBorder(defaultColor, 3);
			break;
		case REMOTE_ONLY:
		case LOCAL_ONLY:
		case LOCAL_BEHIND_REMOTE:
		case LOCAL_AHEAD_REMOTE:
			border = BorderFactory.createDashedBorder(null);
			break;
		case LOCAL_REMOTE_SYNC:
			border = BorderFactory.createLineBorder(defaultColor);
			break;
		default:
			border = BorderFactory.createEmptyBorder();
			break;
        }
        setBorder(border);
	}
}
