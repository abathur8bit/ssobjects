package ssobjects.util;

import junit.framework.TestCase;

import java.io.StringReader;

/**
 * Created by lee on 2/20/17.
 */
public class CSVParserTest extends TestCase
{
    static final String csvfile = "NAME,PHONE,MOOD\nlee,555-1234,happy\npauline,555-1111,sad";
    public void testHeader() throws Exception
    {
        StringReader reader = new StringReader(csvfile);
        CSVParser parser = new CSVParser(reader);

        assertTrue(parser.columnMap.get("NAME") != null);
        assertEquals("NAME",parser.getHeader(0));
        assertEquals("MOOD",parser.getHeader(2));
        assertEquals("PHONE",parser.getHeader(1));
    }

    public void testNextLine() throws Exception
    {
        StringReader reader = new StringReader(csvfile);
        CSVParser parser = new CSVParser(reader);

        assertTrue(parser.nextLine());
        assertEquals("lee",parser.getValue("NAME"));
        assertEquals("happy",parser.getValue("MOOD"));
        assertEquals("555-1234",parser.getValue("PHONE"));

        assertTrue(parser.nextLine());
        assertEquals("pauline",parser.getValue("NAME"));
        assertEquals("sad",parser.getValue("MOOD"));
        assertEquals("555-1111",parser.getValue("PHONE"));

        assertFalse(parser.nextLine());
    }
}
