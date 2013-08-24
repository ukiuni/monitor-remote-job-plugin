package org.ukiuni.callOtherJenkins.CallOtherJenkins;

import org.junit.Test;
import static org.junit.Assert.*;
import org.ukiuni.callOtherJenkins.CallOtherJenkins.util.TimeParser;

public class TestTimeParser {
	@Test
	public void testParseDate() {
		assertEquals(2 * 24 * 60 * 60 * 1000, TimeParser.parse("2d"));
	}

	@Test
	public void testParseHour() {
		assertEquals(4 * 60 * 60 * 1000, TimeParser.parse("4h"));
	}

	@Test
	public void testParseMinute() {
		assertEquals(24 * 60 * 1000, TimeParser.parse("24m"));
	}

	@Test
	public void testParseSecond() {
		assertEquals(180 * 1000, TimeParser.parse("180s"));
	}
}
