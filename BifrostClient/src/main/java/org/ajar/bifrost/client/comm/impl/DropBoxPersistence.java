package org.ajar.bifrost.client.comm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.comm.PersistenceClient;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.files.WriteMode;

public class DropBoxPersistence implements PersistenceClient {

	private static final String ACCESS_TOKEN_KEY = "dropbox.token";
	private static final String CLIENT_IDENTIFIER_KEY = "dropbox.identifier";
	private static String ACCESS_TOKEN = "";
	private static String CLIENT_IDENTIFIER = "";
	public static final String PROTOCOL_PREFIX = "dropbox://";
	
	public static String fromLocation(String location) {
		return location.replace(PROTOCOL_PREFIX, "");
	}
	
	public static String toLocation(String file) {
		return PROTOCOL_PREFIX + file;
	}
	
	private DbxClientV2 client;
	
	@Override
	public String prefixLocation(String location) {
		return toLocation(location);
	}
	
	@Override
	public String trimProtocol(String location) {
		return fromLocation(location);
	}
	
	@Override
	public List<String> configurationKeys() {
		return Arrays.asList(new String[]{ACCESS_TOKEN_KEY, CLIENT_IDENTIFIER_KEY});
	}
	
	@Override
	public void setConfigurationOption(String key, String value) {
		if(key.contentEquals(ACCESS_TOKEN_KEY)) {
			ACCESS_TOKEN = value;
		} else if(key.contentEquals(CLIENT_IDENTIFIER_KEY)) {
			CLIENT_IDENTIFIER = value;
		}
	}
	
	@Override
	public void loadConfiguration() {
		DbxRequestConfig config = DbxRequestConfig.newBuilder(CLIENT_IDENTIFIER).build();
		client = new DbxClientV2(config, ACCESS_TOKEN);
	}
	
	@Override
	public boolean canHandle(String location) {
		return location.startsWith(PROTOCOL_PREFIX);
	}

	@Override
	public boolean readFromRemote(OutputStream streamOut, String location) throws IOException, OperationNotSupportedException {
		if(client == null) {
			throw new OperationNotSupportedException("Dropbox isn't configured");
		}
		
		try(streamOut) {
			client.files().downloadBuilder(fromLocation(location)).download(streamOut);
		} catch (DownloadErrorException e) {
			if(e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
				return false;
			} else {
				System.err.println("Error with location '" + fromLocation(location) + "'");
				e.printStackTrace();
			}
		} catch (DbxException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public boolean writeToRemote(InputStream streamIn, String location) throws IOException, OperationNotSupportedException {
		if(client == null) {
			throw new OperationNotSupportedException("Dropbox isn't configured");
		}

		try(streamIn) {
			UploadUploader uploader = client.files().uploadBuilder(fromLocation(location))
				.withMode(WriteMode.OVERWRITE)
				.withClientModified(new Date())
				.start();
			
			System.out.println("Uploaded " + streamIn.transferTo(uploader.getOutputStream()) + " bytes");
			uploader.finish();
		} catch (DbxException e) {
			throw new IOException(e);
		}
		return true;
	}

	@Override
	public InputStream readStreamFromLocal(String location) throws OperationNotSupportedException {
		throw new OperationNotSupportedException("Dropbox cannot read from local files.");
	}

	@Override
	public OutputStream writeStreamToLocal(String location) throws OperationNotSupportedException {
		throw new OperationNotSupportedException("Dropbox cannot read from local files.");
	}

	@Override
	public void deleteFromLocal(String location) throws OperationNotSupportedException, IOException {
		throw new OperationNotSupportedException("Dropbox cannot read from local files.");
	}

	@Override
	public void deleteFromRemote(String location) throws OperationNotSupportedException, IOException {
		if(client == null) {
			throw new OperationNotSupportedException("Dropbox isn't configured");
		}
		
		try {
			client.files().deleteV2(fromLocation(location));
		} catch (DbxException e) {
			throw new IOException(e);
		}
	}

}
