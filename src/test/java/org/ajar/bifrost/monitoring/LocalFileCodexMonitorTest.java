package org.ajar.bifrost.monitoring;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.ajar.bifrost.tracking.CodexDifference;
import org.ajar.bifrost.tracking.LocalFileCodex;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalFileCodexMonitorTest {
	
	private static long performanceSpeed;
	private static LocalFileCodex codex;
	private static LocalFileCodexMonitor monitor;
	
	private static File tempDir;
	
	private File newFile;

	private static void mkDir(File file) {
		file.delete();
		file.mkdirs();
	}
	
	/**
	 * Speed of the watcher varies *greatly* based on OS. Need a baseline before we start. 
	 * @throws Exception
	 */
	@BeforeClass
	public static void testService() throws Exception {
		String name = LocalFileCodexMonitorTest.class.getSimpleName();
		tempDir = File.createTempFile(name, "");
		mkDir(tempDir);
		
		WatchService service = FileSystems.getDefault().newWatchService();
		
		WatchKey key = tempDir.toPath().register(service, ENTRY_CREATE);
		
		System.out.println("Testing WatchService performance....");
		File testFile = new File(tempDir, "speedTest.txt");
		for(int i = 0; i < 5; i++) {
			long startTime = System.currentTimeMillis();
			testFile.createNewFile();
			key = service.take();
			long endTime = System.currentTimeMillis();
			long performance = endTime - startTime;
			if(performance > performanceSpeed) performanceSpeed = performance;
			testFile.delete();
			key.reset();
		}
		
		performanceSpeed = performanceSpeed + (performanceSpeed/10);
		System.out.println("WatchService performance: " + performanceSpeed);
		
		codex = new LocalFileCodex(name, tempDir, 0);
		monitor = new LocalFileCodexMonitor(codex);
	}

	@Test
	public void testAddedFile() {
		monitor.startMonitor(null);
		
		newFile = new File(tempDir, "addedFile.txt");
		
		try {
			newFile.createNewFile();
			Thread.sleep(performanceSpeed);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Could not find the added leaves!", 1, difference.addedLeaves().size());
		assertEquals("Should be no removed leaves!", 0, difference.removedLeaves().size());
		assertEquals("Should be no modified leaves!", 0, difference.changedLeaves().size());
		
		assertEquals("Paths do not match!", tempDir.toPath().relativize(newFile.toPath()), difference.addedLeaves().iterator().next().getRelativePath());
	}
	
	@Test
	public void testAddedFilterFile() {
		newFile = new File(tempDir, "addedFile.txt");
		
		PathFilter filter = new PathFilter(tempDir.toPath().relativize(newFile.toPath()));
		monitor.startMonitor(filter);
		
		try {
			newFile.createNewFile();
			Thread.sleep(performanceSpeed);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Should be no added leaves!", 0, difference.addedLeaves().size());
		assertEquals("Should be no removed leaves!", 0, difference.removedLeaves().size());
		assertEquals("Should be no modified leaves!", 0, difference.changedLeaves().size());
	}
	
	@Test
	public void testDeletedFile() {
		newFile = new File(tempDir, "deletedFile.txt");
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		codex.addLeaf(newFile);
		monitor.startMonitor(null);
		newFile.delete();
		
		try {
			Thread.sleep(performanceSpeed);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Should be no added leaves!", 0, difference.addedLeaves().size());
		assertEquals("Removed leaves doesn't match expected!", 1, difference.removedLeaves().size());
		assertEquals("Should be no modified leaves!", 0, difference.changedLeaves().size());
		
		assertEquals("Paths do not match!", tempDir.toPath().relativize(newFile.toPath()), difference.removedLeaves().iterator().next().getRelativePath());
	}

	@Test
	public void testModifiedFile() throws IOException {
		newFile = new File(tempDir, "modified.txt");
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		newFile.setLastModified(System.currentTimeMillis() - 100000);
		
		codex.addLeaf(newFile);
		monitor.startMonitor(null);
		
		newFile.setLastModified(System.currentTimeMillis());
		
		try {
			Thread.sleep(performanceSpeed);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Should be no added leaves!", 0, difference.addedLeaves().size());
		assertEquals("Should be no removed!", 0, difference.removedLeaves().size());
		assertEquals("Modified leaves does not match expected!", 1, difference.changedLeaves().size());
		
		assertEquals("Paths do not match!", tempDir.toPath().relativize(newFile.toPath()), difference.changedLeaves().iterator().next().getRelativePath());
	}
	
	@Test
	public void testAddThenDeleteFile() {
		monitor.startMonitor(null);
		
		newFile = new File(tempDir, "addedFile.txt");
		
		try {
			newFile.createNewFile();
			newFile.delete();
			Thread.sleep(performanceSpeed);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Should be no added leaves!", 0, difference.addedLeaves().size());
		assertEquals("Should be no removed leaves!", 0, difference.removedLeaves().size());
		assertEquals("Should be no modified leaves!", 0, difference.changedLeaves().size());
	}
	
	@Test
	public void testAddThenDeleteFilteredFile() {
		newFile = new File(tempDir, "addedFile.txt");
		
		PathFilter filter = new PathFilter(tempDir.toPath().relativize(newFile.toPath()));
		monitor.startMonitor(filter);
		
		try {
			newFile.createNewFile();
			newFile.delete();
			Thread.sleep(performanceSpeed);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Should be no added leaves!", 0, difference.addedLeaves().size());
		assertEquals("Should be no removed leaves!", 0, difference.removedLeaves().size());
		assertEquals("Should be no modified leaves!", 0, difference.changedLeaves().size());
	}

	@Test
	public void testAddThenModifyFile() {
		newFile = new File(tempDir, "addedFile.txt");
		
		monitor.startMonitor(null);
		
		try {
			newFile.createNewFile();
			newFile.setLastModified(System.currentTimeMillis() + 100000);
			Thread.sleep(performanceSpeed);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Added leaves does not match expected!", 1, difference.addedLeaves().size());
		assertEquals("Should be no removed leaves!", 0, difference.removedLeaves().size());
		assertEquals("Should be no modified leaves!", 0, difference.changedLeaves().size());
		
		assertEquals("Paths do not match!", tempDir.toPath().relativize(newFile.toPath()), difference.addedLeaves().iterator().next().getRelativePath());
	}
	
	@Test
	public void testAddThenModifyFilteredFile() {
		newFile = new File(tempDir, "addedFile.txt");
		
		PathFilter filter = new PathFilter(tempDir.toPath().relativize(newFile.toPath()));
		monitor.startMonitor(filter);
		
		try {
			newFile.createNewFile();
			newFile.setLastModified(System.currentTimeMillis() + 100000);
			Thread.sleep(performanceSpeed);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Should be no added leaves!", 0, difference.addedLeaves().size());
		assertEquals("Should be no removed leaves!", 0, difference.removedLeaves().size());
		assertEquals("Should be no modified leaves!", 0, difference.changedLeaves().size());
	}
	
	@Test
	public void testModifyThenDeleteFile() throws IOException {
		newFile = new File(tempDir, "modified.txt");
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		newFile.setLastModified(System.currentTimeMillis() - 100000);
		
		codex.addLeaf(newFile);
		monitor.startMonitor(null);
		
		newFile.setLastModified(System.currentTimeMillis());
		newFile.delete();
		
		try {
			Thread.sleep(performanceSpeed);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Should be no added leaves!", 0, difference.addedLeaves().size());
		assertEquals("Removed leaves does not match expected!", 1, difference.removedLeaves().size());
		assertEquals("Should be no modified leaves!", 0, difference.changedLeaves().size());
		
		assertEquals("Paths do not match!", tempDir.toPath().relativize(newFile.toPath()), difference.removedLeaves().iterator().next().getRelativePath());
	}
	
	@Test
	public void testDeleteThenAddFile() {
		newFile = new File(tempDir, "deletedFile.txt");
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		codex.addLeaf(newFile);
		monitor.startMonitor(null);
		newFile.delete();
		
		try {
			Thread.sleep(performanceSpeed);
			newFile.createNewFile();
			Thread.sleep(performanceSpeed);
		} catch (InterruptedException | IOException e) {
			fail(e.getMessage());
		}
		
		CodexDifference difference = monitor.stopMonitor();
		
		assertEquals("Should be no added leaves!", 0, difference.addedLeaves().size());
		assertEquals("Should be no removed leaves", 0, difference.removedLeaves().size());
		assertEquals("Modified leaves does not match expected!", 1, difference.changedLeaves().size());
		
		assertEquals("Paths do not match!", tempDir.toPath().relativize(newFile.toPath()), difference.changedLeaves().iterator().next().getRelativePath());
	}
	
	@After
	public void tearDown() throws Exception {
		codex.removeLeaf(newFile);
		if(newFile.exists()) delete(newFile);
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		delete(tempDir);
	}
	
	private static void delete(File f) {
		if(f.isDirectory()) {
			for(File child : f.listFiles()) {
				delete(child);
			}
		}
		f.delete();
	}
}
