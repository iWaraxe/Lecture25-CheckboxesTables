package com.coherentsolutions.java.webauto.section02;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ex01SortLastNameTest
 * This class demonstrates sorting the last names in a table using Selenium WebDriver.
 */
public class Ex01SortLastNameTest {

    private WebDriver driver;

    private static final String URL = "https://the-internet.herokuapp.com/tables";

    private static final By LAST_NAME_HEADER = By.xpath(".//table[@id = 'table1']//span[contains(text(), 'Last Name')]");
    private static final By LAST_NAMES = By.cssSelector("#table1 td:nth-child(1)");

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
    public void sortLastNameTest() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<String> lastNames = driver.findElements(LAST_NAMES).stream().map(WebElement::getText)
                .collect(Collectors.toList());
        System.out.println("Last Names: " + lastNames);
        Collections.sort(lastNames);
        driver.findElement(LAST_NAME_HEADER).click();
        List<String> sortedLastNames = driver.findElements(LAST_NAMES).stream().map(WebElement::getText)
                .collect(Collectors.toList());
        System.out.println("Sorted Last Names: " + sortedLastNames);
        Assert.assertEquals(sortedLastNames, lastNames);
    }

    public static void main(String[] args) {
        // This method is for running the class directly if needed.
        Ex01SortLastNameTest test = new Ex01SortLastNameTest();
        test.openBrowser();
        test.sortLastNameTest();
        test.closeBrowser();
    }
}
