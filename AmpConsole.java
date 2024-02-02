package SiteMapTest;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class AmpConsole {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void sitemapAmp() throws XPathExpressionException {
        try {
            String xmlLink = "https://telugu.timesnownews.com/google-news-sitemap-te.xml";
            driver.get(xmlLink);
            System.out.println("Navited to Chrome");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new URL(xmlLink).openStream());


            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new TeluguSitemap.DefaultNamespaceContext());

            XPathExpression expr = xpath.compile("//url/loc");

            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            int maxUrlsToPrint = 100;
            for (int i = 0; i < nodeList.getLength() && i < maxUrlsToPrint; i++) {
                String url = nodeList.item(i).getTextContent();
                if (url.contains("/web-stories/")) {
                    System.out.println("Skipping validation for URL: " + url);
                    continue;
                }
                String ampUrl = url + "/amp#development=1";
                System.out.println(ampUrl);
                driver.get(ampUrl);
                LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
                for (LogEntry entry : logEntries) {
                    System.out.println("Message: " + entry.getMessage());
                    System.out.println("Level: " + entry.getLevel());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DefaultNamespaceContext implements NamespaceContext {
        @Override
        public String getNamespaceURI(String prefix) {
            if ("xmlns".equals(prefix)) {
                return "http://www.sitemaps.org/schemas/sitemap/0.9";
            } else if ("xmlns:xhtml".equals(prefix)) {
                return "http://www.w3.org/1999/xhtml";
            } else if ("xmlns:news".equals(prefix)) {
                return "http://www.google.com/schemas/sitemap-news/0.9";
            } else if ("xmlns:image".equals(prefix)) {
                return "http://www.google.com/schemas/sitemap-image/1.1";
            }
            return null;
        }

        @Override
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Iterator<String> getPrefixes(String uri) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    @AfterMethod
    public void tearUp() {
        driver.close();
        driver.quit();
    }
}