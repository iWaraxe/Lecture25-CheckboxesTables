package com.coherentsolutions.java.webauto.section01.advanced;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
 * Advanced Example: Form Validation with Required Checkboxes
 * 
 * Real-world scenario: Complex forms with checkbox validation rules
 * Demonstrates form submission validation, error handling, and accessibility
 */
public class Ex08FormValidationWithCheckboxesTest {

    private WebDriver driver;
    private JavascriptExecutor js;
    private static final String URL = "https://the-internet.herokuapp.com/checkboxes";

    @BeforeMethod
    public void openBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void requiredCheckboxValidationTest() {
        // Fill form fields
        driver.findElement(By.id("username")).sendKeys("testuser");
        driver.findElement(By.id("email")).sendKeys("test@example.com");

        // Try to submit without accepting terms
        WebElement termsCheckbox = driver.findElement(By.id("accept-terms"));
        WebElement submitButton = driver.findElement(By.id("submit-form"));

        // Ensure terms not checked
        if (termsCheckbox.isSelected()) {
            termsCheckbox.click();
        }

        // Submit form
        submitButton.click();

        // Verify validation error appears
        WebElement errorMessage = driver.findElement(By.className("checkbox-error"));
        Assert.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        Assert.assertTrue(errorMessage.getText().contains("You must accept the terms"), 
            "Error should mention terms acceptance");

        // Verify form not submitted
        Assert.assertTrue(driver.getCurrentUrl().contains("form"), "Should remain on form page");

        // Accept terms and resubmit
        termsCheckbox.click();
        submitButton.click();

        // Verify successful submission
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("success"));
        Assert.assertTrue(driver.getCurrentUrl().contains("success"), "Form should be submitted successfully");
    }

    @Test
    public void atLeastOneCheckboxRequiredTest() {
        // Scenario: User must select at least one interest
        List<WebElement> interestCheckboxes = driver.findElements(By.className("interest-checkbox"));
        
        // Ensure all unchecked
        interestCheckboxes.forEach(cb -> {
            if (cb.isSelected()) cb.click();
        });

        // Try to submit
        WebElement submitButton = driver.findElement(By.id("submit-interests"));
        submitButton.click();

        // Verify validation error
        WebElement errorDiv = driver.findElement(By.id("interest-error"));
        Assert.assertTrue(errorDiv.isDisplayed(), "Error should be displayed");
        Assert.assertEquals(errorDiv.getAttribute("role"), "alert", "Error should have alert role for accessibility");

        // Select one interest
        interestCheckboxes.get(0).click();

        // Error should disappear immediately
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.invisibilityOf(errorDiv));

        // Submit should now work
        submitButton.click();
        wait.until(ExpectedConditions.urlContains("interests-saved"));
    }

    @Test
    public void customCheckboxValidationMessagesTest() {
        // Age verification with custom validation
        WebElement ageCheckbox = driver.findElement(By.id("age-verification"));
        WebElement submitButton = driver.findElement(By.id("submit-age-restricted"));

        // Submit without checking
        submitButton.click();

        // Verify custom validation message
        String validationMessage = (String) js.executeScript(
            "return arguments[0].validationMessage;", ageCheckbox
        );
        Assert.assertTrue(validationMessage.contains("You must be 18 or older"), 
            "Custom validation message should be set");

        // Verify checkbox has error styling
        String borderColor = ageCheckbox.getCssValue("border-color");
        Assert.assertTrue(borderColor.contains("rgb(255, 0, 0)") || borderColor.contains("red"), 
            "Checkbox should have red border on error");

        // Check the box
        ageCheckbox.click();

        // Verify error styling removed
        borderColor = ageCheckbox.getCssValue("border-color");
        Assert.assertFalse(borderColor.contains("rgb(255, 0, 0)") || borderColor.contains("red"), 
            "Red border should be removed after checking");
    }

    @Test
    public void progressiveDisclosureWithCheckboxesTest() {
        // Additional options appear based on checkbox selection
        WebElement advancedOptions = driver.findElement(By.id("show-advanced"));
        WebElement advancedPanel = driver.findElement(By.id("advanced-panel"));

        // Initially hidden
        Assert.assertFalse(advancedPanel.isDisplayed(), "Advanced panel should be hidden initially");

        // Check to show advanced options
        advancedOptions.click();

        // Verify panel appears with animation
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.visibilityOf(advancedPanel));

        // Verify additional checkboxes are now available
        List<WebElement> advancedCheckboxes = advancedPanel.findElements(By.className("advanced-option"));
        Assert.assertTrue(advancedCheckboxes.size() > 0, "Advanced options should be available");

        // Uncheck to hide
        advancedOptions.click();
        wait.until(ExpectedConditions.invisibilityOf(advancedPanel));
    }

    @Test
    public void accessibilityComplianceTest() {
        // Verify checkbox accessibility attributes
        WebElement checkbox = driver.findElement(By.id("accessible-checkbox"));
        WebElement label = driver.findElement(By.cssSelector("label[for='accessible-checkbox']"));

        // Verify ARIA attributes
        Assert.assertEquals(checkbox.getAttribute("role"), "checkbox", "Should have checkbox role");
        Assert.assertNotNull(checkbox.getAttribute("aria-label"), "Should have aria-label");
        Assert.assertEquals(checkbox.getAttribute("aria-required"), "true", "Should indicate required status");

        // Verify keyboard navigation
        checkbox.sendKeys(" "); // Space key to toggle
        Assert.assertTrue(checkbox.isSelected(), "Should be checkable via keyboard");

        // Verify focus indication
        js.executeScript("arguments[0].focus();", checkbox);
        String outline = checkbox.getCssValue("outline");
        Assert.assertFalse(outline.contains("none"), "Should have visible focus indicator");

        // Verify label association
        label.click();
        Assert.assertFalse(checkbox.isSelected(), "Clicking label should toggle checkbox");
    }

    @Test
    public void bulkOperationValidationTest() {
        // Scenario: Bulk delete with confirmation checkboxes
        List<WebElement> itemCheckboxes = driver.findElements(By.className("item-select"));
        WebElement selectAllCheckbox = driver.findElement(By.id("select-all"));
        WebElement deleteButton = driver.findElement(By.id("bulk-delete"));

        // Initially delete button should be disabled
        Assert.assertFalse(deleteButton.isEnabled(), "Delete button should be disabled with no selection");

        // Select some items
        itemCheckboxes.get(0).click();
        itemCheckboxes.get(1).click();

        // Delete button should now be enabled
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton));

        // Click delete
        deleteButton.click();

        // Verify confirmation appears with count
        WebElement confirmDialog = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("delete-confirm"))
        );
        Assert.assertTrue(confirmDialog.getText().contains("Delete 2 items?"), 
            "Confirmation should show item count");

        // Verify safety checkbox in confirmation
        WebElement confirmCheckbox = confirmDialog.findElement(By.id("confirm-delete-checkbox"));
        WebElement confirmButton = confirmDialog.findElement(By.id("confirm-delete-button"));

        // Confirm button should be disabled until checkbox checked
        Assert.assertFalse(confirmButton.isEnabled(), "Confirm button should require checkbox");

        // Check confirmation checkbox
        confirmCheckbox.click();
        Assert.assertTrue(confirmButton.isEnabled(), "Confirm button should be enabled after checking");
    }

    @Test
    public void dynamicValidationRulesTest() {
        // Validation rules change based on user type
        WebElement userTypeSelect = driver.findElement(By.id("user-type"));
        
        // Select "Business" user type
        userTypeSelect.sendKeys("business");

        // Business users must accept additional terms
        WebElement businessTerms = driver.findElement(By.id("business-terms"));
        WebElement dataProcessing = driver.findElement(By.id("data-processing"));
        
        Assert.assertTrue(businessTerms.getAttribute("required").equals("true"), 
            "Business terms should be required for business users");
        Assert.assertTrue(dataProcessing.getAttribute("required").equals("true"), 
            "Data processing should be required for business users");

        // Switch to "Personal" user type
        userTypeSelect.clear();
        userTypeSelect.sendKeys("personal");

        // Requirements should change
        Assert.assertNull(businessTerms.getAttribute("required"), 
            "Business terms should not be required for personal users");
        Assert.assertNull(dataProcessing.getAttribute("required"), 
            "Data processing should not be required for personal users");
    }
}