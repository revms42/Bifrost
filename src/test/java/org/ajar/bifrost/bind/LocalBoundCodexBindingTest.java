package org.ajar.bifrost.bind;

import static org.junit.Assert.*;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.ajar.bifrost.monitoring.FilterGroup;
import org.ajar.bifrost.monitoring.PathFilter;
import org.ajar.bifrost.network.Source;
import org.ajar.bifrost.network.Source.SourceType;
import org.ajar.bifrost.tracking.CodexDifference;
import org.ajar.bifrost.tracking.LocalBoundCodex;
import org.junit.Test;

public class LocalBoundCodexBindingTest {

	@Test
	public void testRoundTrip() {
		File folder = new File("folder");
		LocalBoundCodex codex = new LocalBoundCodex("myCodex", folder, 1);
		
		codex.addLeaf(new File(folder, "myFile.xml"));
		
		PathFilter filter = new PathFilter((new File("test.xml")).toPath());
		
		FilterGroup group = new FilterGroup(filter);
		codex.setFilter(group);
		
		Source location = new Source();
		location.setProtocol(SourceType.LOCAL);
		location.setLocation("/tmp");
		codex.setRemoteLocation(location);
		
		StringWriter writer = new StringWriter();
		JAXB.marshal(codex, writer);
		
		String marshalled = writer.toString();
		System.out.println(marshalled);
		
		StringReader reader = new StringReader(marshalled);
		LocalBoundCodex newCodex = JAXB.unmarshal(reader, LocalBoundCodex.class);
		
		writer = new StringWriter();
		JAXB.marshal(newCodex, writer);
		System.out.println(writer.toString());
		
		assertEquals("New codex does not equal old codex!", codex, newCodex);
		
		assertEquals("Codex filters are not the same!", codex.getFilter(), newCodex.getFilter());
		
		CodexDifference diff = codex.describeDifference(newCodex);
		
		assertTrue("New codex does not have the same leaves as the old!", diff.identical());
	}

}
