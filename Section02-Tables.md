# Section 02: Working with Tables in Selenium WebDriver

## Overview

HTML tables are one of the most complex elements to automate in web applications. They present structured data in rows and columns, often with dynamic content, sorting capabilities, pagination, and interactive elements. This section covers comprehensive strategies for table automation, from basic cell reading to complex data validation and manipulation scenarios that mirror real-world applications.

## Key Concepts

### Understanding HTML Table Structure

Tables consist of several key elements:
- **`<table>`**: The container element
- **`<thead>`**: Contains header rows
- **`<tbody>`**: Contains data rows
- **`<tr>`**: Table rows
- **`<th>`**: Header cells
- **`<td>`**: Data cells
- **`<tfoot>`**: Footer rows (less common)

### Why Table Automation is Challenging

1. **Dynamic Content**: Tables often load data asynchronously
2. **Complex Navigation**: Accessing specific cells requires understanding of row/column indices
3. **Sorting Verification**: Validating sort order requires data extraction and comparison
4. **Performance**: Large tables can have thousands of cells
5. **Nested Elements**: Cells may contain buttons, links, or other interactive elements
6. **Responsive Design**: Tables may restructure on different screen sizes

### Critical XPath Strategies for Tables

```java
// Row by index
"//table[@id='data']/tbody/tr[3]"

// Cell by row and column
"//table[@id='data']/tbody/tr[3]/td[2]"

// Cell by header text
"//table[@id='data']//tr[./th[text()='Email']]/following-sibling::tr/td[3]"

// All cells in a column
"//table[@id='data']/tbody/tr/td[position()=2]"
```

## Code Examples Analysis

### Example 1: Basic Table Navigation (`DataTableTest.java`)

```java
@Test
public void readTableDataTest() {
    WebElement table = driver.findElement(By.id("table1"));
    List<WebElement> rows = table.findElements(By.tagName("tr"));
    
    // Skip header row, process data rows
    for (int i = 1; i < rows.size(); i++) {
        List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));
        String lastName = cells.get(0).getText();
        String firstName = cells.get(1).getText();
        String email = cells.get(2).getText();
        
        System.out.println(String.format("Row %d: %s %s - %s", i, firstName, lastName, email));
    }
}
```

**What this demonstrates:**
- **Hierarchical Navigation**: Table → Rows → Cells
- **Index Management**: Skipping header row (index 0)
- **Data Extraction**: Getting text from each cell
- **Structured Access**: Using list indices for specific columns

**Why this approach:**
- Clear and readable for simple tables
- Direct mapping to visual table structure
- Easy to debug with row/column indices

### Example 2: Column Data Extraction (`DataTableTest.java`)

```java
private List<String> getColumnData(int columnIndex) {
    return driver.findElements(By.xpath(String.format("//table[@id='%s']/tbody/tr/td[%d]", 
            TABLE_ID, columnIndex)))
            .stream()
            .map(WebElement::getText)
            .collect(Collectors.toList());
}
```

**Advanced Pattern - Stream Processing:**
- **Single XPath**: Efficiently selects all cells in a column
- **Functional Approach**: Maps elements to text values
- **Immutable Result**: Returns new list without side effects

**Why streams over loops:**
```java
// Traditional approach - more verbose
List<String> columnData = new ArrayList<>();
List<WebElement> cells = driver.findElements(By.xpath("..."));
for (WebElement cell : cells) {
    columnData.add(cell.getText());
}

// Stream approach - concise and functional
List<String> columnData = driver.findElements(By.xpath("..."))
    .stream()
    .map(WebElement::getText)
    .collect(Collectors.toList());
```

### Example 3: Sorting Verification (`Ex01SortLastNameTest.java`)

```java
@Test
public void verifySortByLastNameTest() {
    // Click header to sort
    WebElement lastNameHeader = driver.findElement(By.xpath("//span[text()='Last Name']"));
    lastNameHeader.click();
    
    // Get column data
    List<String> actualOrder = getColumnData(1);
    
    // Create expected sorted list
    List<String> expectedOrder = new ArrayList<>(actualOrder);
    Collections.sort(expectedOrder);
    
    // Verify sorting
    Assert.assertEquals(actualOrder, expectedOrder, "Table should be sorted by last name");
}
```

