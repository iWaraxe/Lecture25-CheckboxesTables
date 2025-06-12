package com.coherentsolutions.java.webauto.section02.advanced;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Advanced Example: Table Data Export Functionality
 * 
 * Real-world scenario: Exporting table data to various formats (CSV, Excel, PDF)
 * Demonstrates download handling, file verification, and data integrity checks
 */
public class Ex06DataExportTest {

    private WebDriver driver;
    private static final String URL = "https://the-internet.herokuapp.com/tables";
    private static final String DOWNLOAD_DIR = System.getProperty("user.dir") + "/downloads";

    @BeforeMethod
    public void openBrowser() {
        // Configure Chrome to download files to specific directory
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", DOWNLOAD_DIR);
        prefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", prefs);

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.get(URL);

        // Create download directory if not exists
        new File(DOWNLOAD_DIR).mkdirs();
    }

    @AfterMethod
    public void closeBrowser() {
        driver.quit();
        // Clean up downloads
        cleanDownloadDirectory();
    }

    @Test
    public void exportTableToCSVTest() throws Exception {
        // Get table data before export
        List<Map<String, String>> tableData = extractTableData();

        // Click export to CSV button
        WebElement exportButton = driver.findElement(By.id("export-csv"));
        exportButton.click();

        // Wait for download to complete
        File downloadedFile = waitForDownload("table-export.csv", 10);
        Assert.assertNotNull(downloadedFile, "CSV file should be downloaded");

        // Verify CSV content
        List<String> csvLines = Files.readAllLines(downloadedFile.toPath());
        
        // Verify header
        String[] headers = csvLines.get(0).split(",");
        Assert.assertEquals(headers[0], "Last Name", "First column should be Last Name");
        Assert.assertEquals(headers[1], "First Name", "Second column should be First Name");

        // Verify data rows
        Assert.assertEquals(csvLines.size() - 1, tableData.size(), 
            "CSV should have same number of data rows as table");

        // Verify specific data integrity
        for (int i = 0; i < tableData.size(); i++) {
            Map<String, String> expectedRow = tableData.get(i);
            String[] csvRow = csvLines.get(i + 1).split(",");
            
            Assert.assertEquals(csvRow[0], expectedRow.get("Last Name"));
            Assert.assertEquals(csvRow[1], expectedRow.get("First Name"));
            Assert.assertEquals(csvRow[2], expectedRow.get("Email"));
        }
    }

    @Test
    public void exportFilteredDataTest() throws Exception {
        // Apply filter first
        WebElement filterInput = driver.findElement(By.id("table-filter"));
        filterInput.sendKeys("Smith");
        
        // Wait for filter to apply
        Thread.sleep(1000); // In real test, use proper wait

        // Get filtered data
        List<Map<String, String>> filteredData = extractTableData();
        Assert.assertTrue(filteredData.size() < 4, "Table should be filtered");

        // Export filtered data
        WebElement exportButton = driver.findElement(By.id("export-filtered"));
        exportButton.click();

        // Verify exported file contains only filtered data
        File downloadedFile = waitForDownload("filtered-export.csv", 10);
        List<String> csvLines = Files.readAllLines(downloadedFile.toPath());
        
        Assert.assertEquals(csvLines.size() - 1, filteredData.size(), 
            "Exported file should contain only filtered rows");
        
        // Verify all exported rows contain filter term
        for (int i = 1; i < csvLines.size(); i++) {
            Assert.assertTrue(csvLines.get(i).contains("Smith"), 
                "Exported row should contain filter term");
        }
    }

