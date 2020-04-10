package ssobjects.http;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Created by lee on 2/13/17.
 */
public class RequestTest extends TestCase
{
    public static final String HEADER =
            "POST /DriverProxy HTTP/1.1\n"+
            "Accept-Encoding: gzip,deflate\n"+
            "Content-Type: text/xml;charset=UTF-8\n"+
            "SOAPAction: \"http://ews.wrightexpress.com/DriverManagement/Ping\"\n"+
            "Content-Length: %d\n"+
            "Host: zergling.com:9000\n"+
            "Connection: Keep-Alive\n"+
            "User-Agent: Apache-HttpClient/4.1.1 (java 1.5)\n\n";
    public static final String CONTENT =
//            "foo\nbar";
            "<soapenv:Envelope xmlns:driv=\"http://ews.wrightexpress.com/DriverManagement/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"+
            "   <soapenv:Header><wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsse:UsernameToken wsu:Id=\"UsernameToken-51D711DBA3853F485514870386653183\"><wsse:Username>OCUSER</wsse:Username><wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">Set4data</wsse:Password><wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">rZOucjf1xrbPH7ikly0zpA==</wsse:Nonce><wsu:Created>2017-02-14T02:17:45.318Z</wsu:Created></wsse:UsernameToken></wsse:Security></soapenv:Header>\n"+
            "   <soapenv:Body>\n"+
            "      <driv:PingRequest>1</driv:PingRequest>\n"+
            "   </soapenv:Body>\n"+
            "</soapenv:Envelope>";

    public static Test suite() {return new TestSuite(RequestTest.class);}

    public void testTypeString()
    {
        //make sure that an enum.toString() returns the string of the enum as expected
        assertTrue("GET".equals(Request.Type.GET.toString()));
        assertTrue("POST".equals(Request.Type.POST.toString()));
    }
//    public void testGetHost() throws Exception
//    {
//        Request r = new Request();
//        StringBuffer stream = new StringBuffer(String.format(HEADER,CONTENT.length()));
//        stream.append(CONTENT);
//        StringReader reader = new StringReader(stream.toString());
//        System.out.println("Stream:\n["+stream.toString()+"]");
//        r.parseRequest(reader);
//        assertEquals("/DriverProxy",r.getUrl());
//        assertEquals("zergling.com:9000",r.getHeader("Host"));
//    }//ignore
    public void testReadContent() throws Exception
    {
        Request r = new Request();
        StringBuffer stream = new StringBuffer(String.format(HEADER,CONTENT.length()));
        stream.append(CONTENT);
        r.setContentLength(CONTENT.length());
        BufferedReader in = new BufferedReader(new StringReader(CONTENT));
        r.parseContent(in);
        assertTrue(r.getContent().equals(CONTENT));
    }
}