**Sorting Verification Strategy:**
1. **Trigger Sort**: Click column header
2. **Extract Actual**: Get current column order
3. **Generate Expected**: Sort a copy of the data
4. **Compare**: Assert lists are equal

**Why copy before sorting:**
```java
// WRONG: Modifies original list
Collections.sort(actualOrder);
Assert.assertEquals(actualOrder, actualOrder); // Always passes!

// CORRECT: Compare against sorted copy
List<String> expectedOrder = new ArrayList<>(actualOrder);
Collections.sort(expectedOrder);
Assert.assertEquals(actualOrder, expectedOrder);
```

### Example 4: Row Count Verification (`Ex03VerifyTableRowCountTest.java`)

```java
@Test
public void verifyDynamicRowCountTest() {
    // Initial count
    int initialRows = driver.findElements(By.xpath("//table[@id='data']/tbody/tr")).size();
    
    // Add new record
    driver.findElement(By.id("add-record")).click();
    fillForm();
    driver.findElement(By.id("save")).click();
    
    // Wait for table update
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
        By.xpath("//table[@id='data']/tbody/tr"), initialRows
    ));
    
    // Verify count increased
    int newRows = driver.findElements(By.xpath("//table[@id='data']/tbody/tr")).size();
    Assert.assertEquals(newRows, initialRows + 1, "Table should have one more row");
}
```

**Dynamic Content Handling:**
- **Baseline Measurement**: Capture initial state
- **Action Trigger**: Perform operation that changes table
- **Explicit Wait**: Wait for specific condition (row count change)
- **Relative Assertion**: Compare against baseline, not fixed values

### Example 5: Cell Data Verification (`Ex04VerifyTableCellDataTest.java`)

```java
@Test
public void verifyCellDataAfterEditTest() {
    String targetEmail = "john.doe@example.com";
    String newPhone = "555-1234";
    
    // Find row by email
    WebElement row = driver.findElement(
        By.xpath(String.format("//tr[td[text()='%s']]", targetEmail))
    );
    
    // Click edit button in that row
    row.findElement(By.className("edit-btn")).click();
    
    // Update phone number
    WebElement phoneInput = driver.findElement(By.id("phone"));
    phoneInput.clear();
    phoneInput.sendKeys(newPhone);
    driver.findElement(By.id("save")).click();
    
    // Verify cell updated
    WebElement phoneCell = row.findElement(By.xpath("./td[4]"));
    Assert.assertEquals(phoneCell.getText(), newPhone, "Phone number should be updated");
}
```

**Row-Centric Operations:**
- **Find Row by Content**: Use unique identifier (email) to locate row
- **Scoped Actions**: Find elements within row context
- **Maintain Reference**: Keep row reference for verification

### Example 6: Extract Full Table Data (`Ex05ExtractTableDataTest.java`)

```java
@Test
public void extractCompleteTableDataTest() {
    List<Map<String, String>> tableData = new ArrayList<>();
    
    // Get headers
    List<String> headers = driver.findElements(By.xpath("//table[@id='data']/thead/tr/th"))
        .stream()
        .map(WebElement::getText)
        .collect(Collectors.toList());
    
    // Get all rows
    List<WebElement> rows = driver.findElements(By.xpath("//table[@id='data']/tbody/tr"));
    
    // Convert each row to map
    for (WebElement row : rows) {
        List<WebElement> cells = row.findElements(By.tagName("td"));
        Map<String, String> rowData = new HashMap<>();
        
        for (int i = 0; i < headers.size() && i < cells.size(); i++) {
            rowData.put(headers.get(i), cells.get(i).getText());
        }
        
        tableData.add(rowData);
    }
    
    // Now we can access data by column name
    tableData.forEach(row -> {
        System.out.println(row.get("First Name") + " " + row.get("Last Name"));
    });
}
```

**Data Structure Strategy:**
- **Header Mapping**: Use headers as keys for better readability
- **List of Maps**: Each row is a map of column name to value
- **Flexible Access**: Access data by column name instead of index