    @Test
    public void exportWithCustomColumnsTest() {
        // Select columns to export
        WebElement columnSelector = driver.findElement(By.id("export-columns"));
        columnSelector.click();

        // Deselect some columns
        WebElement emailCheckbox = driver.findElement(By.id("export-col-email"));
        WebElement websiteCheckbox = driver.findElement(By.id("export-col-website"));
        
        if (emailCheckbox.isSelected()) emailCheckbox.click();
        if (websiteCheckbox.isSelected()) websiteCheckbox.click();

        // Export with selected columns only
        WebElement exportButton = driver.findElement(By.id("export-custom"));
        exportButton.click();

        // Wait for export options to be applied
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement successMessage = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("export-success"))
        );

        Assert.assertTrue(successMessage.getText().contains("Exported 2 columns"), 
            "Success message should confirm column count");
    }

    @Test
    public void batchExportMultipleFormatsTest() throws Exception {
        // Export to multiple formats
        String[] formats = {"csv", "xlsx", "pdf"};
        
        for (String format : formats) {
            WebElement formatButton = driver.findElement(By.id("export-" + format));
            formatButton.click();

            // Wait for each download
            File downloadedFile = waitForDownload("table-export." + format, 10);
            Assert.assertNotNull(downloadedFile, 
                String.format("%s file should be downloaded", format.toUpperCase()));

            // Verify file size (should have content)
            Assert.assertTrue(downloadedFile.length() > 0, 
                String.format("%s file should not be empty", format.toUpperCase()));

            // Format-specific validation
            switch (format) {
                case "csv":
                    Assert.assertTrue(Files.readString(downloadedFile.toPath()).contains(","), 
                        "CSV should contain comma separators");
                    break;
                case "xlsx":
                    Assert.assertTrue(downloadedFile.getName().endsWith(".xlsx"), 
                        "Excel file should have correct extension");
                    break;
                case "pdf":
                    // PDF validation would require PDF library
                    Assert.assertTrue(downloadedFile.length() > 1000, 
                        "PDF should have reasonable size");
                    break;
            }
        }
    }

    @Test
    public void exportLargeDatasetWithProgressTest() {
        // Trigger large dataset export
        WebElement largeExportButton = driver.findElement(By.id("export-large-dataset"));
        largeExportButton.click();

        // Verify progress indicator appears
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebElement progressBar = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("export-progress"))
        );

        // Verify progress updates
        String initialProgress = progressBar.getAttribute("aria-valuenow");
        
        // Wait a bit for progress
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        
        String updatedProgress = progressBar.getAttribute("aria-valuenow");
        Assert.assertNotEquals(initialProgress, updatedProgress, 
            "Progress should update during export");

        // Wait for completion
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.attributeToBe(progressBar, "aria-valuenow", "100"));

        // Verify completion message
        WebElement completionMessage = driver.findElement(By.className("export-complete"));
        Assert.assertTrue(completionMessage.isDisplayed(), 
            "Completion message should be displayed");
    }

    @Test
    public void scheduleRecurringExportTest() {
        // Open export scheduler
        WebElement scheduleButton = driver.findElement(By.id("schedule-export"));
        scheduleButton.click();

        // Configure schedule
        WebElement frequencySelect = driver.findElement(By.id("export-frequency"));
        frequencySelect.sendKeys("weekly");

        WebElement daySelect = driver.findElement(By.id("export-day"));
        daySelect.sendKeys("Monday");

        WebElement timeInput = driver.findElement(By.id("export-time"));
        timeInput.sendKeys("09:00");

        WebElement emailInput = driver.findElement(By.id("export-email"));
        emailInput.sendKeys("reports@example.com");

        // Save schedule
        WebElement saveButton = driver.findElement(By.id("save-schedule"));
        saveButton.click();

        // Verify schedule saved
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement confirmation = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("schedule-saved"))
        );

        Assert.assertTrue(confirmation.getText().contains("Weekly export scheduled"), 
            "Confirmation should mention frequency");
        Assert.assertTrue(confirmation.getText().contains("Monday at 09:00"), 
            "Confirmation should mention schedule details");
    }

    private List<Map<String, String>> extractTableData() {
        // Extract current table data for comparison
        return driver.findElements(By.xpath("//table[@id='table1']/tbody/tr"))
            .stream()
            .map(row -> {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                Map<String, String> rowData = new HashMap<>();
                rowData.put("Last Name", cells.get(0).getText());
                rowData.put("First Name", cells.get(1).getText());
                rowData.put("Email", cells.get(2).getText());
                rowData.put("Due", cells.get(3).getText());
                rowData.put("Web Site", cells.get(4).getText());
                return rowData;
            })
            .collect(Collectors.toList());
    }

    private File waitForDownload(String fileName, int timeoutSeconds) {
        File downloadedFile = new File(DOWNLOAD_DIR, fileName);
        int waited = 0;
        
        while (!downloadedFile.exists() && waited < timeoutSeconds) {
            try {
                Thread.sleep(1000);
                waited++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return downloadedFile.exists() ? downloadedFile : null;
    }

    private void cleanDownloadDirectory() {
        File downloadDir = new File(DOWNLOAD_DIR);
        if (downloadDir.exists()) {
            for (File file : downloadDir.listFiles()) {
                file.delete();
            }
        }
    }
}