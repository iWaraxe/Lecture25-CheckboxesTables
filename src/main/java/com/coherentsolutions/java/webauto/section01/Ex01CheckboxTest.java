package com.coherentsolutions.java.webauto.section01;

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
 * Ex01CheckboxTest
 * This class demonstrates basic operations with checkboxes using Selenium WebDriver.
 */
public class Ex01CheckboxTest {

    private WebDriver driver;
    private static final String URL = "https://the-internet.herokuapp.com/checkboxes";

    private static final By CHECKBOX_1 = By.xpath(".//form[@id = 'checkboxes']/input[1]");
    private static final By CHECKBOX_2 = By.xpath(".//form[@id = 'checkboxes']/input[2]");

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
    public void checkboxDefaultConditionTest() {
        WebElement firstCheckboxElement = driver.findElement(CHECKBOX_1);
        WebElement secondCheckboxElement = driver.findElement(CHECKBOX_2);
        Assert.assertFalse(firstCheckboxElement.isSelected());
        Assert.assertTrue(secondCheckboxElement.isSelected());
    }

    @Test
    public void selectedCheckboxTest() {
        WebElement firstCheckboxElement = driver.findElement(CHECKBOX_1);
        WebElement secondCheckboxElement = driver.findElement(CHECKBOX_2);
        selectCheckbox(firstCheckboxElement);
        selectCheckbox(secondCheckboxElement);
        Assert.assertTrue(firstCheckboxElement.isSelected());
        Assert.assertTrue(secondCheckboxElement.isSelected());
    }

    private void selectCheckbox(WebElement checkboxElement) {
        if (!checkboxElement.isSelected()) {
            checkboxElement.click();
        }
    }

    public static void main(String[] args) {
        // This method is for running the class directly if needed.
        Ex01CheckboxTest test = new Ex01CheckboxTest();
        test.openBrowser();
        test.checkboxDefaultConditionTest();
        test.selectedCheckboxTest();
        test.closeBrowser();
    }
}
