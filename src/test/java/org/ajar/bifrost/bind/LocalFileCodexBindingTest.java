package org.ajar.bifrost.bind;

import static org.junit.Assert.*;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.ajar.bifrost.tracking.CodexDifference;
import org.ajar.bifrost.tracking.LocalFileCodex;
import org.junit.Test;

public class LocalFileCodexBindingTest {

	@Test
	public void testRoundTrip() throws Exception {
		File folder = new File("folder");
		LocalFileCodex codex = new LocalFileCodex("myCodex", folder, 1);
		
		codex.addLeaf(new File(folder, "myFile.xml"));
		
		StringWriter writer = new StringWriter();
		JAXB.marshal(codex, writer);
		
		String marshalled = writer.toString();
		System.out.println(marshalled);
		
		StringReader reader = new StringReader(marshalled);
		LocalFileCodex newCodex = JAXB.unmarshal(reader, LocalFileCodex.class);
		
		writer = new StringWriter();
		JAXB.marshal(newCodex, writer);
		System.out.println(writer.toString());
		
		assertEquals("New codex does not equal old codex!", codex, newCodex);
		
		CodexDifference diff = codex.describeDifference(newCodex);
		
		assertTrue("New codex does not have the same leaves as the old!", diff.identical());
	}

}
