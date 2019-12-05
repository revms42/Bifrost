package org.ajar.bifrost.client.ui.workflow;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.ajar.bifrost.client.ui.HeimdallBifrostClient;
import org.ajar.bifrost.core.model.data.MappedFile;

public class FileSelectionDialog {
	
	private static class MappingListItem<O extends MappedFile, F> {
		
		private O mappedFile;
		private F proposed;
		private boolean selected;
		
		public MappingListItem(O mappedFile, F proposed, boolean selected) {
			this.mappedFile = mappedFile;
			this.selected = selected;
			this.proposed = proposed;
		}
	}
	
	private static class MappingListRenderer<O extends MappedFile, F> extends JLabel implements ListCellRenderer<MappingListItem<O,F>> {
		private static final long serialVersionUID = -3717168452229837640L;

		public MappingListRenderer() {
			super();
			this.setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends MappingListItem<O,F>> list, MappingListItem<O,F> value, int index, boolean isSelected, boolean cellHasFocus) {
			this.setText(getLabel(value));
			
			if(value.selected) {
				this.setBackground(Color.GREEN);
			} else {
				this.setBackground(Color.LIGHT_GRAY);
			}
			
			return this;
		}
		
		private String getLabel(MappingListItem<O,F> value) {
			return value.mappedFile.getName() + ": #" + value.mappedFile.getVersion() + " -> " + value.proposed.toString();
		}
	}
	
	private static abstract class MappingSelectionListener<A extends MappedFile, F> extends JPanel implements MouseListener, KeyListener, ListSelectionListener {
		private static final long serialVersionUID = 1L;

		protected final ListModel<MappingListItem<A,F>> model;
		protected MappingListItem<A,F> selected;
		
		public MappingSelectionListener(DefaultListModel<MappingListItem<A,F>> model) {
			super();
			this.model = model;
			
			this.setLayout(new BorderLayout());
			
			JCheckBox box = new JCheckBox(new AbstractAction("Map") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					selected.selected = !selected.selected;
				}
				
			});
			box.setSelected(true);
			this.add(box, BorderLayout.WEST);
			
