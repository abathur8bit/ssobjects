package ssobjects.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by lee on 2/20/17.
 */
public class CSVParser
{
    protected HashMap<String,Integer> columnMap = new HashMap<>();
    protected HashMap<String,String> columnValues = new HashMap<>();
    protected ArrayList<String> columnIndex = new ArrayList<>();
    protected BufferedReader in;
    protected String currentLine;

    public CSVParser(Reader reader) throws IOException
    {
        in = new BufferedReader(reader);
        parseHeaders();
    }

    public void parseHeaders() throws IOException
    {
        String str = in.readLine();
        StringTokenizer tok = new StringTokenizer(str,",");
        Integer index = 0;
        while(tok.hasMoreTokens())
        {
            String columnName = tok.nextToken();
            columnMap.put(columnName,index);
            columnIndex.add(columnName);
            ++index;
        }
    }

    public boolean nextLine() throws IOException
    {
        currentLine = in.readLine();
        if(currentLine == null)
            return false;
        columnValues.clear();
        StringTokenizer tok = new StringTokenizer(currentLine,",");
        Integer index = 0;
        while(tok.hasMoreTokens())
        {
            String value = tok.nextToken();
            String key = columnIndex.get(index);
            columnValues.put(key,value);
            ++index;
        }
        return true;
    }

    public String getCurrentLine()
    {
        return currentLine;
    }

    public int columnCount()
    {
        return columnIndex.size();
    }

    public String getHeader(int index)
    {
        return columnIndex.get(index);
    }

    public String getValue(String column)
    {
        return columnValues.get(column);
    }
}