## Real-World Use Cases

### 1. E-Commerce Order Management
```java
public class OrderManagementTest {
    @Test
    public void filterAndProcessPendingOrdersTest() {
        // Apply filter for pending orders
        Select statusFilter = new Select(driver.findElement(By.id("status-filter")));
        statusFilter.selectByValue("pending");
        
        // Wait for table refresh
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading")));
        
        // Process each pending order
        List<WebElement> rows = driver.findElements(By.xpath("//table[@id='orders']/tbody/tr"));
        
        for (WebElement row : rows) {
            String orderId = row.findElement(By.className("order-id")).getText();
            String amount = row.findElement(By.className("amount")).getText();
            
            // Verify all displayed orders are pending
            String status = row.findElement(By.className("status")).getText();
            Assert.assertEquals(status, "Pending", "Only pending orders should be displayed");
            
            // Process high-value orders
            double orderAmount = Double.parseDouble(amount.replace("$", ""));
            if (orderAmount > 1000) {
                row.findElement(By.className("expedite-btn")).click();
                
                // Wait for status update
                wait.until(ExpectedConditions.textToBePresentInElement(
                    row.findElement(By.className("status")), "Expedited"
                ));
            }
        }
    }
}
```

### 2. Financial Report Validation
```java
public class FinancialReportTest {
    @Test
    public void validateQuarterlyReportTotalsTest() {
        WebElement reportTable = driver.findElement(By.id("quarterly-report"));
        
        // Extract all revenue values
        List<Double> revenues = driver.findElements(
            By.xpath("//table[@id='quarterly-report']/tbody/tr/td[@class='revenue']")
        ).stream()
            .map(cell -> Double.parseDouble(cell.getText().replace("$", "").replace(",", "")))
            .collect(Collectors.toList());
        
        // Calculate sum
        double calculatedTotal = revenues.stream().mapToDouble(Double::doubleValue).sum();
        
        // Get displayed total
        String displayedTotalText = driver.findElement(
            By.xpath("//table[@id='quarterly-report']/tfoot/tr/td[@class='total-revenue']")
        ).getText();
        double displayedTotal = Double.parseDouble(displayedTotalText.replace("$", "").replace(",", ""));
        
        // Verify within acceptable margin (floating point precision)
        Assert.assertEquals(calculatedTotal, displayedTotal, 0.01, 
            "Calculated total should match displayed total");
    }
}
```

### 3. Search Results Ranking Verification
```java
public class SearchResultsTest {
    @Test
    public void verifySearchResultsRelevanceTest() {
        // Perform search
        WebElement searchBox = driver.findElement(By.id("search"));
        searchBox.sendKeys("selenium automation");
        searchBox.submit();
        
        // Wait for results
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("results-table")));
        
        // Get relevance scores
        List<Double> relevanceScores = driver.findElements(
            By.xpath("//table[@id='results-table']/tbody/tr/td[@class='relevance-score']")
        ).stream()
            .map(cell -> Double.parseDouble(cell.getAttribute("data-score")))
            .collect(Collectors.toList());
        
        // Verify descending order
        for (int i = 1; i < relevanceScores.size(); i++) {
            Assert.assertTrue(
                relevanceScores.get(i-1) >= relevanceScores.get(i),
                "Results should be ordered by relevance score descending"
            );
        }
        
        // Verify all results contain search term
        List<WebElement> titles = driver.findElements(
            By.xpath("//table[@id='results-table']/tbody/tr/td[@class='title']")
        );
        
        titles.forEach(title -> {
            String titleText = title.getText().toLowerCase();
            Assert.assertTrue(
                titleText.contains("selenium") || titleText.contains("automation"),
                "Result should contain search terms"
            );
        });
    }
}
```

