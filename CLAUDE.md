# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a comprehensive Selenium WebDriver training project (Lecture 25) focusing on checkbox and table automation. The project includes:
- Basic to advanced test examples
- Detailed markdown documentation explaining concepts and best practices
- Real-world use cases demonstrating practical applications

## Build and Development Commands

### Build the project
```bash
mvn clean compile
```

### Run all tests
```bash
mvn test
```

### Run specific test sections
```bash
# Checkbox tests
mvn test -Dtest="com.coherentsolutions.java.webauto.section01.*"

# Table tests
mvn test -Dtest="com.coherentsolutions.java.webauto.section02.*"

# Advanced tests only
mvn test -Dtest="com.coherentsolutions.java.webauto.section*/advanced/*"
```

### Run a specific test class
```bash
mvn test -Dtest=CheckboxTest
mvn test -Dtest=DataTableTest
mvn test -Dtest=Ex06CookieConsentTest
```

### Run a specific test method
```bash
mvn test -Dtest=CheckboxTest#checkboxDefaultConditionTest
```

### Clean build artifacts
```bash
mvn clean
```

## Code Architecture

### Project Structure
```
src/main/java/com/coherentsolutions/java/webauto/
├── section01/                    # Checkbox tests
│   ├── CheckboxTest.java        # Basic examples
│   ├── Ex01-Ex02...             # Basic exercises
│   └── advanced/                # Real-world scenarios
│       ├── Ex03-Ex05...         # Original advanced exercises
│       ├── Ex06CookieConsentTest.java     # Cookie consent management
│       ├── Ex07SubscriptionPreferencesTest.java  # Complex dependencies
│       └── Ex08FormValidationWithCheckboxesTest.java  # Form validation
├── section02/                    # Table tests
│   ├── DataTableTest.java       # Basic examples
│   ├── Ex01-Ex02...             # Sorting exercises
│   └── advanced/                # Complex table scenarios
│       ├── Ex03-Ex05...         # Original advanced exercises
│       ├── Ex06DataExportTest.java        # Export functionality
│       ├── Ex07InlineEditingTest.java     # Inline editing
│       └── Ex08PaginationInfiniteScrollTest.java  # Pagination handling
```

### Documentation Structure
- `Section01-Checkboxes.md`: Comprehensive guide for checkbox automation
- `Section02-Tables.md`: Comprehensive guide for table automation
- Both documents include:
  - Conceptual explanations
  - Code analysis with the "why" behind approaches
  - Real-world use cases
  - Common pitfalls and solutions
  - Best practices and testing strategies

### Key Architectural Patterns

1. **Test Organization**: Tests are organized in `src/main/java` (not `src/test/java`) as this is an educational project. Each section contains basic examples and an `/advanced/` subdirectory with more complex scenarios.

2. **Page Element Pattern**: Uses static `By` locators as constants for reusability:
   ```java
   private static final By CHECKBOX_1 = By.xpath(".//form[@id = 'checkboxes']/input[1]");
   ```

3. **TestNG Framework**: All tests use TestNG annotations (`@Test`, `@BeforeMethod`, `@AfterMethod`) with WebDriverManager for automatic driver management.

4. **Helper Methods**: Common operations are extracted into private helper methods (e.g., `selectCheckbox()`, `getColumnData()`).

5. **Progressive Complexity**: Examples progress from basic operations to advanced real-world scenarios

### Test URLs
- Basic Examples: `https://the-internet.herokuapp.com/checkboxes` and `/tables`
- Advanced scenarios simulate various real-world applications

### Common Test Patterns

1. **Setup/Teardown**: Every test class initializes ChromeDriver in `@BeforeMethod` and quits in `@AfterMethod`
2. **Defensive Programming**: Always check element state before acting (e.g., `if (!checkbox.isSelected())`)
3. **Explicit Waits**: Used for dynamic content and state changes
4. **Stream API**: Modern Java approach for data manipulation
5. **Validation Patterns**: Comprehensive assertions including state, visibility, and data integrity

### Teaching Philosophy

This project emphasizes understanding the "why" behind automation approaches:
- Each test demonstrates specific concepts and patterns
- Documentation explains when and why to use certain techniques
- Real-world scenarios show practical applications
- Common pitfalls are highlighted with solutions