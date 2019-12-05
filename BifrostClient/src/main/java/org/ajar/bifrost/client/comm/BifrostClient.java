package org.ajar.bifrost.client.comm;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ajar.bifrost.bind.BifrostPathConst;
import org.ajar.bifrost.core.model.call.PackageListResponse;
import org.ajar.bifrost.core.model.data.RegisteredPackage;

public interface BifrostClient extends BifrostPathConst {
	
	static Set<BifrostClient> INSTANCES = new HashSet<BifrostClient>();
	
	public static BifrostClient forServer(String address) {
		if(INSTANCES.size() == 0) {
			INSTANCES.add(BifrostHttpClient.singleton());
		}
		return INSTANCES.parallelStream().filter(client -> client.canReach(address)).findFirst().orElse(null);
	}
	
	public final static String KEY_SERVER = "bifrost.server.address";
	
	public boolean canReach(String serverUrl);
	public List<String> getConfigurationKeys();
	public void setConfigurationSetting(String key, String value);
	public void initializeClient();

	public PackageListResponse getPackageList(int page, int pageSize) throws IOException;
	public RegisteredPackage getPackageInfo(String name) throws IOException;
	public RegisteredPackage updatePackage(String name, String location) throws IOException;
	public RegisteredPackage deletePackage(String name, String location) throws IOException;
	public RegisteredPackage registerPackage(String name, String location) throws IOException;
	public RegisteredPackage setVersion(String name, long version) throws IOException;
}