### 4. User Management with Inline Editing
```java
public class UserManagementTest {
    @Test
    public void inlineEditUserRolesTest() {
        // Find user by email
        String userEmail = "admin@example.com";
        WebElement userRow = driver.findElement(
            By.xpath(String.format("//tr[td[text()='%s']]", userEmail))
        );
        
        // Click role cell to enable editing
        WebElement roleCell = userRow.findElement(By.className("role-cell"));
        roleCell.click();
        
        // Wait for dropdown to appear
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        WebElement roleDropdown = wait.until(
            ExpectedConditions.elementToBeClickable(roleCell.findElement(By.tagName("select")))
        );
        
        // Change role
        Select roleSelect = new Select(roleDropdown);
        roleSelect.selectByValue("super-admin");
        
        // Save changes (blur event)
        roleDropdown.sendKeys(Keys.TAB);
        
        // Verify change persisted
        wait.until(ExpectedConditions.textToBePresentInElement(roleCell, "Super Admin"));
        
        // Refresh page and verify persistence
        driver.navigate().refresh();
        userRow = driver.findElement(
            By.xpath(String.format("//tr[td[text()='%s']]", userEmail))
        );
        Assert.assertEquals(
            userRow.findElement(By.className("role-cell")).getText(),
            "Super Admin",
            "Role change should persist after refresh"
        );
    }
}
```

## Advanced Patterns and Techniques

### 1. Table Page Object Pattern
```java
public class DataTable {
    private final WebDriver driver;
    private final By tableLocator;
    
    public DataTable(WebDriver driver, By tableLocator) {
        this.driver = driver;
        this.tableLocator = tableLocator;
    }
    
    public List<String> getHeaders() {
        return driver.findElements(By.xpath(tableLocator + "/thead/tr/th"))
            .stream()
            .map(WebElement::getText)
            .collect(Collectors.toList());
    }
    
    public List<Map<String, String>> getAllData() {
        List<String> headers = getHeaders();
        List<Map<String, String>> data = new ArrayList<>();
        
        List<WebElement> rows = driver.findElements(By.xpath(tableLocator + "/tbody/tr"));
        for (WebElement row : rows) {
            data.add(getRowData(row, headers));
        }
        
        return data;
    }
    
    public Map<String, String> getRowData(WebElement row, List<String> headers) {
        List<WebElement> cells = row.findElements(By.tagName("td"));
        Map<String, String> rowData = new HashMap<>();
        
        for (int i = 0; i < headers.size() && i < cells.size(); i++) {
            rowData.put(headers.get(i), cells.get(i).getText());
        }
        
        return rowData;
    }
    
    public void sortBy(String columnName) {
        driver.findElement(By.xpath(String.format(
            tableLocator + "/thead/tr/th[text()='%s']", columnName
        ))).click();
    }
    
    public WebElement findRowByColumnValue(String columnName, String value) {
        int columnIndex = getHeaders().indexOf(columnName) + 1;
        return driver.findElement(By.xpath(String.format(
            tableLocator + "/tbody/tr[td[position()=%d and text()='%s']]",
            columnIndex, value
        )));
    }
}
```

### 2. Pagination Handling
```java
public class PaginatedTableTest {
    @Test
    public void processAllPagesTest() {
        List<String> allEmails = new ArrayList<>();
        
        do {
            // Extract data from current page
            List<String> pageEmails = driver.findElements(
                By.xpath("//table[@id='users']/tbody/tr/td[3]")
            ).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
            
            allEmails.addAll(pageEmails);
            
            // Check for next page
            List<WebElement> nextButton = driver.findElements(By.className("next-page"));
            if (!nextButton.isEmpty() && nextButton.get(0).isEnabled()) {
                nextButton.get(0).click();
                
                // Wait for table to refresh
                new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.stalenessOf(
                        driver.findElement(By.xpath("//table[@id='users']/tbody/tr[1]"))
                    ));
            } else {
                break;
            }
        } while (true);
        
        // Verify no duplicates across pages
        Set<String> uniqueEmails = new HashSet<>(allEmails);
        Assert.assertEquals(uniqueEmails.size(), allEmails.size(), 
            "No duplicate emails should exist across pages");
    }
}
```

