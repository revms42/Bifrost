package org.ajar.bifrost.bind;

import static org.junit.Assert.*;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.ajar.bifrost.monitoring.PathFilter;
import org.junit.Test;

public class PathFilterBindingTest {

	@Test
	public void testRoundTrip() {
		PathFilter filter = new PathFilter((new File("test.xml")).toPath());
		
		StringWriter writer = new StringWriter();
		JAXB.marshal(filter, writer);
		
		String marshalled = writer.toString();
		System.out.println(marshalled);
		
		StringReader reader = new StringReader(marshalled);
		PathFilter newFilter = JAXB.unmarshal(reader, PathFilter.class);
		
		assertEquals("New filter does not equal old filter!", filter, newFilter);
	}

}
