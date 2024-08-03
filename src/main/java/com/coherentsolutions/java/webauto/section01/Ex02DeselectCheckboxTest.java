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
 * Ex02DeselectCheckboxTest
 * This class demonstrates how to deselect checkboxes using Selenium WebDriver.
 */
public class Ex02DeselectCheckboxTest {

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
    public void deselectCheckboxTest() {
        WebElement firstCheckboxElement = driver.findElement(CHECKBOX_1);
        WebElement secondCheckboxElement = driver.findElement(CHECKBOX_2);
        deselectCheckbox(firstCheckboxElement);
        deselectCheckbox(secondCheckboxElement);
        Assert.assertFalse(firstCheckboxElement.isSelected());
        Assert.assertFalse(secondCheckboxElement.isSelected());
    }

    private void deselectCheckbox(WebElement checkboxElement) {
        if (checkboxElement.isSelected()) {
            checkboxElement.click();
        }
    }

    public static void main(String[] args) {
        // This method is for running the class directly if needed.
        Ex02DeselectCheckboxTest test = new Ex02DeselectCheckboxTest();
        test.openBrowser();
        test.deselectCheckboxTest();
        test.closeBrowser();
    }
}