### 3. Complex Filtering and Verification
```java
public class AdvancedFilterTest {
    @Test
    public void multipleFilterCriteriaTest() {
        // Apply multiple filters
        applyFilter("department", "Engineering");
        applyFilter("location", "San Francisco");
        applyFilter("experience", ">5");
        
        // Wait for filtering to complete
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("filtering")));
        
        // Verify all rows match criteria
        List<WebElement> rows = driver.findElements(By.xpath("//table[@id='employees']/tbody/tr"));
        
        rows.forEach(row -> {
            Map<String, String> rowData = extractRowData(row);
            
            Assert.assertEquals(rowData.get("Department"), "Engineering");
            Assert.assertEquals(rowData.get("Location"), "San Francisco");
            Assert.assertTrue(
                Integer.parseInt(rowData.get("Experience")) > 5,
                "Experience should be greater than 5 years"
            );
        });
        
        // Verify result count
        String resultText = driver.findElement(By.className("result-count")).getText();
        Assert.assertEquals(rows.size(), extractNumber(resultText),
            "Displayed count should match actual row count");
    }
    
    private void applyFilter(String filterName, String filterValue) {
        WebElement filterElement = driver.findElement(By.id("filter-" + filterName));
        if (filterElement.getTagName().equals("select")) {
            new Select(filterElement).selectByVisibleText(filterValue);
        } else {
            filterElement.clear();
            filterElement.sendKeys(filterValue);
        }
    }
}
```

### 4. Performance Optimization for Large Tables
```java
public class LargeTableOptimizationTest {
    @Test
    public void efficientLargeTableProcessingTest() {
        // Use JavaScript for better performance on large tables
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Get row count without loading all elements
        Long rowCount = (Long) js.executeScript(
            "return document.querySelectorAll('#large-table tbody tr').length"
        );
        
        System.out.println("Table has " + rowCount + " rows");
        
        // Process in batches to avoid memory issues
        int batchSize = 100;
        for (int i = 0; i < rowCount; i += batchSize) {
            List<Map<String, Object>> batchData = (List<Map<String, Object>>) js.executeScript(
                "var rows = document.querySelectorAll('#large-table tbody tr');" +
                "var data = [];" +
                "for (var i = arguments[0]; i < Math.min(arguments[1], rows.length); i++) {" +
                "  var cells = rows[i].querySelectorAll('td');" +
                "  data.push({" +
                "    id: cells[0].textContent," +
                "    name: cells[1].textContent," +
                "    value: parseFloat(cells[2].textContent)" +
                "  });" +
                "}" +
                "return data;",
                i, i + batchSize
            );
            
            // Process batch
            processBatch(batchData);
        }
    }
}
```

## Common Pitfalls and Solutions

### 1. StaleElementReferenceException in Dynamic Tables
```java
// PROBLEMATIC: Table updates after action
List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));
rows.get(0).findElement(By.className("delete")).click();
rows.get(1).getText(); // StaleElementReferenceException!

// SOLUTION: Re-find elements after table updates
driver.findElements(By.xpath("//tbody/tr")).get(0)
    .findElement(By.className("delete")).click();
// Re-find for next operation
driver.findElements(By.xpath("//tbody/tr")).get(0).getText();
```

### 2. Incorrect XPath for Nested Tables
```java
// PROBLEMATIC: Selects cells from all tables
List<WebElement> cells = driver.findElements(By.xpath("//td"));

// SOLUTION: Be specific about table context
List<WebElement> cells = driver.findElements(
    By.xpath("//table[@id='specific-table']/tbody/tr/td")
);
```

### 3. Performance Issues with Large Tables
```java
// PROBLEMATIC: Loading thousands of elements
List<WebElement> allCells = driver.findElements(By.tagName("td"));

// SOLUTION: Use targeted queries or JavaScript
String cellValue = (String) ((JavascriptExecutor) driver).executeScript(
    "return document.querySelector('#table1 tbody tr:nth-child(50) td:nth-child(3)').textContent"
);
```

## Best Practices

### ✅ DO's

1. **Use Specific Locators**: Always scope XPath/CSS to specific table
2. **Handle Dynamic Content**: Use explicit waits for table updates
3. **Optimize for Performance**: Use JavaScript for large tables
4. **Verify Data Integrity**: Check calculations, totals, and sorting
5. **Test Edge Cases**: Empty tables, single row, pagination boundaries
6. **Create Reusable Methods**: Build table utility classes

