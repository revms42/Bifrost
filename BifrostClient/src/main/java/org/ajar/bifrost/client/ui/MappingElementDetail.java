package org.ajar.bifrost.client.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.client.ui.MappingElement.Status;
import org.ajar.bifrost.client.ui.workflow.CheckInExistingUI;
import org.ajar.bifrost.client.ui.workflow.CheckInNewUI;
import org.ajar.bifrost.client.ui.workflow.CheckOutExistingUI;
import org.ajar.bifrost.client.ui.workflow.CheckOutNewUI;
import org.ajar.bifrost.client.workflow.HeimdallWorkflow;
import org.ajar.bifrost.core.model.data.LocalMapping;
import org.ajar.bifrost.core.model.data.RegisteredPackage;
import org.ajar.bifrost.core.model.data.StoredMapping;

import static java.util.stream.Collectors.toList;

public class MappingElementDetail extends JPanel implements ListSelectionListener {
	private static final long serialVersionUID = 100657251051473002L;

	private static HeimdallWorkflow workflow;
	
	private final MappingListModel model;
	private final JLabel label;
	private final JButton option1;
	private final JButton option2;
	private final JButton option3;
	private final DefaultListModel<String> fileListModel;
	
	public MappingElement selected;
	
	private final AbstractAction checkInNew;
	private final AbstractAction checkInExisting;
	private final AbstractAction checkOutNew;
	private final AbstractAction checkOutExisting;
	private final AbstractAction deleteExisting;
	
	private static void showNotification(String text, String title) {
		JOptionPane.showConfirmDialog(null, text, title, JOptionPane.OK_CANCEL_OPTION);
	}
	
