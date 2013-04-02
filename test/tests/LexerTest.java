package tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LexerTest {

	static final String unclosedCommentMessage = ":-o Unclosed comment";
	static final ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();

	private File openTestXML(String name) throws IOException {
		File f = new File("test-resources/" + name);
		if (f.exists()) {
			return f;
		} else {
			throw new IOException(
			        String.format("File %s does not exists", name));
		}
	}

	private void checkOutputContains(String[] expected) {
		String output = tmpOut.toString();
		for (String line : expected) {
			boolean contains = output.contains(line);
			assertTrue(String.format("Could not find line \"%s\"", line),
			        contains);
		}
	}

	private void checkOutputNotContains(String[] expected) {
		String output = tmpOut.toString();
		for (String line : expected) {
			boolean contains = output.contains(line);
			assertTrue(
			        String.format("Line \"%s\" should not be in output", line),
			        !contains);
		}
	}

	private void checkLexanal(String expected) {
		try {
			File expectedFile = openTestXML(expected);
			File actualFile = new File("lexanal.xml");

			assertTrue("Actual file does not exists", actualFile.exists());
			assertNotNull("Could not open expected file", expectedFile);
			assertNotNull("Could not open actual file", actualFile);

			assertEquals("The lexanal differ!",
			        FileUtils.readFileToString(expectedFile, "utf-8"),
			        FileUtils.readFileToString(actualFile, "utf-8"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	private void compile(String programName) {
		compiler.Main.prgName = String.format("programs/%s", programName);
		compiler.lexanal.Main.exec();
	}

	@BeforeClass
	public static void testSetup() {
		System.setErr(new PrintStream(tmpOut));
	}

	@AfterClass
	public static void testCleanup() {
		System.setErr(System.err);
	}

	@Before
	public void cleanBuffer() {
		tmpOut.reset();
	}

	@Test
	public void testNestedCommentsBad() {
		compile("test-nested-comments-bad");
		String[] errors = { unclosedCommentMessage };
		checkOutputContains(errors);
	}

	@Test
	public void testNestedCommentsOk() {
		compile("test-nested-comments-ok");
		String[] errors = { unclosedCommentMessage };
		checkOutputNotContains(errors);
	}

	@Test
	public void testKeywords() {
		compile("test-keywords");
		checkLexanal("keywords.xml");
	}

	@Test
	public void testConstantsOk() {
		compile("test-constants-ok");
		checkLexanal("constants-ok.xml");
	}

	@Test
	public void testConstantBad() {
		compile("test-constants-bad");

		String[] errors = { ":-o Can not understand ''' (line: 0, column: 0)",
		        ":-o Can not understand ''' (line: 0, column: 3)",
		        ":-o Can not understand ''' (line: 2, column: 0)",
		        ":-o Can not understand 'č' (line: 2, column: 1)",
		        ":-o Can not understand ''' (line: 2, column: 2)",
		        ":-o Can not understand ''' (line: 3, column: 0)",
		        ":-o Can not understand 'ć' (line: 3, column: 1)",
		        ":-o Can not understand ''' (line: 3, column: 2)",
		        ":-o Can not understand ''' (line: 4, column: 0)",
		        ":-o Can not understand 'ž' (line: 4, column: 1)",
		        ":-o Can not understand ''' (line: 4, column: 2)",
		        ":-o Can not understand ''' (line: 5, column: 0)",
		        ":-o Can not understand 'š' (line: 5, column: 1)",
		        ":-o Can not understand ''' (line: 5, column: 2)",
		        ":-o Can not understand ''' (line: 6, column: 0)",
		        ":-o Can not understand 'đ' (line: 6, column: 1)",
		        ":-o Can not understand ''' (line: 6, column: 2)",
		        ":-o Can not understand ''' (line: 7, column: 0)",
		        ":-o Can not understand 'æ' (line: 7, column: 1)",
		        ":-o Can not understand ''' (line: 7, column: 2)",
		        ":-o Can not understand ''' (line: 8, column: 0)",
		        ":-o Can not understand 'å' (line: 8, column: 1)",
		        ":-o Can not understand ''' (line: 8, column: 2)",
		        ":-o Can not understand ''' (line: 9, column: 0)",
		        ":-o Can not understand 'ø' (line: 9, column: 1)",
		        ":-o Can not understand ''' (line: 9, column: 2)", };

		checkOutputContains(errors);
		checkLexanal("constants-bad.xml");
	}

	@Test
	public void testSymbolsOk() {
		compile("test-symbols-ok");
		checkLexanal("symbols-ok.xml");
	}

	@Test
	public void testAtomicTypes() {
		compile("test-atomic-types");
		checkLexanal("atomic-types.xml");
	}

	@Test
	public void testHelloWorld() {
		compile("hello-world");
		checkLexanal("hello-world.xml");
	}
}
