package org.ajar.bifrost.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.comm.PersistenceClient;
import org.ajar.bifrost.client.comm.impl.DropBoxPersistence;
import org.ajar.bifrost.client.comm.impl.FileSystemPersistence;
import org.ajar.bifrost.client.comm.impl.StringPersistence;
import org.ajar.bifrost.client.model.BifrostMapping;

import static java.util.stream.Collectors.toList;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class MappedFileOperations {
	
	private static final Set<PersistenceClient> clients;
	
	static {
		clients = new HashSet<>();
		registerPersistenceClient(new DropBoxPersistence());
		registerPersistenceClient(new FileSystemPersistence());
		registerPersistenceClient(new StringPersistence());
	}
	
	public static Set<PersistenceClient> getRegisteredClients() {
		return clients;
	}
	
	public static void registerPersistenceClient(PersistenceClient client) {
		clients.add(client);
	}
	
	public static PersistenceClient findPersistenceClientForLocation(String location) {
		return clients.parallelStream().filter(client -> client.canHandle(location)).findFirst().orElse(null);
	}

	public static boolean publishLocalToRemote(BifrostMapping mapping) throws OperationNotSupportedException, IOException {
		InputStream localClient = findPersistenceClientForLocation(mapping.getLocalLocation())
				.readStreamFromLocal(mapping.getLocalLocation());
		return findPersistenceClientForLocation(mapping.getRemoteLocation())
				.writeToRemote(localClient, mapping.getRemoteLocation());
	}
	
	public static boolean downloadRemoteToLocal(BifrostMapping mapping) throws OperationNotSupportedException, IOException {
		OutputStream localClient = findPersistenceClientForLocation(mapping.getLocalLocation())
				.writeStreamToLocal(mapping.getLocalLocation());
		return findPersistenceClientForLocation(mapping.getRemoteLocation())
				.readFromRemote(localClient, mapping.getRemoteLocation());
	}
	
	public static boolean publishLocalToRemote(List<BifrostMapping> mappings) {
		return !mappings.parallelStream().anyMatch(mapping -> {
			try {
				return !publishLocalToRemote(mapping);
			} catch (OperationNotSupportedException | IOException e) {
				e.printStackTrace();
				return false;
			}
		});
	}
	
	public static boolean downloadRemoteToLocal(List<BifrostMapping> mappings) {
		return !mappings.parallelStream().anyMatch(mapping -> {
			try {
				return !downloadRemoteToLocal(mapping);
			} catch (OperationNotSupportedException | IOException e) {
				e.printStackTrace();
				return false;
			}
		});
	}
	
	public static List<Path> proposeMonitorPaths(List<Path> filePaths) {
		int bestCost = -1;
		List<Path> monitorPaths = null;
		
		Set<Path> newPaths = new HashSet<>(filePaths.parallelStream().map(path -> path.getParent()).collect(toList()));
		int newCost = 0;
		while(newCost > -1) {
			if(monitorPaths != null) {
				Path selected = newPaths.parallelStream().max((a, b) -> a.getNameCount() - b.getNameCount()).orElse(null);
				
				if(selected == null) {
					break;
				} else {
					newPaths.remove(selected);
					newPaths.add(selected.getParent());
				}
			}
			
			newCost = pathValue(newPaths);
			
			if(newCost > bestCost) {
				monitorPaths = new LinkedList<>(newPaths);
			}
			
			if(newPaths.size() == 1) {
				break;
			}
		}
		
		return monitorPaths;
	}
	
	private static int pathValue(Collection<Path> paths) {
		int depthValue = paths.parallelStream().map(path -> path.getNameCount()).reduce(0, (a, b) -> a + b );
		return depthValue - paths.size();
	}
}
