package com.coherentsolutions.java.webauto.section01.advanced;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Advanced Example: Cookie Consent Management
 * 
 * Real-world scenario: Modern websites require cookie consent management
 * This example demonstrates handling GDPR-compliant cookie preferences
 */
public class Ex06CookieConsentTest {

    private WebDriver driver;
    private static final String URL = "https://the-internet.herokuapp.com/checkboxes";

    @BeforeMethod
    public void openBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void cookieConsentManagementTest() {
        // Simulate cookie consent dialog with checkboxes
        Map<String, Boolean> cookiePreferences = new HashMap<>();
        cookiePreferences.put("necessary", true);      // Always true
        cookiePreferences.put("analytics", false);     // User choice
        cookiePreferences.put("marketing", false);     // User choice
        cookiePreferences.put("preferences", true);    // User choice

        // Process each cookie type
        cookiePreferences.forEach((cookieType, shouldAccept) -> {
            if (cookieType.equals("necessary")) {
                // Verify necessary cookies can't be unchecked
                WebElement necessaryCheckbox = findCookieCheckbox(cookieType);
                Assert.assertTrue(necessaryCheckbox.isSelected(), "Necessary cookies should always be selected");
                Assert.assertFalse(necessaryCheckbox.isEnabled(), "Necessary cookies should be disabled for editing");
            } else {
                setCookieConsent(cookieType, shouldAccept);
            }
        });

        // Save preferences
        saveCookiePreferences();
        
        // Verify preferences were saved
        verifyCookiePreferencesSaved(cookiePreferences);
    }

    @Test
    public void acceptAllCookiesTest() {
        // Quick accept all functionality
        WebElement acceptAllButton = driver.findElement(By.id("accept-all-cookies"));
        acceptAllButton.click();

        // Verify all checkboxes are selected
        String[] cookieTypes = {"necessary", "analytics", "marketing", "preferences"};
        for (String cookieType : cookieTypes) {
            WebElement checkbox = findCookieCheckbox(cookieType);
            Assert.assertTrue(checkbox.isSelected(), 
                String.format("%s cookies should be selected after 'Accept All'", cookieType));
        }
    }

    @Test
    public void rejectAllOptionalCookiesTest() {
        // Reject all except necessary
        WebElement rejectAllButton = driver.findElement(By.id("reject-optional-cookies"));
        rejectAllButton.click();

        // Verify only necessary cookies are selected
        Assert.assertTrue(findCookieCheckbox("necessary").isSelected(), "Necessary cookies should remain selected");
        Assert.assertFalse(findCookieCheckbox("analytics").isSelected(), "Analytics cookies should be deselected");
        Assert.assertFalse(findCookieCheckbox("marketing").isSelected(), "Marketing cookies should be deselected");
        Assert.assertFalse(findCookieCheckbox("preferences").isSelected(), "Preference cookies should be deselected");
    }

    private WebElement findCookieCheckbox(String cookieType) {
        // In real scenario, this would find actual cookie consent checkboxes
        // For demo, using the test page checkboxes
        return driver.findElement(By.xpath(String.format("//input[@type='checkbox'][@name='%s-cookies']", cookieType)));
    }

    private void setCookieConsent(String cookieType, boolean consent) {
        WebElement checkbox = findCookieCheckbox(cookieType);
        if (checkbox.isSelected() != consent) {
            checkbox.click();
        }
    }

    private void saveCookiePreferences() {
        WebElement saveButton = driver.findElement(By.id("save-cookie-preferences"));
        saveButton.click();
        
        // Wait for confirmation
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cookie-saved-message")));
    }

    private void verifyCookiePreferencesSaved(Map<String, Boolean> expectedPreferences) {
        // In real scenario, this might check localStorage or cookies
        // For demo, we'll verify the checkboxes maintain their state
        expectedPreferences.forEach((cookieType, expectedState) -> {
            WebElement checkbox = findCookieCheckbox(cookieType);
            Assert.assertEquals(checkbox.isSelected(), expectedState,
                String.format("Cookie preference for %s should be %s", cookieType, expectedState));
        });
    }
}