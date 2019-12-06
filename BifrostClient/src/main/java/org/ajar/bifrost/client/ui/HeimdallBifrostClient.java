package org.ajar.bifrost.client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.MappedFileOperations;
import org.ajar.bifrost.client.comm.BifrostClient;
import org.ajar.bifrost.client.comm.PersistenceClient;
import org.ajar.bifrost.client.model.MappingInventory;
import org.ajar.bifrost.client.ui.workflow.CreateNewUI;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class HeimdallBifrostClient extends JFrame {
	private static final long serialVersionUID = -6168888185143776388L;
	public static final JFileChooser chooser = new JFileChooser();

	private interface DefaultableCommandLineArg {
		
		String getDefault(HeimdallBifrostClient client);
		boolean isValidValue(String value);
	}
	
	private enum CommandLineArg implements DefaultableCommandLineArg {
		PROPERTIES("-p", "-p\tProperties file that contains the working properties of Heimdall.", true){
			@Override
			public String getDefault(HeimdallBifrostClient client) {
				try {
					File heimdall = new File(Heimdall.class.getProtectionDomain().getCodeSource().getLocation().toURI());
					
		    		if(!heimdall.isDirectory()) heimdall = heimdall.getParentFile();
		    		
		    		return heimdall.toPath().toAbsolutePath().resolve(new File("heimdall.properties").toPath()).toString();
				} catch (URISyntaxException e) {
					e.printStackTrace();
					return (new File("heimdall.properties")).getAbsolutePath();
				}
			}

			@Override
			public boolean isValidValue(String value) {
				File file = new File(value);
				return file.exists();
			}
		},
		INVENTORY("-i", "-i\tInventory of Bifrost mappings locally available.", true) {
			@Override
			public String getDefault(HeimdallBifrostClient client) {
				try {
					File heimdall = new File(Heimdall.class.getProtectionDomain().getCodeSource().getLocation().toURI());
					
		    		if(!heimdall.isDirectory()) heimdall = heimdall.getParentFile();
		    		
		    		return heimdall.toPath().toAbsolutePath().resolve(new File("inventory.json").toPath()).toString();
				} catch (URISyntaxException e) {
					e.printStackTrace();
					return (new File("inventory.json")).getAbsolutePath();
				}
			}

			@Override
			public boolean isValidValue(String value) {
				File file = new File(value);
				return file.exists();
			}
		},
		HELP("-h", "-h\tDisplays the help screen (this)", false){
			public String getDefault(HeimdallBifrostClient client) {
				return CommandLineArg.usage();
			}
		};
		
		private final String match;
		private final String description;
		private final boolean hasValue;
		
		private CommandLineArg(String match, String description, boolean hasValue) {
			this.match = match;
			this.description = description;
			this.hasValue = hasValue;
		}
		
		public boolean matches(String s) {
			return s != null? match.contentEquals(s) : false;
		}
		
		public boolean isValidValue(String value) {
			return true;
		}
		
		public static CommandLineArg parse(String s) {
			for(CommandLineArg arg : CommandLineArg.values()) {
				if(arg.matches(s)) return arg;
			}
			return null;
		}
		
		public static String usage() {
			StringBuilder builder = new StringBuilder();
			builder.append("Heimdall Bifrost Client: Version 0.0.1 (pre-alpha)\n");
			builder.append("Usage: java -jar heimdall.jar ([key] [value])...\n");
			
			for(CommandLineArg arg : CommandLineArg.values()) {
				builder.append("\t");
				builder.append(arg.description);
				builder.append("\n");
			}
			
			return builder.toString();
		}
	}
	
	public static void main(String[] args) throws IOException {
		HashMap<CommandLineArg, String> parsed = new HashMap<>();
		
		Iterator<String> argIterator = Arrays.asList(args).iterator();
		String arg = null;
		while(argIterator.hasNext()) {
			arg = argIterator.next();
			CommandLineArg def = CommandLineArg.parse(arg);
			
			if(def == null) {
				errorOut("ERROR: Do not recognize argument '" + arg + "'\n");
			}
			switch(def) {
			case HELP:
				System.out.println(def.getDefault(null));
				System.exit(0);
				break;
			default:
				if(def.hasValue) {
					if(argIterator.hasNext()) {
						String argValue = argIterator.next();
						
						if(def.isValidValue(argValue)) {
							parsed.put(def, argValue);							
						} else {
							errorOut("ERROR: The value '" + argValue + "' is not valid for '" + arg + "'");
						}
					} else {
						errorOut("ERROR: The argument '" + arg + "' requires a value");
					}
				} else {
					parsed.put(def, null);
				}
			}
		}
		
		HeimdallBifrostClient client = new HeimdallBifrostClient();
		
		try {
			client.configure(parsed);
		} catch (IOException e) {
			e.printStackTrace();
			errorOut("ERROR: Error configuring Heimdall");
		}
		
		client.layoutUI();
		client.setupMenuBar();
		client.pack();
		client.setVisible(true);
	}

	private static void errorOut(String message) {
		System.err.println(message);
		System.out.println(CommandLineArg.usage());
		System.exit(1);
	}

	private String propLocation;
	private JList<MappingElement> mappingListUI;
	private MappingListModel mappingList;
	private MappingElementDetail detailPanel;
	
	public HeimdallBifrostClient() {
		super();
		//TODO: Replace this with something that saves.
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private void configure(HashMap<CommandLineArg,String> options) throws IOException {
		propLocation = options.getOrDefault(CommandLineArg.PROPERTIES, CommandLineArg.PROPERTIES.getDefault(this));
		File propFile = new File(propLocation);
		
		if(!propFile.exists()) propFile.createNewFile();
		
		Heimdall.loadPropertiesFromFile(propFile);
		Heimdall.configureFromProperties(Heimdall.getProperties());
		
		String inventoryOverride = options.get(CommandLineArg.INVENTORY);
		if(inventoryOverride != null) {
			Heimdall.getProperties().setProperty(MappingInventory.INVENTORY_KEY, inventoryOverride);
		}
		
		Heimdall.loadMappingInventory();
	}
	
	private void layoutUI() throws IOException {
		this.setLayout(new BorderLayout());
		
		createMappingList();
		mappingListUI.setPreferredSize(new Dimension(200,300));
		
		detailPanel = new MappingElementDetail(mappingList);
		detailPanel.setPreferredSize(new Dimension(200,300));
		mappingListUI.addListSelectionListener(detailPanel);
		
		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mappingListUI, detailPanel);
		this.add(splitter, BorderLayout.CENTER);
		this.setSize(800, 600);
		
		this.pack();
	}
	
	private void createMappingList() throws IOException {
		mappingList = new MappingListModel();
		mappingList.setupList();
		mappingListUI = new JList<>(mappingList);
		mappingListUI.setCellRenderer(new MappingListCellRenderer());
	}
	
	private void setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		menuBar.add(createFileMenu());
		menuBar.add(createMappingMenu());
		menuBar.add(createServerMenu());
		menuBar.add(createClientMenu());
		menuBar.add(createPersistenceMenu());
		
		this.setJMenuBar(menuBar);
	}
	
	private JMenu createFileMenu() {
		JMenu file = new JMenu("File");
		
		JMenuItem open = new JMenuItem(new AbstractAction("Load Properties") {
			private static final long serialVersionUID = -3856178176268761248L;

			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setFileFilter(PropertiesFileFilter.singleton);
				if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						Heimdall.loadPropertiesFromFile(chooser.getSelectedFile());
						Heimdall.configureFromProperties(Heimdall.getProperties());
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showConfirmDialog(null, e1.getMessage());
					}
				}
			}
		});
		file.add(open);
		
		JMenuItem save = new JMenuItem(new AbstractAction("Save Properties") {
			private static final long serialVersionUID = -3856178176268761248L;

			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setFileFilter(PropertiesFileFilter.singleton);
				if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						Heimdall.savePropertiesToFile(chooser.getSelectedFile());
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showConfirmDialog(null, e1.getMessage());
					}
				}
			}
		});
		file.add(save);
		file.addSeparator();
		
		JMenuItem load = new JMenuItem(new AbstractAction("Load Inventory") {
			private static final long serialVersionUID = -3856178176268761248L;

			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setFileFilter(new JsonFileFilter("Heimdall Inventory Files (.json)"));
				if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						Heimdall.getProperties().setProperty(MappingInventory.INVENTORY_KEY, chooser.getSelectedFile().getAbsolutePath());
						Heimdall.loadMappingInventory();
						mappingList.setupList();
						mappingListUI.ensureIndexIsVisible(mappingList.getSize());
						detailPanel.selected = null;
						detailPanel.valueChanged(null);
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showConfirmDialog(null, e1.getMessage());
					}
				}
			}
		});
		file.add(load);
		
		JMenuItem saveInv = new JMenuItem(new AbstractAction("Save Inventory") {
			private static final long serialVersionUID = -3856178176268761248L;

			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setFileFilter(new JsonFileFilter("Heimdall Inventory Files (.json)"));
				if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						Heimdall.getProperties().setProperty(MappingInventory.INVENTORY_KEY, chooser.getSelectedFile().getAbsolutePath());
						Heimdall.saveMappingInventory();
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showConfirmDialog(null, e1.getMessage());
					}
				}
			}
		});
		file.add(saveInv);
		file.addSeparator();
		
		JMenuItem quit = new JMenuItem(new AbstractAction("Quit") {
			private static final long serialVersionUID = -3856178176268761248L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(quit);
		
		return file;
	}
	
	private final JMenu createMappingMenu() {
		JMenu mapping = new JMenu("Mapping");
		
		JMenuItem newMapping = new JMenuItem(new AbstractAction("New...") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				CreateNewUI workFlow = new CreateNewUI();
				
				if(!workFlow.startWorkflow()) {
					JOptionPane.showConfirmDialog(null, "Creation failed", "Failed", JOptionPane.OK_CANCEL_OPTION);
				} else {
					mappingList.addNewElement(workFlow.getCreatedMapping());
				}
			}
		});
		mapping.add(newMapping);
		
		JMenuItem refresh = new JMenuItem(new AbstractAction("Refresh") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Heimdall.loadMappingInventory();
					mappingList.setupList();
					mappingListUI.ensureIndexIsVisible(mappingList.getSize());
					detailPanel.selected = null;
					detailPanel.valueChanged(null);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
			
		});
		mapping.add(refresh);
		
		return mapping;
	}
	
	private JMenu createServerMenu() {
		JMenu server = new JMenu("Server");
		
		JMenuItem configureServer = new JMenuItem(new AbstractAction("Server Location") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String server = Heimdall.getProperties().getProperty(BifrostClient.KEY_SERVER);
				server = JOptionPane.showInputDialog("Server Address", server);
				
				Heimdall.getProperties().setProperty(BifrostClient.KEY_SERVER, server);
				Heimdall.configureFromProperties(Heimdall.getProperties());
			}
			
		});
		server.add(configureServer);
		
		return server;
	}
	
	private JMenu createClientMenu() {
		JMenu client = new JMenu("Client");
		
		for(BifrostClient bc : BifrostClient.INSTANCES) {
			JMenuItem configureClient = new JMenuItem(new AbstractAction("Configure " + bc.getClass().getSimpleName()) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					configureClient(bc);
				}
				
			});
			client.add(configureClient);
		}
		return client;
	}
	
	private void configureClient(BifrostClient client) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		
		for(String key : client.getConfigurationKeys()) {
			JTextField field = new JTextField();
			field.setToolTipText(key);
			field.setName(key);
			field.setText(Heimdall.getProperties().getProperty(key));
			
			field.setBorder(BorderFactory.createTitledBorder(key));
			panel.add(field);
		}
		
		if(JOptionPane.showConfirmDialog(
				null, 
				new JScrollPane(panel), 
				"Configure " + client.getClass().getSimpleName(), 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.INFORMATION_MESSAGE
		) == JOptionPane.OK_OPTION) {
			for(Component c : panel.getComponents()) {
				if(c instanceof JTextField) {
					String key = c.getName();
					String value = ((JTextField) c).getText();
					
					Heimdall.getProperties().setProperty(key, value);
				}
			}
			Heimdall.configureFromProperties(Heimdall.getProperties());
		}
	}
	
	private JMenu createPersistenceMenu() {
		JMenu client = new JMenu("Persistence");
		
		for(PersistenceClient bc : MappedFileOperations.getRegisteredClients()) {
			JMenuItem configureClient = new JMenuItem(new AbstractAction("Configure " + bc.getClass().getSimpleName()) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					configurePersistence(bc);
				}
				
			});
			client.add(configureClient);
		}
		return client;
	}
	
	private void configurePersistence(PersistenceClient client) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		
		for(String key : client.configurationKeys()) {
			JTextField field = new JTextField();
			field.setToolTipText(key);
			field.setName(key);
			field.setText(Heimdall.getProperties().getProperty(key));
			
			field.setBorder(BorderFactory.createTitledBorder(key));
			panel.add(field);
		}
		
		if(JOptionPane.showConfirmDialog(
				null, 
				new JScrollPane(panel), 
				"Configure " + client.getClass().getSimpleName(), 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.INFORMATION_MESSAGE
		) == JOptionPane.OK_OPTION) {
			for(Component c : panel.getComponents()) {
				if(c instanceof JTextField) {
					String key = c.getName();
					String value = ((JTextField) c).getText();
					
					Heimdall.getProperties().setProperty(key, value);
				}
			}
			Heimdall.configureFromProperties(Heimdall.getProperties());
		}
	}
}
