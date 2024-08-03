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

/**
 * Ex04CheckboxLabelTest
 * This class demonstrates how to find and interact with checkboxes using associated labels in Selenium WebDriver.
 */
public class Ex04CheckboxLabelTest {

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
    public void selectCheckboxByLabelTest() {
        // Not working example just to show the code snippet
        WebElement checkboxLabel = driver.findElement(By.xpath(".//form[@id = 'checkboxes']/input[1]/following-sibling::label"));
        checkboxLabel.click();
        WebElement checkbox = driver.findElement(By.xpath(".//form[@id = 'checkboxes']/input[1]"));
        Assert.assertTrue(checkbox.isSelected());
    }

    public static void main(String[] args) {
        // This method is for running the class directly if needed.
        Ex04CheckboxLabelTest test = new Ex04CheckboxLabelTest();
        test.openBrowser();
        test.selectCheckboxByLabelTest();
        test.closeBrowser();
    }
}
