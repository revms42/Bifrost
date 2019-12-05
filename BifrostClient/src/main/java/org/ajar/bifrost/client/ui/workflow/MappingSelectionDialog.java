package org.ajar.bifrost.client.ui.workflow;

import static java.util.stream.Collectors.toList;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.ajar.bifrost.client.MappedFileOperations;
import org.ajar.bifrost.client.comm.PersistenceClient;

public class MappingSelectionDialog {

	private static class RegisteredClientElement {
		private final PersistenceClient client;
		
		public RegisteredClientElement(PersistenceClient client) {
			this.client = client;
		}
		
		@Override
		public String toString() {
			return client.getClass().getSimpleName();
		}
	}
	
	public static PersistenceClient selectPersistenceMethod(String title) {
		DefaultListModel<RegisteredClientElement> model = new DefaultListModel<>();
		model.addAll(MappedFileOperations.getRegisteredClients().parallelStream().map(client -> new RegisteredClientElement(client)).collect(toList()));
		
		JList<RegisteredClientElement> allMappings = new JList<>(model);
		allMappings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		int choice = JOptionPane.showConfirmDialog(
				null, 
				new JScrollPane(allMappings), 
				title, 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE
		);
		
		if(choice == JOptionPane.OK_OPTION) {
			int selected = allMappings.getSelectedIndex();
			return model.get(selected).client;
		} else {
			return null;	
		}
	}
}
