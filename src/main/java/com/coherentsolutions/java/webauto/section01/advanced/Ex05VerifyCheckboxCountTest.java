package com.coherentsolutions.java.webauto.section01.advanced;

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
 * Ex05VerifyCheckboxCountTest
 * This class demonstrates how to verify the number of checkboxes present on a page using Selenium WebDriver.
 */
public class Ex05VerifyCheckboxCountTest {

    private WebDriver driver;
    private static final String URL = "https://the-internet.herokuapp.com/checkboxes";

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
    public void verifyCheckboxCountTest() {
        List<WebElement> checkboxes = driver.findElements(By.xpath(".//form[@id = 'checkboxes']/input"));
        Assert.assertEquals(checkboxes.size(), 2);
    }

    public static void main(String[] args) {
        // This method is for running the class directly if needed.
        Ex05VerifyCheckboxCountTest test = new Ex05VerifyCheckboxCountTest();
        test.openBrowser();
        test.verifyCheckboxCountTest();
        test.closeBrowser();
    }
}
