package org.ajar.bifrost.client.comm;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.List;

import org.ajar.bifrost.client.comm.BifrostHttpUrl.InfoQueryUrl;
import org.ajar.bifrost.client.comm.BifrostHttpUrl.ListQueryUrl;
import org.ajar.bifrost.client.comm.BifrostHttpUrl.RegisterQueryUrl;
import org.ajar.bifrost.client.comm.BifrostHttpUrl.SetVersionUrl;
import org.ajar.bifrost.core.model.call.PackageListResponse;
import org.ajar.bifrost.core.model.data.DefaultRegisteredPackage;
import org.ajar.bifrost.core.model.data.RegisteredPackage;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class BifrostHttpClient implements BifrostClient {
	
	private final static BifrostHttpClient singleton;
	
	static {
		singleton = new BifrostHttpClient();
		INSTANCES.add(singleton);
	}
	
	private final HttpTransport transportFactory;
	private final GsonFactory gsonFactory;
	
	private String serverUrl = "http://localhost";
	
	private HttpRequestFactory requestFactory;
	
	private BifrostHttpClient() {
		transportFactory = new NetHttpTransport();
		gsonFactory = new GsonFactory();
	}
	
	public static BifrostHttpClient singleton() {
		return singleton;
	}
	

	@Override
	public boolean canReach(String serverUrl) {
		return serverUrl.startsWith("http");
	}

	@Override
	public List<String> getConfigurationKeys() {
		return Collections.singletonList(KEY_SERVER);
	}

	@Override
	public void setConfigurationSetting(String key, String value) {
		if(key.equals(KEY_SERVER)) {
			serverUrl = value;
		}
	}

	@Override
	public void initializeClient() {
		requestFactory = transportFactory.createRequestFactory((HttpRequest request) -> {
			request.setParser(new JsonObjectParser(gsonFactory));
		});
		if(!serverUrl.startsWith("http")) serverUrl = "http://" + serverUrl;
	}

	@Override
	public PackageListResponse getPackageList(int page, int pageSize) throws IOException {
		if(requestFactory == null) throw new NullPointerException("The client has not been initialized!");
		
		ListQueryUrl url = (ListQueryUrl) BifrostHttpUrl.LIST_QUERY.buildUrl(serverUrl);
		url.page = page;
		url.pageSize = pageSize;
		
		HttpRequest request = requestFactory.buildGetRequest(url);
		
		try {
			HttpResponse response = request.execute();
			return response.parseAs(PackageListResponse.class);
		} catch(ConnectException | IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public RegisteredPackage getPackageInfo(String name) throws IOException {
		if(requestFactory == null) throw new NullPointerException("The client has not been initialized!");
		
		InfoQueryUrl url = (InfoQueryUrl) BifrostHttpUrl.INFO_QUERY.buildUrl(serverUrl);
		url.name = name;
		
		HttpRequest request = requestFactory.buildGetRequest(url);
		
		try {
			HttpResponse response = request.execute();
			return response.parseAs(DefaultRegisteredPackage.class);
		} catch(ConnectException | IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public RegisteredPackage updatePackage(String name, String location) throws IOException {
		if(requestFactory == null) throw new NullPointerException("The client has not been initialized!");
		
		RegisterQueryUrl url = (RegisterQueryUrl) BifrostHttpUrl.UPDATE_QUERY.buildUrl(serverUrl);
		url.name = name;
		url.location = location;
		
		HttpRequest request = requestFactory.buildGetRequest(url);
		
		try {
			HttpResponse response = request.execute();
			return response.parseAs(DefaultRegisteredPackage.class);
		} catch(ConnectException | IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public RegisteredPackage deletePackage(String name, String location) throws IOException {
		if(requestFactory == null) throw new NullPointerException("The client has not been initialized!");
		
		RegisterQueryUrl url = (RegisterQueryUrl) BifrostHttpUrl.DELETE_QUERY.buildUrl(serverUrl);
		url.name = name;
		url.location = location;
		
		HttpRequest request = requestFactory.buildGetRequest(url);
		
		try {
			HttpResponse response = request.execute();
			return response.parseAs(DefaultRegisteredPackage.class);
		} catch(ConnectException | IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public RegisteredPackage registerPackage(String name, String location) throws IOException {
		if(requestFactory == null) throw new NullPointerException("The client has not been initialized!");
		
		RegisterQueryUrl url = (RegisterQueryUrl) BifrostHttpUrl.REGISTER_QUERY.buildUrl(serverUrl);
		url.name = name;
		url.location = location;
		
		HttpRequest request = requestFactory.buildGetRequest(url);
		
		try {
			HttpResponse response = request.execute();
			return response.parseAs(DefaultRegisteredPackage.class);
		} catch(ConnectException | IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public RegisteredPackage setVersion(String name, long version) throws IOException {
		if(requestFactory == null) throw new NullPointerException("The client has not been initialized!");
		SetVersionUrl url = (SetVersionUrl) BifrostHttpUrl.SET_VERSION.buildUrl(serverUrl);
		url.name = name;
		url.version = version;
		
		HttpRequest request = requestFactory.buildGetRequest(url);
		
		try {
			HttpResponse response = request.execute();
			return response.parseAs(DefaultRegisteredPackage.class);
		} catch (ConnectException | IllegalArgumentException e) {
			return null;
		}
	}
}
