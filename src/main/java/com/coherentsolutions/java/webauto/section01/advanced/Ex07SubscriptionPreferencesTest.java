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
import java.util.List;

/**
 * Advanced Example: Email Subscription Preferences with Dependencies
 * 
 * Real-world scenario: Newsletter subscriptions with category dependencies
 * Demonstrates parent-child checkbox relationships and conditional logic
 */
public class Ex07SubscriptionPreferencesTest {

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
    public void parentChildCheckboxDependencyTest() {
        // Main newsletter checkbox controls sub-categories
        WebElement mainNewsletter = driver.findElement(By.id("subscribe-newsletter"));
        WebElement dailyDigest = driver.findElement(By.id("daily-digest"));
        WebElement weeklyRoundup = driver.findElement(By.id("weekly-roundup"));
        WebElement breakingNews = driver.findElement(By.id("breaking-news"));

        // When main is unchecked, all children should be disabled
        if (mainNewsletter.isSelected()) {
            mainNewsletter.click();
        }
        
        Assert.assertFalse(dailyDigest.isEnabled(), "Daily digest should be disabled when main is unchecked");
        Assert.assertFalse(weeklyRoundup.isEnabled(), "Weekly roundup should be disabled when main is unchecked");
        Assert.assertFalse(breakingNews.isEnabled(), "Breaking news should be disabled when main is unchecked");

        // When main is checked, children should be enabled
        mainNewsletter.click();
        
        Assert.assertTrue(dailyDigest.isEnabled(), "Daily digest should be enabled when main is checked");
        Assert.assertTrue(weeklyRoundup.isEnabled(), "Weekly roundup should be enabled when main is checked");
        Assert.assertTrue(breakingNews.isEnabled(), "Breaking news should be enabled when main is checked");
    }

    @Test
    public void mutuallyExclusiveOptionsTest() {
        // Email frequency: Daily OR Weekly (not both)
        WebElement dailyEmails = driver.findElement(By.id("frequency-daily"));
        WebElement weeklyEmails = driver.findElement(By.id("frequency-weekly"));

        // Select daily
        if (!dailyEmails.isSelected()) {
            dailyEmails.click();
        }
        Assert.assertTrue(dailyEmails.isSelected(), "Daily should be selected");
        Assert.assertFalse(weeklyEmails.isSelected(), "Weekly should be automatically deselected");

        // Select weekly
        weeklyEmails.click();
        Assert.assertFalse(dailyEmails.isSelected(), "Daily should be automatically deselected");
        Assert.assertTrue(weeklyEmails.isSelected(), "Weekly should be selected");
    }

    @Test
    public void conditionalCheckboxEnablementTest() {
        // Special offers checkbox only enabled if marketing emails accepted
        WebElement marketingEmails = driver.findElement(By.id("marketing-emails"));
        WebElement specialOffers = driver.findElement(By.id("special-offers"));
        WebElement partnerOffers = driver.findElement(By.id("partner-offers"));

        // Initially marketing unchecked
        if (marketingEmails.isSelected()) {
            marketingEmails.click();
        }

        // Verify dependent checkboxes are disabled
        Assert.assertFalse(specialOffers.isEnabled(), "Special offers should be disabled");
        Assert.assertFalse(partnerOffers.isEnabled(), "Partner offers should be disabled");

        // Enable marketing emails
        marketingEmails.click();

        // Verify dependent checkboxes are now enabled
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.elementToBeClickable(specialOffers));
        
        Assert.assertTrue(specialOffers.isEnabled(), "Special offers should be enabled");
        Assert.assertTrue(partnerOffers.isEnabled(), "Partner offers should be enabled");
    }

    @Test
    public void subscriptionLimitValidationTest() {
        // User can select maximum 3 topic categories
        String[] topics = {"technology", "sports", "entertainment", "politics", "health", "business"};
        int maxSelections = 3;
        int selectedCount = 0;

        // Try to select more than allowed
        for (String topic : topics) {
            WebElement topicCheckbox = driver.findElement(By.id("topic-" + topic));
            
            if (selectedCount < maxSelections) {
                if (!topicCheckbox.isSelected()) {
                    topicCheckbox.click();
                    selectedCount++;
                }
                Assert.assertTrue(topicCheckbox.isEnabled(), 
                    String.format("Topic %s should be enabled when under limit", topic));
            } else {
                // Verify remaining checkboxes are disabled
                Assert.assertFalse(topicCheckbox.isEnabled() && !topicCheckbox.isSelected(), 
                    String.format("Topic %s should be disabled when limit reached", topic));
            }
        }

        // Verify warning message appears
        WebElement warningMessage = driver.findElement(By.className("selection-limit-warning"));
        Assert.assertTrue(warningMessage.isDisplayed(), "Warning about selection limit should be displayed");
        Assert.assertTrue(warningMessage.getText().contains("maximum of 3 topics"), 
            "Warning should mention the limit");
    }

    @Test
    public void smartDefaultsBasedOnProfileTest() {
        // Set user profile
        selectUserProfile("developer");

        // Verify smart defaults are applied
        Assert.assertTrue(driver.findElement(By.id("topic-technology")).isSelected(), 
            "Technology should be pre-selected for developers");
        Assert.assertTrue(driver.findElement(By.id("topic-programming")).isSelected(), 
            "Programming should be pre-selected for developers");
        Assert.assertFalse(driver.findElement(By.id("topic-fashion")).isSelected(), 
            "Fashion should not be pre-selected for developers");

        // Change profile
        selectUserProfile("designer");

        // Verify defaults updated
        Assert.assertTrue(driver.findElement(By.id("topic-design")).isSelected(), 
            "Design should be pre-selected for designers");
        Assert.assertTrue(driver.findElement(By.id("topic-ux")).isSelected(), 
            "UX should be pre-selected for designers");
        Assert.assertFalse(driver.findElement(By.id("topic-backend")).isSelected(), 
            "Backend should not be pre-selected for designers");
    }

    @Test
    public void unsubscribeAllWithConfirmationTest() {
        // Select some subscriptions first
        List<WebElement> allCheckboxes = driver.findElements(By.cssSelector("input[type='checkbox'].subscription"));
        allCheckboxes.stream().limit(5).forEach(cb -> {
            if (!cb.isSelected()) cb.click();
        });

        // Click unsubscribe all
        WebElement unsubscribeAllBtn = driver.findElement(By.id("unsubscribe-all"));
        unsubscribeAllBtn.click();

        // Verify confirmation dialog
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement confirmDialog = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("confirm-dialog"))
        );

        Assert.assertTrue(confirmDialog.getText().contains("Are you sure"), 
            "Confirmation dialog should appear");

        // Confirm unsubscribe
        driver.findElement(By.id("confirm-unsubscribe")).click();

        // Verify all checkboxes are unchecked
        allCheckboxes = driver.findElements(By.cssSelector("input[type='checkbox'].subscription"));
        allCheckboxes.forEach(cb -> {
            Assert.assertFalse(cb.isSelected(), "All subscriptions should be unchecked");
        });

        // Verify success message
        WebElement successMsg = driver.findElement(By.className("unsubscribe-success"));
        Assert.assertTrue(successMsg.isDisplayed(), "Success message should be displayed");
    }

    private void selectUserProfile(String profileType) {
        WebElement profileDropdown = driver.findElement(By.id("user-profile"));
        profileDropdown.sendKeys(profileType);
        
        // Wait for profile to be applied
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.attributeContains(
            By.id("profile-indicator"), "data-profile", profileType
        ));
    }
}