	public MappingElementDetail(MappingListModel model) {
		this.model = model;
		
		this.setLayout(new BorderLayout());
		
		label = new JLabel("Waiting...");
		label.setPreferredSize(new Dimension(200,25));
		
		option1 = new JButton();
		option2 = new JButton();
		option3 = new JButton();
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,3));
		
		buttonPanel.add(option1);
		buttonPanel.add(option2);
		buttonPanel.add(option3);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		topPanel.add(label, BorderLayout.CENTER);
		topPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		topPanel.setPreferredSize(new Dimension(200,50));
		this.add(topPanel, BorderLayout.NORTH);
		
		fileListModel = new DefaultListModel<>();
		JList<String> list = new JList<>(fileListModel);
		
		list.setPreferredSize(new Dimension(200,300));
		this.add(new JScrollPane(list), BorderLayout.CENTER);
		
		checkInNew = new AbstractAction() {
			private static final long serialVersionUID = 7547209487562102041L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(workflow == null) {
					workflow = new CheckInNewUI(selected.getMapping());
					if(!workflow.startWorkflow()) {
						showNotification("Failed to create new mapping!", "Failed");
					} else {
						refreshInformation();
						showNotification("Checked in new mapping!", "Success");
					}
					workflow = null;
				} else {
					showNotification("Cannot create now another operation is pending!", "In Progress");
				}
			}
		};
		
		checkInExisting = new AbstractAction() {
			private static final long serialVersionUID = 3976117406293296587L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(workflow == null) {
					workflow = new CheckInExistingUI(selected.getMapping());
					if(!workflow.startWorkflow()) {
						showNotification("Failed to checkin mapping!", "Failed");
					} else {
						refreshInformation();
						showNotification("Checkin Successfull!", "Success");
					}
					workflow = null;
				} else {
					showNotification("Cannot check in now another operation is pending!", "In Progress");
				}
			}
		};
		
		checkOutNew = new AbstractAction() {
			private static final long serialVersionUID = 2692449940075364422L;
			private final JFileChooser chooser = HeimdallBifrostClient.chooser;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(workflow == null) {
					chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					chooser.setFileFilter(new JsonFileFilter("Heimdall Mapping Files (.json)"));
					if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						try {
							RegisteredPackage regPack = Heimdall.getClient().getPackageInfo(selected.getName());
							selected.setPackageStatusDescription(regPack);
							workflow = new CheckOutNewUI(regPack, chooser.getSelectedFile());
							
							if(!workflow.startWorkflow()) {
								showNotification("Failed to checkout mapping!", "Failed");
							} else {
								String port = JOptionPane.showInputDialog("Please provide the port to associate with the checked out mapping.");
								RegisteredPackage newPackage = Heimdall.getClient().registerPackage(selected.getRegisteredPackage().getName(), port);
								
								if(newPackage != null) {
									selected.setPackageStatusDescription(newPackage);
									refreshInformation();
									showNotification("Mapping checked out!", "Success");
								} else {
									showNotification("Failed to checkout mapping! Server is unreachable!", "Failed");
								}
							}
						} catch (IOException e1) {
							e1.printStackTrace();
							showNotification("Failed to checkout mapping!", "Failed");
						}
					}
					workflow = null;
				} else {
					showNotification("Cannot check out now another operation is pending!", "In Progress");
				}
			}
		};
		
		checkOutExisting = new AbstractAction() {
			private static final long serialVersionUID = -2308131719078601874L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(workflow == null) {
					workflow = new CheckOutExistingUI(selected.getRegisteredPackage());
					
					if(!workflow.startWorkflow()) {
						showNotification("Failed to checkout mapping!", "Failed");
					} else {
						String port = JOptionPane.showInputDialog("Please the ip port to associate with the checked out mapping.");
						try {
							RegisteredPackage newPackage = Heimdall.getClient().registerPackage(selected.getRegisteredPackage().getName(), port);
							
							if(newPackage != null) {
								selected.setPackageStatusDescription(newPackage);
								refreshInformation();
								showNotification("Checkout successful!", "Success");
							} else {
								showNotification("Failed to checkout mapping! Server is unreachable!", "Failed");
							}
						} catch (IOException e1) {
							e1.printStackTrace();
							showNotification("Failed to checkout mapping!", "Failed");
						}
					}
					workflow = null;
				} else {
					showNotification("Cannot check out now another operation is pending!", "In Progress");
				}
			}
		};
		
		deleteExisting = new AbstractAction() {
			private static final long serialVersionUID = -2308131719078601874L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(
						null, "Are you sure you want to delete " + selected.getName() + "?",
						"Confirm Delete",
						JOptionPane.OK_CANCEL_OPTION
				) == JOptionPane.OK_OPTION) {
					try {
						if(selected.getMapping() != null) {
							Heimdall.deleteMonitorFiles(selected.getMapping());
							selected.getMapping().deleteLocalMapping();
							Heimdall.getInventory().remove(selected.getName());
						}
						
						if(selected.getRegisteredPackage() != null) {
							if(JOptionPane.showConfirmDialog(
									null, 
									"Delete remotely stored files as well?",
									"Delete Stored", 
									JOptionPane.OK_CANCEL_OPTION
							) == JOptionPane.OK_OPTION) {
								selected.getMapping().deleteRemoteMapping();
								RegisteredPackage ret = Heimdall.getClient().deletePackage(
										selected.getRegisteredPackage().getName(),
										selected.getRegisteredPackage().getLocation()
								);
								
								if(ret == null) {
									showNotification(
											"Could not delete remote files! Server is unreachable!",
											"Communication Error");
								}
							}
						}
						
						model.deleteElement(selected);
						selected = null;
						refreshInformation();
					} catch (IOException | OperationNotSupportedException e1) {
						e1.printStackTrace();
						showNotification(e1.getMessage(), "Error");
					}
				}
			}
		};
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e == null) {
			updateInformation();
		} else if(!e.getValueIsAdjusting()) {
			selected = model.getElementAt(e.getFirstIndex());
			updateInformation();
		}
	}
	
	private void refreshInformation() {
		if(selected != null) {
			try {
				selected.setPackageStatusDescription(Heimdall.getClient().getPackageInfo(selected.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			selected.determineStatus();
			updateInformation();
		}
	}
	
	public void updateInformation() {
		if(selected != null) {
			long localVersion = -1L;
			long remoteVersion = -1L;
			
			if(selected.getMapping() != null) {
				LocalMapping local = null;
				StoredMapping remote = null;
				
				try {
					local = selected.getMapping().getLocalMapping();
					localVersion = local.getVersion();
				} catch (OperationNotSupportedException | IOException e) {}
				
				try {
					remote = selected.getMapping().getStoredMapping();
					remoteVersion = remote.getVersion();
				} catch (OperationNotSupportedException | IOException e) {}
				
				if(local != null) {
					this.setName(local.getName());
				} else if(remote != null) {
					this.setName(remote.getName());
				}
			}
			
			Exception ioe = null;
			if(remoteVersion != -1L && selected.getPackageStatusDescription() != null) {
				try {
					RegisteredPackage regPack = Heimdall.getClient().getPackageInfo(getName());
					selected.setPackageStatusDescription(regPack);
					
					if(remoteVersion != regPack.getVersion()) {
						Heimdall.getClient().setVersion(getName(), remoteVersion);
						selected.determineStatus();
					}
				} catch (IOException e) {
					ioe = e;
				}
			}
			
			Status status = selected.getStatus();
			
			fileListModel.removeAllElements();
			
			switch(status) {
			case CHECKED_OUT_LOCAL:
				label.setText("Checked Out");
				
				option1.setEnabled(true);
				option1.setAction(checkInExisting);
				option1.setText("Check In");
				
				option2.setEnabled(true);
				option2.setAction(deleteExisting);
				option2.setText("Delete");
				
				option3.setText("");
				option3.setEnabled(false);
				
				fileListModel.addAll(createFileList(selected.getMapping()));
				break;
			case CHECKED_OUT_REMOTE:
				label.setText("Available at " + selected.getRegisteredPackage().getLocation());
				option1.setText("");
				option1.setEnabled(false);
				option2.setText("");
				option2.setEnabled(false);
				option3.setText("");
				option3.setEnabled(false);
				break;
			case ERROR:
				if(ioe != null) {
					label.setText(ioe.getMessage());
				} else {
					label.setText("Error Updating Information");
				}
				option1.setText("");
				option1.setEnabled(false);
				option2.setText("");
				option2.setEnabled(false);
				option3.setText("");
				option3.setEnabled(false);
				break;
			case LOCAL_AHEAD_REMOTE:
				label.setText("Ahead of remote by " + (localVersion - remoteVersion));
				
				option1.setEnabled(true);
				//TODO: This may not work.....
				option1.setAction(checkInExisting);
				option1.setText("Update Remote");
				
				option2.setEnabled(true);
				option2.setAction(deleteExisting);
				option2.setText("Delete");
				
				option3.setEnabled(true);
				option3.setAction(checkOutExisting);
				option3.setText("Check Out");
				
				fileListModel.addAll(createFileList(selected.getMapping()));
				break;
			case LOCAL_BEHIND_REMOTE:
				label.setText("Behind remote by " + (remoteVersion - localVersion));
				option1.setEnabled(true);
				option1.setAction(checkOutExisting);
				option1.setText("Update and Check Out");
				
				option2.setEnabled(true);
				option2.setAction(deleteExisting);
				option2.setText("Delete");
				
				option3.setText("");
				option3.setEnabled(false);
				
				fileListModel.addAll(createFileList(selected.getMapping()));
				break;
			case LOCAL_ONLY:
				label.setText("Not yet uploaded");
				
				option1.setEnabled(true);
				option1.setAction(checkInNew);
				option1.setText("Publish");
				
				option2.setEnabled(true);
				option2.setAction(deleteExisting);
				option2.setText("Delete");
				
				option3.setText("");
				option3.setEnabled(false);
				
				fileListModel.addAll(createFileList(selected.getMapping()));
				break;
			case LOCAL_REMOTE_SYNC:
				label.setText("Ready to check out");
				
				option1.setEnabled(true);
				option1.setAction(checkOutExisting);
				option1.setText("Check Out");
				
				option2.setEnabled(true);
				option2.setAction(deleteExisting);
				option2.setText("Delete");
				
				option3.setText("");
				option3.setEnabled(false);
				
				fileListModel.addAll(createFileList(selected.getMapping()));
				break;
			case NOT_UPDATED:
				label.setText("Waiting for update....");
				option1.setText("");
				option1.setEnabled(false);
				option2.setText("");
				option2.setEnabled(false);
				option3.setText("");
				option3.setEnabled(false);
				break;
			case REMOTE_ONLY:
				label.setText("Not yet downloaded");
				
				option1.setEnabled(true);
				option1.setAction(checkOutNew);
				option1.setText("Download and Check Out");
				
				option2.setEnabled(true);
				option2.setAction(deleteExisting);
				option2.setText("Delete");
				
				option3.setText("");
				option3.setEnabled(false);
				
				fileListModel.addAll(createFileList(selected.getMapping()));
				break;
			default:
				label.setText("ERROR!!!");
				option1.setText("");
				option1.setEnabled(false);
				option2.setText("");
				option2.setEnabled(false);
				option3.setText("");
				option3.setEnabled(false);
				break;
			}
		} else {
			fileListModel.removeAllElements();
			label.setText("");
			option1.setText("");
			option1.setEnabled(false);
			option2.setText("");
			option2.setEnabled(false);
			option3.setText("");
			option3.setEnabled(false);
		}
		this.revalidate();
	}
	
	private List<String> createFileList(BifrostPersistenceWrapper wrapper) {
		if(wrapper == null) return new LinkedList<String>();
		
		HashMap<String,String> list = new HashMap<>();
		if(wrapper.getLocalLocation() != null) {
			try {
				wrapper.getLocalMapping().getFiles().forEach(localFile -> {
					list.put(localFile.getName(), localFile.getLocation() + " #" + localFile.getVersion());
				});
			} catch (OperationNotSupportedException | IOException e) {
				e.printStackTrace();
			}
		}
		
		if(wrapper.getRemoteLocation() != null && wrapper.getRemoteLocation().length() > 0) {
			try {
				wrapper.getStoredMapping().getFiles().forEach(remoteFile -> {
					if(list.containsKey(remoteFile.getName())) {
						String localPart = list.get(remoteFile.getName());
						list.put(
							remoteFile.getName(), 
							localPart + " -> " + remoteFile.getLocation() + " #" + remoteFile.getVersion()
						);
					} else {
						list.put(
								remoteFile.getName(), 
								"(none) -> " + remoteFile.getLocation() + " #" + remoteFile.getVersion()
							);
					}
				});
			} catch (OperationNotSupportedException | IOException e) {
				e.printStackTrace();
			}
		}
		
		return list.values().parallelStream().map(value -> {
			if(!value.contains(" -> ")) {
				return value + " -> (none)";
			} else {
				return value;
			}
		}).collect(toList());
	}
}
