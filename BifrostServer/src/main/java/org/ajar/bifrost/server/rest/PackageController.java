package org.ajar.bifrost.server.rest;

import org.ajar.bifrost.bind.BifrostPathConst;
import org.ajar.bifrost.core.model.call.PackageListRequest;
import org.ajar.bifrost.core.model.call.PackageListResponse;
import org.ajar.bifrost.core.model.call.PackageSummary;
import org.ajar.bifrost.core.model.call.RegisterPackage;
import org.ajar.bifrost.core.model.data.RegisteredPackage;
import org.ajar.bifrost.server.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.toList;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
@RestController
public class PackageController implements BifrostPathConst {
	
	private final static Gson gson = new Gson();
	
	@Autowired
	PackageService packageService;

	@RequestMapping(LIST)
	public PackageListResponse getPackageList(@RequestBody String request) {
		return getPackageList(gson.fromJson(request, PackageListRequest.class));
	}
	
	@RequestMapping(LIST_QUERY)
	public PackageListResponse getPackageList(
			@RequestParam(value="page", defaultValue="0") String page, 
			@RequestParam(value="pageSize", defaultValue="-1") String pageSize
	) {
		int pageInteger = Integer.parseInt(page);
		int pageSizeInteger = Integer.parseInt(pageSize);
		
		return getPackageList(new PackageListRequest(pageInteger, pageSizeInteger));
	}
	
	private PackageListResponse getPackageList(PackageListRequest request) {
		int size = request.getPageSize();
		int page = request.getPage();
		
		List<RegisteredPackage> list = null;
		if(size == -1) {
			list = packageService.getPackageList();
		} else {
			int start = size * page;
			int end = size * (page + 1);
			list = packageService.getPackageSubList(start, end);
		}
		
		return new PackageListResponse(page, list.parallelStream().map(rp -> new PackageSummary(rp.getName(), rp.isActive())).collect(toList()));
	}
	
	@RequestMapping(INFO)
	public RegisteredPackage getPackageInfo(@RequestBody String name) {
		return packageService.getPackageByName(name);
	}
	
	@RequestMapping(INFO_QUERY)
	public RegisteredPackage getQueryPackageInfo(@RequestParam String name) {
		return getPackageInfo(name);
	}
	
	@RequestMapping(path = UPDATE, method = {POST, PUT, GET})
	public RegisteredPackage updatePackage(@RequestBody String mapping) {
		return updatePackage(gson.fromJson(mapping, RegisterPackage.class));
	}
	
	@RequestMapping(path = UPDATE_QUERY, method = {POST, GET})
	public RegisteredPackage updatePackage(@RequestParam String name, @RequestParam String location) {
		return updatePackage(new RegisterPackage(name, location));
	}
	
	private RegisteredPackage updatePackage(RegisterPackage mapping) {
		return packageService.updatePackage(mapping);
	}
	
	@RequestMapping(path = DELETE, method = {RequestMethod.DELETE, PUT, POST, GET})
	public RegisteredPackage deletePackage(@RequestBody String mapping) {
		return deletePackage(gson.fromJson(mapping, RegisterPackage.class));
	}
	
	@RequestMapping(path = DELETE_QUERY, method = {POST, GET})
	public RegisteredPackage deletePackage(@RequestParam String name, @RequestParam String location) {
		return deletePackage(new RegisterPackage(name, location));
	}
	
	private RegisteredPackage deletePackage(RegisterPackage mapping) {
		return packageService.deletePackage(mapping.getName());
	}
	
	@RequestMapping(path = REGISTER, method = {GET, POST, PUT})
	public RegisteredPackage getPackage(@RequestBody String mapping, HttpServletRequest request) {
		return getPackage(gson.fromJson(mapping, RegisterPackage.class), request);
	}
	
	@RequestMapping(path = REGISTER_QUERY, method = {POST, GET})
	public RegisteredPackage getPackage(@RequestParam String name, @RequestParam String location, HttpServletRequest request) {
		return getPackage(new RegisterPackage(name, location), request);
	}
	
	private RegisteredPackage getPackage(RegisterPackage mapping, HttpServletRequest request) {
		return packageService.activatePackage(mapping, request.getRemoteAddr());
	}
	
	@RequestMapping(path = SET_VERSION, method = {GET, POST})
	public RegisteredPackage setVersionQuery(@RequestParam String name, @RequestParam String version) {
		return packageService.setVersion(name, Long.parseLong(version));
	}
}
