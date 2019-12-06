package org.ajar.bifrost.server;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.google.gson.Gson;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.ajar.bifrost.core.model.call.PackageListRequest;
import org.ajar.bifrost.core.model.call.RegisterPackage;
import org.ajar.bifrost.server.service.PackageRepository;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class BifrostServerApplicationTests {

	private final String[] tests = {"test1", "test2", "test3"};
	private final String[] locations = {"http://mystorage/test1.xml", "http://mystorage/test2.xml", "http://mystorage/test3.xml"};
	private final String[] actives = {"8080", "1234", "508080"};
	
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PackageRepository repository;
	
	@Test
	public void testPackageControllerQuery() throws Exception {
        this.mockMvc.perform(get("/query/list").param("page", "0").param("pageSize", "-1"))
        	.andExpect(jsonPath("page").value("0")).andExpect(jsonPath("registered").isEmpty());
        
        this.mockMvc.perform(get("/query/update").param("name", tests[0]).param("location", locations[0]))
    		.andExpect(jsonPath("name").value(tests[0]))
    		.andExpect(jsonPath("version").value("1"))
    		.andExpect(jsonPath("location").value(locations[0]))
    		.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/query/register").param("name", tests[0]).param("location", actives[0]))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("1"))
			.andExpect(jsonPath("location").value("127.0.0.1:" + actives[0]))
			.andExpect(jsonPath("active").value("true"));
        
        this.mockMvc.perform(get("/query/update").param("name", tests[0]).param("location", locations[0]))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("2"))
			.andExpect(jsonPath("location").value(locations[0]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/set-version").param("name", tests[0]).param("version", "3"))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("3"))
			.andExpect(jsonPath("location").value(locations[0]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/query/delete").param("name", tests[0]).param("location", locations[0]))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("3"))
			.andExpect(jsonPath("location").value(locations[0]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/query/list").param("page", "0").param("pageSize", "-1"))
    		.andExpect(jsonPath("page").value("0")).andExpect(jsonPath("registered").isEmpty());
        
        this.mockMvc.perform(get("/query/update").param("name", tests[0]).param("location", locations[0]))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("1"))
			.andExpect(jsonPath("location").value(locations[0]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/query/update").param("name", tests[1]).param("location", locations[1]))
			.andExpect(jsonPath("name").value(tests[1]))
			.andExpect(jsonPath("version").value("1"))
			.andExpect(jsonPath("location").value(locations[1]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/query/update").param("name", tests[2]).param("location", locations[2]))
			.andExpect(jsonPath("name").value(tests[2]))
			.andExpect(jsonPath("version").value("1"))
			.andExpect(jsonPath("location").value(locations[2]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/query/list").param("page", "0").param("pageSize", "3"))
    		.andExpect(jsonPath("page").value("0"))
    		.andExpect(jsonPath("registered").isArray());
        
        repository.deleteAll();
	}

	@Test
	public void testPackageController() throws Exception {
		Gson gson = new Gson();
        this.mockMvc.perform(get("/list").content(gson.toJson(new PackageListRequest(0, -1))))
        	.andExpect(jsonPath("page").value("0")).andExpect(jsonPath("registered").isEmpty());
        
        this.mockMvc.perform(get("/update").content(gson.toJson(new RegisterPackage(tests[0], locations[0]))))
    		.andExpect(jsonPath("name").value(tests[0]))
    		.andExpect(jsonPath("version").value("1"))
    		.andExpect(jsonPath("location").value(locations[0]))
    		.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/register").content(gson.toJson(new RegisterPackage(tests[0], actives[0]))))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("1"))
			.andExpect(jsonPath("location").value("127.0.0.1:" + actives[0]))
			.andExpect(jsonPath("active").value("true"));
        
        this.mockMvc.perform(get("/update").content(gson.toJson(new RegisterPackage(tests[0], locations[0]))))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("2"))
			.andExpect(jsonPath("location").value(locations[0]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/set-version").param("name", tests[0]).param("version", "3"))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("3"))
			.andExpect(jsonPath("location").value(locations[0]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/delete").content(gson.toJson(new RegisterPackage(tests[0], locations[0]))))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("3"))
			.andExpect(jsonPath("location").value(locations[0]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/list").content(gson.toJson(new PackageListRequest(0, -1))))
    		.andExpect(jsonPath("page").value("0")).andExpect(jsonPath("registered").isEmpty());
        
        this.mockMvc.perform(get("/update").content(gson.toJson(new RegisterPackage(tests[0], locations[0]))))
			.andExpect(jsonPath("name").value(tests[0]))
			.andExpect(jsonPath("version").value("1"))
			.andExpect(jsonPath("location").value(locations[0]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/update").content(gson.toJson(new RegisterPackage(tests[1], locations[1]))))
			.andExpect(jsonPath("name").value(tests[1]))
			.andExpect(jsonPath("version").value("1"))
			.andExpect(jsonPath("location").value(locations[1]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/update").content(gson.toJson(new RegisterPackage(tests[2], locations[2]))))
			.andExpect(jsonPath("name").value(tests[2]))
			.andExpect(jsonPath("version").value("1"))
			.andExpect(jsonPath("location").value(locations[2]))
			.andExpect(jsonPath("active").value("false"));
        
        this.mockMvc.perform(get("/list").content(gson.toJson(new PackageListRequest(0, 3))))
    		.andExpect(jsonPath("page").value("0"))
    		.andExpect(jsonPath("registered").isArray());
        
        repository.deleteAll();
	}
}