### ❌ DON'Ts

1. **Don't Use Absolute XPath**: Brittle and breaks with structure changes
2. **Don't Ignore Loading States**: Wait for spinners/overlays to disappear
3. **Don't Load Everything**: Paginate or filter when possible
4. **Don't Hardcode Indices**: Use data attributes or content-based location
5. **Don't Assume Order**: Tables might be pre-sorted differently

## Testing Strategies

### 1. Data Integrity Testing
```java
@Test
public void verifyTableDataIntegrityTest() {
    DataTable table = new DataTable(driver, By.id("financial-data"));
    List<Map<String, String>> data = table.getAllData();
    
    // Verify calculations
    data.forEach(row -> {
        double price = Double.parseDouble(row.get("Price"));
        int quantity = Integer.parseInt(row.get("Quantity"));
        double total = Double.parseDouble(row.get("Total"));
        
        Assert.assertEquals(price * quantity, total, 0.01,
            "Total should equal price * quantity");
    });
}
```

### 2. Sorting Algorithm Testing
```java
@Test
public void testMultiColumnSortingTest() {
    // Sort by department, then by name
    driver.findElement(By.xpath("//th[text()='Department']")).click();
    driver.findElement(By.xpath("//th[text()='Name']")).click();
    
    List<Map<String, String>> tableData = extractTableData();
    
    // Verify sorting
    for (int i = 1; i < tableData.size(); i++) {
        String prevDept = tableData.get(i-1).get("Department");
        String currDept = tableData.get(i).get("Department");
        
        int deptComparison = prevDept.compareTo(currDept);
        Assert.assertTrue(deptComparison <= 0, "Departments should be sorted");
        
        if (deptComparison == 0) {
            String prevName = tableData.get(i-1).get("Name");
            String currName = tableData.get(i).get("Name");
            Assert.assertTrue(prevName.compareTo(currName) <= 0,
                "Names should be sorted within same department");
        }
    }
}
```

### 3. Filter Combination Testing
```java
@Test
public void testFilterCombinationsTest() {
    String[] statuses = {"Active", "Inactive", "Pending"};
    String[] departments = {"Sales", "Engineering", "HR"};
    
    for (String status : statuses) {
        for (String dept : departments) {
            applyFilters(Map.of("status", status, "department", dept));
            
            List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));
            if (rows.isEmpty()) {
                System.out.println("No data for: " + status + " - " + dept);
                continue;
            }
            
            // Verify all rows match filters
            rows.forEach(row -> {
                Assert.assertTrue(row.getText().contains(status));
                Assert.assertTrue(row.getText().contains(dept));
            });
        }
    }
}
```

## Teaching Summary

Table automation represents one of the most complex challenges in web testing, requiring a combination of element location strategies, data structure knowledge, and performance optimization techniques. Mastering table automation prepares testers for handling any structured data presentation in web applications.

**Key Learning Outcomes:**

1. **Structure Understanding**: Know the anatomy of HTML tables and how to navigate them
2. **XPath Mastery**: Use powerful XPath expressions for precise element location
3. **Data Extraction**: Convert table data into usable structures (Lists, Maps)
4. **Verification Strategies**: Validate sorting, filtering, calculations, and data integrity
5. **Performance Awareness**: Optimize for large datasets using targeted queries and JavaScript
6. **Dynamic Content Handling**: Manage updates, pagination, and asynchronous loading

**Progressive Skill Development:**
1. **Basic**: Read cell values, count rows, simple navigation
2. **Intermediate**: Extract columns, verify sorting, handle pagination
3. **Advanced**: Complex filtering, inline editing, performance optimization
4. **Expert**: Custom table frameworks, JavaScript integration, large dataset handling

**Critical Thinking Points:**
- Why use XPath position() vs array index?
- When should we use JavaScript vs Selenium methods?
- How do we balance performance vs readability?
- What's the best data structure for our use case?

Remember: Tables are microcosms of application complexity. The patterns learned here - structured navigation, data extraction, dynamic content handling, and performance optimization - apply throughout test automation. Master tables, and you'll have the tools to handle any complex UI element.