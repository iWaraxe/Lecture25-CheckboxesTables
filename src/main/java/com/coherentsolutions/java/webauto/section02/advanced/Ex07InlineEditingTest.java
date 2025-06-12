package com.coherentsolutions.java.webauto.section02.advanced;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * Advanced Example: Inline Table Editing
 * 
 * Real-world scenario: Editing table data directly without form navigation
 * Demonstrates click-to-edit, validation, keyboard navigation, and save mechanisms
 */
public class Ex07InlineEditingTest {

    private WebDriver driver;
    private Actions actions;
    private static final String URL = "https://the-internet.herokuapp.com/tables";

    @BeforeMethod
    public void openBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        actions = new Actions(driver);
        driver.get(URL);
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
    }

    @Test
    public void clickToEditCellTest() {
        // Find editable cell
        WebElement editableCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[@class='editable'][1]")
        );
        String originalValue = editableCell.getText();

        // Double-click to enable editing
        actions.doubleClick(editableCell).perform();

        // Wait for input field to appear
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebElement inputField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.xpath(".//input[@type='text']"))
        );

        // Clear and enter new value
        inputField.clear();
        inputField.sendKeys("Updated Value");
        inputField.sendKeys(Keys.ENTER);

        // Verify cell updated
        wait.until(ExpectedConditions.textToBePresentInElement(editableCell, "Updated Value"));
        Assert.assertEquals(editableCell.getText(), "Updated Value", "Cell value should be updated");

        // Verify save indicator
        WebElement saveIndicator = driver.findElement(By.className("save-status"));
        Assert.assertTrue(saveIndicator.getText().contains("Saved"), "Save status should show saved");
    }

    @Test
    public void editWithValidationTest() {
        // Find email cell
        WebElement emailCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[@data-type='email']")
        );

        // Enable editing
        actions.doubleClick(emailCell).perform();
        WebElement inputField = driver.findElement(By.xpath(".//input[@type='email']"));

        // Try invalid email
        inputField.clear();
        inputField.sendKeys("invalid-email");
        inputField.sendKeys(Keys.TAB);

        // Verify validation error
        WebElement errorTooltip = driver.findElement(By.className("validation-error"));
        Assert.assertTrue(errorTooltip.isDisplayed(), "Validation error should be shown");
        Assert.assertTrue(errorTooltip.getText().contains("valid email"), 
            "Error should mention email format");

        // Cell should retain original value
        String cellValue = emailCell.getText();
        Assert.assertNotEquals(cellValue, "invalid-email", 
            "Invalid value should not be saved");

        // Enter valid email
        actions.doubleClick(emailCell).perform();
        inputField = driver.findElement(By.xpath(".//input[@type='email']"));
        inputField.clear();
        inputField.sendKeys("valid@example.com");
        inputField.sendKeys(Keys.ENTER);

        // Verify successful update
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.textToBePresentInElement(emailCell, "valid@example.com"));
    }

    @Test
    public void dropdownEditingTest() {
        // Find status cell with dropdown
        WebElement statusCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[@data-type='select']")
        );
        String originalStatus = statusCell.getText();

        // Click to edit
        statusCell.click();

        // Wait for dropdown to appear
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebElement dropdown = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.xpath(".//select"))
        );

        // Change selection
        Select select = new Select(dropdown);
        select.selectByValue("completed");

        // Click outside to save
        driver.findElement(By.tagName("body")).click();

        // Verify update
        wait.until(ExpectedConditions.textToBePresentInElement(statusCell, "Completed"));
        Assert.assertNotEquals(statusCell.getText(), originalStatus, 
            "Status should be updated");
    }

    @Test
    public void keyboardNavigationEditingTest() {
        // Start editing first cell
        WebElement firstCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[@class='editable'][1]")
        );
        
        actions.doubleClick(firstCell).perform();
        WebElement inputField = driver.findElement(By.xpath(".//input"));
        
        // Edit and navigate with Tab
        inputField.clear();
        inputField.sendKeys("First Edit");
        inputField.sendKeys(Keys.TAB);

        // Should move to next editable cell
        WebElement activeElement = driver.switchTo().activeElement();
        Assert.assertEquals(activeElement.getTagName(), "input", 
            "Tab should move to next editable cell");

        // Edit current cell
        activeElement.clear();
        activeElement.sendKeys("Second Edit");
        activeElement.sendKeys(Keys.SHIFT, Keys.TAB);

        // Should move back to previous cell
        activeElement = driver.switchTo().activeElement();
        activeElement.sendKeys(Keys.ESCAPE);

        // Verify ESC cancels edit
        Assert.assertFalse(isElementPresent(By.xpath("//input")), 
            "ESC should cancel editing mode");
    }

    @Test
    public void bulkEditModeTest() {
        // Enable bulk edit mode
        WebElement bulkEditButton = driver.findElement(By.id("bulk-edit-mode"));
        bulkEditButton.click();

        // All editable cells should show as editable
        List<WebElement> editableCells = driver.findElements(By.className("editable"));
        editableCells.forEach(cell -> {
            Assert.assertTrue(cell.getAttribute("class").contains("edit-mode"), 
                "All editable cells should be in edit mode");
        });

        // Edit multiple cells
        WebElement firstNameCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[2]/input")
        );
        firstNameCell.clear();
        firstNameCell.sendKeys("Bulk");

        WebElement lastNameCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[1]/input")
        );
        lastNameCell.clear();
        lastNameCell.sendKeys("Edit");

        // Save all changes
        WebElement saveAllButton = driver.findElement(By.id("save-all-edits"));
        saveAllButton.click();

        // Verify all changes saved
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement successMessage = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("bulk-save-success"))
        );
        Assert.assertTrue(successMessage.getText().contains("2 cells updated"), 
            "Success message should show update count");
    }

    @Test
    public void editWithAutoSaveTest() throws InterruptedException {
        // Enable auto-save
        WebElement autoSaveToggle = driver.findElement(By.id("auto-save-toggle"));
        if (!autoSaveToggle.isSelected()) {
            autoSaveToggle.click();
        }

        // Edit cell
        WebElement editableCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[@class='editable'][1]")
        );
        actions.doubleClick(editableCell).perform();
        
        WebElement inputField = driver.findElement(By.xpath(".//input"));
        inputField.clear();
        inputField.sendKeys("Auto Save Test");

        // Wait for auto-save (typically after a delay)
        Thread.sleep(3000); // Auto-save delay

        // Verify saved without pressing Enter
        WebElement saveIndicator = driver.findElement(By.className("auto-save-indicator"));
        Assert.assertTrue(saveIndicator.getText().contains("Auto-saved"), 
            "Should show auto-save status");

        // Refresh page to verify persistence
        driver.navigate().refresh();
        
        editableCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[@class='editable'][1]")
        );
        Assert.assertEquals(editableCell.getText(), "Auto Save Test", 
            "Auto-saved value should persist");
    }

    @Test
    public void concurrentEditConflictTest() {
        // Simulate concurrent edit scenario
        WebElement editableCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[@class='editable'][1]")
        );
        
        // Start editing
        actions.doubleClick(editableCell).perform();
        WebElement inputField = driver.findElement(By.xpath(".//input"));
        inputField.clear();
        inputField.sendKeys("My Edit");

        // Simulate another user's edit (via JavaScript)
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "arguments[0].setAttribute('data-server-value', 'Other User Edit');",
            editableCell
        );

        // Try to save
        inputField.sendKeys(Keys.ENTER);

        // Should show conflict dialog
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebElement conflictDialog = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("edit-conflict-dialog"))
        );

        Assert.assertTrue(conflictDialog.getText().contains("conflict"), 
            "Conflict dialog should appear");
        
        // Choose to keep local changes
        WebElement keepLocalButton = conflictDialog.findElement(By.id("keep-local"));
        keepLocalButton.click();

        // Verify local changes kept
        wait.until(ExpectedConditions.textToBePresentInElement(editableCell, "My Edit"));
    }

    @Test
    public void editHistoryTrackingTest() {
        // Enable edit history
        WebElement historyToggle = driver.findElement(By.id("track-edit-history"));
        if (!historyToggle.isSelected()) {
            historyToggle.click();
        }

        // Make an edit
        WebElement editableCell = driver.findElement(
            By.xpath("//table[@id='table1']/tbody/tr[1]/td[@class='editable'][1]")
        );
        String originalValue = editableCell.getText();
        
        actions.doubleClick(editableCell).perform();
        WebElement inputField = driver.findElement(By.xpath(".//input"));
        inputField.clear();
        inputField.sendKeys("Tracked Edit");
        inputField.sendKeys(Keys.ENTER);

        // View edit history
        actions.contextClick(editableCell).perform();
        WebElement viewHistoryOption = driver.findElement(By.className("view-history"));
        viewHistoryOption.click();

        // Verify history dialog
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebElement historyDialog = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("edit-history-dialog"))
        );

        // Should show original and new value
        Assert.assertTrue(historyDialog.getText().contains(originalValue), 
            "History should show original value");
        Assert.assertTrue(historyDialog.getText().contains("Tracked Edit"), 
            "History should show new value");
        Assert.assertTrue(historyDialog.getText().contains("ago"), 
            "History should show timestamp");
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}