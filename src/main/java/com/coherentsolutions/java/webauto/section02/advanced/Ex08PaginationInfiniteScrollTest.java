package com.coherentsolutions.java.webauto.section02.advanced;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Advanced Example: Pagination and Infinite Scroll Handling
 * 
 * Real-world scenario: Large datasets with pagination or infinite scrolling
 * Demonstrates navigation, data collection across pages, and performance optimization
 */
public class Ex08PaginationInfiniteScrollTest {

    private WebDriver driver;
    private JavascriptExecutor js;
    private static final String URL = "https://the-internet.herokuapp.com/tables";

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
    public void traditionalPaginationTest() {
        // Collect all data across pages
        List<String> allEmails = new ArrayList<>();
        int pageCount = 0;
        
        // Get total pages
        WebElement pageInfo = driver.findElement(By.className("pagination-info"));
        int totalPages = extractTotalPages(pageInfo.getText());

        while (pageCount < totalPages) {
            // Collect data from current page
            List<WebElement> emailCells = driver.findElements(
                By.xpath("//table[@id='paginated-table']/tbody/tr/td[3]")
            );
            
            emailCells.forEach(cell -> allEmails.add(cell.getText()));
            
            // Check if next button exists and is enabled
            WebElement nextButton = driver.findElement(By.className("pagination-next"));
            if (nextButton.isEnabled()) {
                nextButton.click();
                
                // Wait for page to load
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
                wait.until(ExpectedConditions.stalenessOf(emailCells.get(0)));
                
                pageCount++;
            } else {
                break;
            }
        }

        // Verify collected data
        Assert.assertTrue(allEmails.size() > 0, "Should collect emails from all pages");
        
        // Verify no duplicates
        Set<String> uniqueEmails = new HashSet<>(allEmails);
        Assert.assertEquals(uniqueEmails.size(), allEmails.size(), 
            "Should not have duplicate emails across pages");
    }

