package com.coherentsolutions.java.webauto.section02;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataTableTest {

    private WebDriver driver;

    private static final String URL = "https://the-internet.herokuapp.com/tables";

    private static final By LAST_NAME_HEADER = By.xpath(".//table[@id = 'table1']//span[contains(text(), 'Last Name')]");
    private static final By FIRST_NAME_HEADER = By.xpath(".//table[@id = 'table1']//span[contains(text(), 'First Name')]");
    private static final By LAST_NAMES = By.cssSelector("#table1 td:nth-child(1)");
    private static final By FIRST_NAMES = By.cssSelector("#table1 td:nth-child(2)");

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
        List<String> lastNames = driver.findElements(LAST_NAMES).stream()
                .map(webElement -> webElement.getText())
                .collect(Collectors.toList());
        System.out.println(lastNames);
        Collections.sort(lastNames);
        driver.findElement(LAST_NAME_HEADER).click();
        List<String> sortedLastNames = driver.findElements(LAST_NAMES).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        System.out.println(sortedLastNames);
        Assert.assertEquals(sortedLastNames, lastNames);
    }

    @Test
    public void sortFirstNameTest() {
        List<String> firstNames = driver.findElements(FIRST_NAMES).stream().map(WebElement::getText)
                .collect(Collectors.toList());
        Collections.sort(firstNames);
        driver.findElement(FIRST_NAME_HEADER).click();
        List<String> sortedLastNames = driver.findElements(FIRST_NAMES).stream().map(WebElement::getText)
                .collect(Collectors.toList());
        Assert.assertEquals(sortedLastNames, firstNames);
    }
}