			//layoutControls();
		}
		
		protected abstract void layoutControls();
		
		protected String getLabel() {
			if(selected == null) return "WHOOPS!!";
			return selected.mappedFile.getName() + ": #" + selected.mappedFile.getVersion() + " -> " + selected.proposed.toString();
		}

		@Override
		public void keyTyped(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				showMappingDialog();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() > 1) {
				showMappingDialog();
			} else {
				selected.selected = !selected.selected;
			}
		}
		
		private void showMappingDialog() {
			layoutControls();
			JOptionPane.showConfirmDialog(
					null, 
					this, 
					"Map " + selected.mappedFile.getName(), 
					JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE
			);
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			selected = model.getElementAt(e.getFirstIndex());
		}

		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
		
	}
	
	private static class FileMappingSelectionListener<A extends MappedFile> extends MappingSelectionListener<A,File> {
		private static final long serialVersionUID = 1L;

		private JLabel label;
		
		public FileMappingSelectionListener(DefaultListModel<MappingListItem<A,File>> model) {
			super(model);
		}
		
		@Override
		protected void layoutControls() {
			label = new JLabel(getLabel());
			this.add(label, BorderLayout.CENTER);
			
			JButton select = new JButton(new AbstractAction("Select Location") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					FileFilter filter = new FileFilter() {

						@Override
						public boolean accept(File f) {
							return true;
						}

						@Override
						public String getDescription() {
							return "Mapping for " + selected.mappedFile.getName();
						}
					};
					
					chooser.setFileFilter(filter);
					if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						selected.proposed = file;
						label.setText(getLabel());
					}
				}
				
			});
			this.add(select, BorderLayout.EAST);
		}
	}
	
	private static class StringMappingSelectionListener<A extends MappedFile> extends MappingSelectionListener<A,String> {
		private static final long serialVersionUID = 1L;

		private JTextField field;
		
		public StringMappingSelectionListener(DefaultListModel<MappingListItem<A,String>> model) {
			super(model);
		}
		
		@Override
		protected void layoutControls() {
			field = new JTextField();
			field.setEditable(true);
			field.setToolTipText(getLabel());
			field.setText(selected.mappedFile.getLocation());
			this.add(field, BorderLayout.CENTER);
		}
	}
	
	private final static JFileChooser chooser = HeimdallBifrostClient.chooser;

	public static <A extends MappedFile> List<A> selectFilesFromList(List<A> files, String title) {
		//TODO: Messed this up thinking it was one of the methods below. This just needs a multi-selection list.
		DefaultListModel<String> model = new DefaultListModel<>();
		List<String> mappings = files.parallelStream().map(file -> file.getName()).collect(toList());
		model.addAll(mappings);
		
		JList<String> localFileList = new JList<>(model);
		
		localFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		int choice = JOptionPane.showConfirmDialog(
				null, 
				new JScrollPane(localFileList), 
				title, 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE
		);
		
		if(choice == JOptionPane.OK_OPTION) {
			return Arrays.stream(localFileList.getSelectedIndices()).mapToObj(index -> files.get(index)).collect(toList());
		} else {
			return null;	
		}
	}
	
	public static <A extends MappedFile> Map<A,String> mapLocations(Map<A, String> files, String title) {
		LinkedList<MappingListItem<A,String>> mappings = new LinkedList<>();
		files.forEach((file, loc) -> mappings.add(new MappingListItem<>(file, loc, true)));
		
		DefaultListModel<MappingListItem<A,String>> model = new DefaultListModel<>();
		model.addAll(mappings);
		JList<MappingListItem<A,String>> localFileList = new JList<>(model);
		
		localFileList.setCellRenderer(new MappingListRenderer<A,String>());
		
		StringMappingSelectionListener<A> listener = new StringMappingSelectionListener<>(model);
		localFileList.addListSelectionListener(listener);
		localFileList.addKeyListener(listener);
		localFileList.addMouseListener(listener);
		
		int choice = JOptionPane.showConfirmDialog(
				null, 
				new JScrollPane(localFileList), 
				title, 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE
		);
		
		if(choice == JOptionPane.OK_OPTION) {
			return mappings.parallelStream().filter(mapping -> mapping.selected).collect(toMap(
					mapped -> mapped.mappedFile,
					mapped -> mapped.proposed
			));
		} else {
			return null;	
		}
	}
	
	public static <A extends MappedFile> Map<A,File> mapFiles(Map<A, File> files, String title) {
		LinkedList<MappingListItem<A,File>> list = new LinkedList<>();
		files.forEach((file, loc) -> list.add(new MappingListItem<A,File>(file, loc, true)));
		
		DefaultListModel<MappingListItem<A,File>> model = new DefaultListModel<>();
		model.addAll(list);
		JList<MappingListItem<A,File>> localFileList = new JList<>(model);
		
		localFileList.setCellRenderer(new MappingListRenderer<A,File>());
		
		FileMappingSelectionListener<A> listener = new FileMappingSelectionListener<>(model);
		localFileList.addListSelectionListener(listener);
		localFileList.addKeyListener(listener);
		localFileList.addMouseListener(listener);
		
		int choice = JOptionPane.showConfirmDialog(
				null, 
				new JScrollPane(localFileList), 
				title, 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE
		);
		
		if(choice == JOptionPane.OK_OPTION) {
			return list.parallelStream().filter(mapping -> mapping.selected).collect(toMap(
					mapped -> mapped.mappedFile,
					mapped -> mapped.proposed
			));
		} else {
			return null;	
		}
	}
	
	public static List<File> selectFilesFromFileSystem(String title, int selectionMode, boolean save, File... files) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		LinkedList<File> list = new LinkedList<>();
		list.addAll(Arrays.asList(files));
		
		DefaultListModel<File> model = new DefaultListModel<>();
		model.addAll(list);
		JList<File> fileList = new JList<>(model);
		
		panel.add(new JScrollPane(fileList), BorderLayout.CENTER);
		
		JButton addButton = new JButton(new AbstractAction("Add") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(selectionMode);
				
				int result = JFileChooser.CANCEL_OPTION;
				if(save) {
					result = chooser.showSaveDialog(panel);
				} else {
					result = chooser.showOpenDialog(panel);
				}
				
				if(result == JFileChooser.APPROVE_OPTION) {
					model.addElement(chooser.getSelectedFile());
				}
			}
		});
		
		JButton removeButton = new JButton(new AbstractAction("Remove") {
			private static final long serialVersionUID = -9079823207940840859L;

			@Override
			public void actionPerformed(ActionEvent e) {
				model.removeElement(fileList.getSelectedValue());
			}
			
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,2));
		
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		int choice = JOptionPane.showConfirmDialog(
				null, 
				panel, 
				title, 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE
		);
		
		if(choice == JOptionPane.OK_OPTION) {
			return Arrays.stream(model.toArray()).map(file -> (File)file).collect(toList());
		} else {
			return null;	
		}
	}
}
