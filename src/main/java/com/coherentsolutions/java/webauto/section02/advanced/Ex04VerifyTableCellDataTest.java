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
 * Ex04VerifyTableCellDataTest
 * This class demonstrates how to verify the data in a specific cell of a table using Selenium WebDriver.
 */
public class Ex04VerifyTableCellDataTest {

    private WebDriver driver;

    private static final String URL = "https://the-internet.herokuapp.com/tables";
    private static final By TABLE_CELL = By.xpath(".//table[@id = 'table1']/tbody/tr[1]/td[2]");

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
    public void verifyTableCellDataTest() {
        WebElement cell = driver.findElement(TABLE_CELL);
        Assert.assertEquals(cell.getText(), "John"); // Assumes the expected cell data is "John".
    }

    public static void main(String[] args) {
        // This method is for running the class directly if needed.
        Ex04VerifyTableCellDataTest test = new Ex04VerifyTableCellDataTest();
        test.openBrowser();
        test.verifyTableCellDataTest();
        test.closeBrowser();
    }
}
