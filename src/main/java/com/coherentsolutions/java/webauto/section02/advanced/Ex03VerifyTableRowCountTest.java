package com.coherentsolutions.java.webauto.section02.advanced;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Ex03VerifyTableRowCountTest
 * This class demonstrates how to verify the number of rows in a table using Selenium WebDriver.
 */
public class Ex03VerifyTableRowCountTest {

    private WebDriver driver;

    private static final String URL = "https://the-internet.herokuapp.com/tables";
    private static final By TABLE_ROWS = By.xpath(".//table[@id = 'table1']/tbody/tr");

    @BeforeMethod
    public void openBrowser() {
        // Set up the WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Initialize the WebDriver instance
        driver = new ChromeDriver();
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void verifyTableRowCountTest() {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        Assert.assertEquals(rows.size(), 4); // Assumes the table has 4 rows.
    }

    public static void main(String[] args) {
        // This method is for running the class directly if needed.
        Ex03VerifyTableRowCountTest test = new Ex03VerifyTableRowCountTest();
        test.openBrowser();
        test.verifyTableRowCountTest();
        test.closeBrowser();
    }
}
