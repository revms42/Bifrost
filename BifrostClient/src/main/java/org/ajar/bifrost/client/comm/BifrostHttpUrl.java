package org.ajar.bifrost.client.comm;

import org.ajar.bifrost.bind.BifrostPathConst;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public enum BifrostHttpUrl implements BifrostPathConst {
	LIST(BifrostPathConst.LIST),
	LIST_QUERY(BifrostPathConst.LIST_QUERY) {
		@Override
		public ListQueryUrl buildUrl(String serverUrl) {
			return new ListQueryUrl(serverUrl + BifrostPathConst.LIST_QUERY);
		}
	},
	INFO(BifrostPathConst.INFO),
	INFO_QUERY(BifrostPathConst.INFO_QUERY){
		@Override
		public InfoQueryUrl buildUrl(String serverUrl) {
			return new InfoQueryUrl(serverUrl + BifrostPathConst.INFO_QUERY);
		}
	},
	UPDATE(BifrostPathConst.UPDATE),
	UPDATE_QUERY(BifrostPathConst.UPDATE_QUERY){
		@Override
		public RegisterQueryUrl buildUrl(String serverUrl) {
			return new RegisterQueryUrl(serverUrl + BifrostPathConst.UPDATE_QUERY);
		}
	},
	DELETE(BifrostPathConst.DELETE),
	DELETE_QUERY(BifrostPathConst.DELETE_QUERY){
		@Override
		public RegisterQueryUrl buildUrl(String serverUrl) {
			return new RegisterQueryUrl(serverUrl + BifrostPathConst.DELETE_QUERY);
		}
	},
	REGISTER(BifrostPathConst.REGISTER),
	REGISTER_QUERY(BifrostPathConst.REGISTER_QUERY){
		@Override
		public RegisterQueryUrl buildUrl(String serverUrl) {
			return new RegisterQueryUrl(serverUrl + BifrostPathConst.REGISTER_QUERY);
		}
	},
	SET_VERSION(BifrostPathConst.SET_VERSION){
		@Override
		public SetVersionUrl buildUrl(String serverUrl) {
			return new SetVersionUrl(serverUrl + BifrostPathConst.SET_VERSION);
		}
	};
	
	private final String relativePath;
	
	private BifrostHttpUrl(String relativePath) {
		this.relativePath = relativePath;
	}
	
	public GenericUrl buildUrl(String serverUrl) {
		return new GenericUrl(serverUrl + relativePath);
	}
	
	public class ListQueryUrl extends GenericUrl {
		public ListQueryUrl(String url) {
			super(url);
		}
		
		@Key
		public int page = 0;
		@Key
		public int pageSize = -1;
	}
	
	public class InfoQueryUrl extends GenericUrl {
		public InfoQueryUrl(String url) {
			super(url);
		}
		
		@Key
		public String name;
	}
	
	public class RegisterQueryUrl extends GenericUrl {
		public RegisterQueryUrl(String url) {
			super(url);
		}
		
		@Key
		public String name;
		@Key
		public String location;
	}
	
	public class SetVersionUrl extends GenericUrl {
		public SetVersionUrl(String url) {
			super(url);
		}
		
		@Key
		public String name;
		@Key
		public long version;
	}
}
