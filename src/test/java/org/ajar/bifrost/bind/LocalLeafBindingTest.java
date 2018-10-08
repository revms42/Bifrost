package org.ajar.bifrost.bind;

import static org.junit.Assert.*;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.ajar.bifrost.tracking.LocalFileLeaf;
import org.junit.Test;

public class LocalLeafBindingTest {

	@Test
	public void testRoundTrip() {
		File folder = new File("folder");
		LocalFileLeaf leaf = new LocalFileLeaf(folder, new File("folder/test.xml"));
		
		StringWriter writer = new StringWriter();
		JAXB.marshal(leaf, writer);
		
		String marshalled = writer.toString();
		System.out.println(marshalled);
		
		StringReader reader = new StringReader(marshalled);
		LocalFileLeaf newLeaf = JAXB.unmarshal(reader, LocalFileLeaf.class);
		
		assertEquals("New leaf does not equal old filter!", leaf, newLeaf);
	}

}
