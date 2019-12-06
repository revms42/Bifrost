package org.ajar.bifrost.core.model.call;

import java.util.LinkedList;
import java.util.List;

import com.google.api.client.util.Key;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class PackageListResponse {

	@Key
	private int page;
	@Key
	private List<PackageSummary> registered;
	
	public PackageListResponse() {
		page = 0;
		registered = new LinkedList<>();
	}
	
	public PackageListResponse(int page, List<PackageSummary> registered) {
		this.page = page;
		this.registered = registered;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public List<PackageSummary> getRegistered() {
		return registered;
	}
	
	public void setRegistered(List<PackageSummary> registered) {
		this.registered = registered;
	}
}
