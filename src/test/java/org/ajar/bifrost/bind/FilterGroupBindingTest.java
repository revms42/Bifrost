package org.ajar.bifrost.bind;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.ajar.bifrost.monitoring.FilterGroup;
import org.ajar.bifrost.monitoring.PathFilter;
import org.junit.Test;

public class FilterGroupBindingTest {

	@Test
	public void testRoundTrip() {
		PathFilter filter = new PathFilter((new File("test.xml")).toPath());
		
		FilterGroup group = new FilterGroup(filter);
		
		StringWriter writer = new StringWriter();
		JAXB.marshal(group, writer);
		
		String marshalled = writer.toString();
		System.out.println(marshalled);
		
		StringReader reader = new StringReader(marshalled);
		FilterGroup newGroup = JAXB.unmarshal(reader, FilterGroup.class);
		
		assertEquals("New group does not equal old group!", group.getFilters(), newGroup.getFilters());
	}
}
