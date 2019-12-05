package org.ajar.bifrost.core.model.call;

public class PackageListRequest {
	
	private int page;
	private int pageSize;
	
	public PackageListRequest(int page, int pageSize) {
		this.page = page;
		this.pageSize = pageSize;
	}
	
	public int getPage() {
		return page;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
}