    @Test
    public void pageJumpNavigationTest() {
        // Direct page navigation
        WebElement pageInput = driver.findElement(By.id("page-number-input"));
        WebElement goButton = driver.findElement(By.id("go-to-page"));

        // Jump to specific page
        pageInput.clear();
        pageInput.sendKeys("5");
        goButton.click();

        // Verify navigation
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.className("current-page"), "Page 5"
        ));

        // Verify data changed
        WebElement firstRow = driver.findElement(
            By.xpath("//table[@id='paginated-table']/tbody/tr[1]")
        );
        Assert.assertTrue(firstRow.getAttribute("data-row-number").startsWith("4"), 
            "First row should be from page 5");
    }

    @Test
    public void dynamicPageSizeTest() {
        // Change page size
        Select pageSizeSelect = new Select(driver.findElement(By.id("page-size")));
        
        // Get initial row count
        int initialRows = driver.findElements(
            By.xpath("//table[@id='paginated-table']/tbody/tr")
        ).size();
        Assert.assertEquals(initialRows, 10, "Default should show 10 rows");

        // Change to 25 rows per page
        pageSizeSelect.selectByValue("25");

        // Wait for table to reload
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.numberOfElementsToBe(
            By.xpath("//table[@id='paginated-table']/tbody/tr"), 25
        ));

        // Verify page info updated
        WebElement pageInfo = driver.findElement(By.className("pagination-info"));
        Assert.assertTrue(pageInfo.getText().contains("1-25"), 
            "Page info should show 1-25 records");
    }

    @Test
    public void infiniteScrollTableTest() throws InterruptedException {
        // Navigate to infinite scroll table
        driver.get(URL + "/infinite-scroll");

        Set<String> loadedIds = new HashSet<>();
        int scrollCount = 0;
        int maxScrolls = 5;

        // Initial data
        List<WebElement> rows = driver.findElements(By.cssSelector(".infinite-table tbody tr"));
        rows.forEach(row -> loadedIds.add(row.getAttribute("data-id")));

        while (scrollCount < maxScrolls) {
            int previousCount = loadedIds.size();
            
            // Scroll to bottom of table
            WebElement table = driver.findElement(By.className("infinite-table"));
            js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight", table);

            // Wait for new data to load
            Thread.sleep(1000); // In real test, use proper wait
            
            // Collect new rows
            rows = driver.findElements(By.cssSelector(".infinite-table tbody tr"));
            rows.forEach(row -> loadedIds.add(row.getAttribute("data-id")));

            // Check if new data was loaded
            if (loadedIds.size() > previousCount) {
                System.out.println("Loaded " + (loadedIds.size() - previousCount) + " new rows");
            } else {
                // No more data
                break;
            }
            
            scrollCount++;
        }

        // Verify data loaded
        Assert.assertTrue(loadedIds.size() > 20, 
            "Should load additional rows through infinite scroll");
    }

    @Test
    public void virtualScrollingPerformanceTest() {
        // Navigate to virtual scroll table (renders only visible rows)
        driver.get(URL + "/virtual-scroll");

        // Get viewport info
        Long viewportHeight = (Long) js.executeScript(
            "return document.querySelector('.virtual-table').clientHeight"
        );
        Long rowHeight = (Long) js.executeScript(
            "return document.querySelector('.virtual-table tr').clientHeight"
        );
        
        int visibleRows = (int) (viewportHeight / rowHeight);

        // Verify only visible rows are rendered
        List<WebElement> renderedRows = driver.findElements(
            By.cssSelector(".virtual-table tbody tr")
        );
        Assert.assertTrue(renderedRows.size() <= visibleRows + 2, 
            "Virtual scroll should only render visible rows plus buffer");

        // Scroll to middle
        js.executeScript(
            "document.querySelector('.virtual-table').scrollTop = 5000"
        );

        // Wait for re-render
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//tr[@data-index='100']")
        ));

        // Verify different rows are now rendered
        WebElement middleRow = driver.findElement(By.xpath("//tr[@data-index='100']"));
        Assert.assertTrue(middleRow.isDisplayed(), 
            "Middle rows should be rendered after scroll");
    }

    @Test
    public void paginationWithFiltersTest() {
        // Apply filter
        WebElement filterInput = driver.findElement(By.id("table-filter"));
        filterInput.sendKeys("Engineering");

        // Wait for filter to apply
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.className("result-count"), "Showing"
        ));

        // Get filtered result count
        WebElement resultCount = driver.findElement(By.className("result-count"));
        int filteredTotal = extractNumber(resultCount.getText());

        // Verify pagination updated for filtered results
        WebElement pageInfo = driver.findElement(By.className("pagination-info"));
        Assert.assertTrue(pageInfo.getText().contains("of " + filteredTotal), 
            "Pagination should reflect filtered count");

        // Navigate through filtered pages
        List<String> filteredData = new ArrayList<>();
        
        do {
            // Collect filtered data
            List<WebElement> rows = driver.findElements(
                By.xpath("//table[@id='paginated-table']/tbody/tr")
            );
            
            rows.forEach(row -> {
                Assert.assertTrue(row.getText().contains("Engineering"), 
                    "All rows should match filter");
                filteredData.add(row.getAttribute("data-id"));
            });

            // Try next page
            WebElement nextButton = driver.findElement(By.className("pagination-next"));
            if (nextButton.isEnabled()) {
                nextButton.click();
                wait.until(ExpectedConditions.stalenessOf(rows.get(0)));
            } else {
                break;
            }
        } while (true);

        // Verify collected all filtered data
        Assert.assertEquals(filteredData.size(), filteredTotal, 
            "Should collect all filtered records across pages");
    }

    @Test
    public void loadMoreButtonTest() {
        // Alternative to pagination - Load More button
        driver.get(URL + "/load-more-table");

        List<String> allData = new ArrayList<>();
        WebElement loadMoreButton = driver.findElement(By.id("load-more"));

        // Load initial data
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        rows.forEach(row -> allData.add(row.getText()));

        // Keep clicking load more until disabled
        while (loadMoreButton.isEnabled()) {
            int previousCount = allData.size();
            loadMoreButton.click();

            // Wait for new rows
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("tbody tr"), previousCount
            ));

            // Collect new data
            rows = driver.findElements(By.cssSelector("tbody tr"));
            rows.stream()
                .skip(previousCount)
                .forEach(row -> allData.add(row.getText()));

            // Check if button still enabled
            loadMoreButton = driver.findElement(By.id("load-more"));
        }

        // Verify all data loaded
        WebElement totalRecords = driver.findElement(By.className("total-records"));
        int expectedTotal = extractNumber(totalRecords.getText());
        Assert.assertEquals(allData.size(), expectedTotal, 
            "Should load all available records");
    }

    @Test
    public void keyboardPaginationNavigationTest() {
        // Test keyboard shortcuts for pagination
        WebElement paginationContainer = driver.findElement(By.className("pagination"));
        
        // Focus pagination
        paginationContainer.click();

        // Press right arrow to go next
        //paginationContainer.sendKeys(Keys.ARROW_RIGHT);
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.className("current-page"), "Page 2"
        ));

        // Press left arrow to go back
        //paginationContainer.sendKeys(Keys.ARROW_LEFT);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
            By.className("current-page"), "Page 1"
        ));

        // Press End to go to last page
        //paginationContainer.sendKeys(Keys.END);
        WebElement lastPageIndicator = driver.findElement(By.className("last-page"));
        Assert.assertTrue(lastPageIndicator.getAttribute("class").contains("active"), 
            "Should navigate to last page");

        // Press Home to go to first page
        //paginationContainer.sendKeys(Keys.HOME);
        WebElement firstPageIndicator = driver.findElement(By.className("first-page"));
        Assert.assertTrue(firstPageIndicator.getAttribute("class").contains("active"), 
            "Should navigate to first page");
    }

    private int extractTotalPages(String pageInfo) {
        // Extract total pages from text like "Page 1 of 10"
        String[] parts = pageInfo.split(" of ");
        return Integer.parseInt(parts[1]);
    }

    private int extractNumber(String text) {
        // Extract number from text
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }
}