package SiteMapTest;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class ZoomTvEnSitemap {
    private static final String EXCEL_FILE_PATH = "ZoomTvEnSitemapAmp.xlsx";
    private WebDriver driver;

    private Workbook workbook;
    private ExtentReports extent;
    private ExtentTest test;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("ZoomTvEnSiteMapAmp.html");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        workbook = new XSSFWorkbook();
        createExcelHeader(workbook);
        test = extent.createTest("AMP Validation Test");
    }

    @Test
    public void sitemapAmp() {
        try {
            Sheet sheet = workbook.getSheet("ZoomTvEnSitmemapAmp");
            int rowNum = sheet.getLastRowNum() + 1;

            String xmlLink = "https://www.zoomtventertainment.com/feeds/google-news-sitemap-zoom.xml";
            driver.get(xmlLink);
            System.out.println("Navited to Chrome");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new URL(xmlLink).openStream());


            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new DefaultNamespaceContext());


            XPathExpression expr = xpath.compile("//url/loc");


            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            int maxUrlsToPrint = 100;
            for (int i = 0; i < nodeList.getLength() && i < maxUrlsToPrint; i++) {
                String url = nodeList.item(i).getTextContent();
                String ampUrl = url + "/amp";
                System.out.println(ampUrl);
                driver.navigate().to("https://search.google.com/test/amp");
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(360));

                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='url'][jsname='YPqjbf']")));

                WebElement urlInput = driver.findElement(By.cssSelector("input[type='url'][jsname='YPqjbf']"));
                urlInput.sendKeys(ampUrl);
                WebElement testUrlElement = driver.findElement(By.cssSelector(".UfBne"));
                testUrlElement.click();
                Thread.sleep(10000);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".tD4kDf")));

                WebElement validationStatusElement = driver.findElement(By.cssSelector(".CC5fre"));
                String validationStatus = validationStatusElement.getText();
                System.out.println(validationStatus);
                test.log(Status.INFO, "URL: " + ampUrl);
                test.log(Status.INFO, "Validation Status: " + validationStatus);

                if (validationStatus.equalsIgnoreCase("PASS")) {
                    test.log(Status.PASS, "URL: " + ampUrl);
                    test.log(Status.PASS, "Validation Status: " + validationStatus);
                } else if (validationStatus.equalsIgnoreCase("FAIL")) {
                    test.log(Status.FAIL, "URL: " + ampUrl);
                    test.log(Status.FAIL, "Validation Status: " + validationStatus);
                } else {
                    test.log(Status.INFO, "URL: " + ampUrl);
                    test.log(Status.INFO, "Validation Status: " + validationStatus);
                }

                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(rowNum - 1);
                dataRow.createCell(1).setCellValue(ampUrl);
                dataRow.createCell(2).setCellValue(validationStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterMethod
    public void tearDown() {
        try {
            FileOutputStream outputStream = new FileOutputStream(EXCEL_FILE_PATH);
            workbook.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            extent.flush();
            driver.quit();
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

    private void createExcelHeader(Workbook workbook) {
        Sheet sheet = workbook.createSheet("ZoomTvEnSitmemapAmp");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("SL No.");
        headerRow.createCell(1).setCellValue("URL");
        headerRow.createCell(2).setCellValue("Validation Status");
    }
}
