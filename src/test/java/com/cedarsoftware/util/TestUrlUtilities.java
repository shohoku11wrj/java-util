package com.cedarsoftware.util;

import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br/>
 *         Copyright (c) Cedar Software LLC
 *         <br/><br/>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br/><br/>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br/><br/>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
public class TestUrlUtilities
{
    private static final String httpsUrl = "https://www.myotherdrive.com";
    //private static final String httpsGoogleUrl = "https://www.google.com";
    private static final String domain = "myotherdrive.com";
    private static final String httpUrl = "http://tests.codetested.com/java-util/url-test.html";

    private static final String _expected = "<html>\n" +
            "<head>\n" +
            "\t<title>URL Utilities Rocks!</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>Hello, John!</h1>\n" +
            "</body>\n" +
            "</html>";

    @Test
    public void testConstructorIsPrivate() throws Exception
    {
        Class c = UrlUtilities.class;
        assertEquals(Modifier.FINAL, c.getModifiers() & Modifier.FINAL);

        Constructor<UrlUtilities> con = UrlUtilities.class.getDeclaredConstructor();
        assertEquals(Modifier.PRIVATE, con.getModifiers() & Modifier.PRIVATE);
        con.setAccessible(true);

        assertNotNull(con.newInstance());
    }

    @Test
    public void testGetContentFromUrlAsString() throws Exception
    {
        String content1 = UrlUtilities.getContentFromUrlAsString(httpsUrl, Proxy.NO_PROXY);
        String content2 = UrlUtilities.getContentFromUrlAsString(httpsUrl);

        assertTrue(content1.contains(domain));
        assertTrue(content2.contains(domain));

        assertEquals(content1, content2);

        String content3 = UrlUtilities.getContentFromUrlAsString(httpUrl, Proxy.NO_PROXY);
        String content4 = UrlUtilities.getContentFromUrlAsString(httpUrl);

        assertTrue(content3.equals(_expected));
        assertTrue(content4.equals(_expected));

        assertEquals(content3, content4);
    }

    @Test
    public void testGetContentFromUrl()
    {
        SSLSocketFactory f = UrlUtilities.buildNaiveSSLSocketFactory();
        HostnameVerifier v = UrlUtilities.NAIVE_VERIFIER;

        byte[] content1 = UrlUtilities.getContentFromUrl(httpsUrl, Proxy.NO_PROXY);
        byte[] content2 = UrlUtilities.getContentFromUrl(httpsUrl);
        byte[] content3 = UrlUtilities.getContentFromUrl(httpsUrl, Proxy.NO_PROXY, f, v);
        byte[] content4 = UrlUtilities.getContentFromUrl(httpsUrl, null, 0, null, null, true);
        byte[] content5 = UrlUtilities.getContentFromUrl(httpsUrl, null, null, Proxy.NO_PROXY, f, v);

        //  Allow for 3% difference between pages between requests to handle time and hash value changes.
        assertEquals(new String(content1), new String(content2));
        assertEquals(new String(content2), new String(content3));
        assertEquals(new String(content3), new String(content4));
        assertEquals(new String(content4), new String(content5));
        assertEquals(new String(content5), new String(content1));

        //TODO - add in when we find self-signing site.
//        assertNull(UrlUtilities.getContentFromUrl(httpsGoogleUrl, Proxy.NO_PROXY, null, null));
//        assertNull(UrlUtilities.getContentFromUrl(httpsGoogleUrl, null, null, Proxy.NO_PROXY, null, null));
//        assertNull(UrlUtilities.getContentFromUrl(httpsGoogleUrl, null, 0, null, null, false));

        byte[] content6 = UrlUtilities.getContentFromUrl(httpUrl, Proxy.NO_PROXY, null, null);
        byte[] content7 = UrlUtilities.getContentFromUrl(httpUrl, null, 0, null, null, false);
        byte[] content8 = UrlUtilities.getContentFromUrl(httpUrl, null, null, Proxy.NO_PROXY, null, null);

        assertEquals(new String(content6), new String(content7));
        assertEquals(new String(content7), new String(content8));

        // 404
        assertNull(UrlUtilities.getContentFromUrl(httpUrl + "/google-bucks.html", null, null, Proxy.NO_PROXY, null, null));
    }

    @Test
    public void testSSLTrust() throws Exception
    {
        String content1 = UrlUtilities.getContentFromUrlAsString(httpsUrl, Proxy.NO_PROXY);
        String content2 = UrlUtilities.getContentFromUrlAsString(httpsUrl, null, 0, null, null, true);

        assertTrue(content1.contains(domain));
        assertTrue(content2.contains(domain));

        assertEquals(content1, content2);

    }

    @Test
    public void testCookies() throws Exception
    {
        HashMap cookies = new HashMap();

        byte[] bytes1 = UrlUtilities.getContentFromUrl(httpUrl, null, 0, cookies, cookies, false);

        assertEquals(1, cookies.size());
        assertTrue(cookies.containsKey("codetested.com"));
        assertEquals(_expected, new String(bytes1));

        byte[] bytes2 = UrlUtilities.getContentFromUrl(httpsUrl, null, 0, cookies, cookies, false);

        assertEquals(2, cookies.size());
        assertTrue(cookies.containsKey("codetested.com"));
        assertTrue(cookies.containsKey(domain));

        assertTrue(new String(bytes2).contains("myotherdrive.com"));
    }

    @Test
    public void testHostName()
    {
        assertNotNull(UrlUtilities.getHostName());
    }

    @Test
    public void testGetConnection() throws Exception
    {
        HttpURLConnection c = (HttpURLConnection) UrlUtilities.getConnection(new URL("http://www.google.com"), null, 0, null, true, false, false, true);
        assertNotNull(c);
        c.connect();
        UrlUtilities.disconnect(c);
    }

    @Test
    public void testGetConnection1() throws Exception
    {
        HttpURLConnection c = (HttpURLConnection) UrlUtilities.getConnection("http://www.yahoo.com", true, false, false);
        assertNotNull(c);
        c.connect();
        UrlUtilities.disconnect(c);
    }

    @Test
    public void testGetConnection2() throws Exception
    {
        HttpURLConnection c = (HttpURLConnection) UrlUtilities.getConnection(new URL("http://www.yahoo.com"), true, false, false);
        assertNotNull(c);
        UrlUtilities.setTimeouts(c, 9000, 10000);
        c.connect();
        UrlUtilities.disconnect(c);
    }

    @Test
    public void testReferrer()
    {
        // Not much of a test
        UrlUtilities.setReferrer("http://www.google.com");
    }

    @Test
    public void testAgent()
    {
        UrlUtilities.setUserAgent("chrome");
        assertEquals("chrome", UrlUtilities.getUserAgent());
    }

    @Test
    public void testCookies2() throws Exception
    {
        Map cookies = new HashMap();
        Map gCookie = new HashMap();
        gCookie.put("param", new HashMap());
        cookies.put("google.com", gCookie);
        HttpURLConnection c = (HttpURLConnection) UrlUtilities.getConnection(new URL("http://www.google.com"), cookies, true, false, false, null, null, null);
        UrlUtilities.setCookies(c, cookies);
        c.connect();
        Map outCookies = new HashMap();
        UrlUtilities.getCookies(c, outCookies);
        UrlUtilities.disconnect(c);
    }
}
