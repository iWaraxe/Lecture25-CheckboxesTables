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
 * Ex05ExtractTableDataTest
 * This class demonstrates how to extract and print all data from a table using Selenium WebDriver.
 */
public class Ex05ExtractTableDataTest {

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
    public void extractTableDataTest() {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            for (WebElement cell : cells) {
                System.out.print(cell.getText() + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        // This method is for running the class directly if needed.
        Ex05ExtractTableDataTest test = new Ex05ExtractTableDataTest();
        test.openBrowser();
        test.extractTableDataTest();
        test.closeBrowser();
    }
}
