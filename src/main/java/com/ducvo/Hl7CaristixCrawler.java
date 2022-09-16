package com.ducvo;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class Hl7CaristixCrawler {
  private final static FileService fileService = new FileService("/Users/ducvo/Desktop/hl7");
  private static final Pattern PADDING_PATTERN =
      Pattern.compile(".*?padding-left: (?<indent>\\d+)px;.*");

  public static void main(String[] args) throws IOException {
    WebDriverManager.chromedriver().setup();

    var driver = new ChromeDriver();
    var wait = new WebDriverWait(driver, Duration.ofMinutes(5));

    driver.get("https://hl7-definition.caristix.com/v2/HL7v2.8/TriggerEvents/OMG_O19");
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mat-select-value")));

//    var versionSelection = driver.findElement(By.className("mat-select-value"));
//    versionSelection.click();
//
//    var versions = driver.findElements(By.className("mat-option"));
//
//    var targetVersionString = "2.8";
//    var targetVersion =
//        versions.stream()
//            .filter(
//                i ->
//                    i.findElement(By.className("mat-option-text"))
//                        .getText()
//                        .contains(targetVersionString))
//            .findFirst();
//
//    if (targetVersion.isPresent()) {
//      targetVersion.get().click();
//    } else {
//      log.error("Version Not Found in Caristix");
//    }
//
//    wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mat-select-value")));
//    driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.END);

    var expandButtons = getCollapseButton(driver);
    while (!expandButtons.isEmpty()) {
      driver.findElement(By.cssSelector("body")).click();
      driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.END);
      try {
        expandButtons.forEach(WebElement::click);
      } catch (Exception exception) {
        log.error("Retry");
      }

      expandButtons = getCollapseButton(driver);
    }

    var rows =
        driver.findElements(By.className("tree-table__row-content")).stream()
            .map(
                i -> {
                  var expandButton = i.findElement(By.className("flex-segment"));

                  var indent = getIdentOfElement(expandButton)/4;
                  var prefix =
                      indent != 0
                          ? "%" + indent + "s%-" + (100 - indent) + "s%s"
                          : "%s%-" + (100 - indent) + "s%s";
                  var postfix =
                      String.format(
                          "%-5s%-5s",
                          i.findElement(By.className("flex-optionality")).getText(),
                          i.findElement(By.className("flex-repeatability")).getText());

                  if (expandButton
                      .getAttribute("class")
                      .contains("tree-table-cell-content-with-button")) {
                    // Button expand
                    return String.format(
                        prefix, "", i.findElement(By.className("cx-body")).getText(), postfix);
                  }

                  return String.format(
                      prefix,
                      "",
                      i.findElement(By.tagName("a")).getText()
                          + i.findElement(By.className("cx-body")).getText(),
                      postfix);
                })
            .collect(Collectors.joining("\n"));
    fileService.write("2.8", rows, "OMG_O19");
    driver.quit();
  }

  private static int getIdentOfElement(WebElement expandButton) {
    var style = expandButton.getAttribute("style");

    if (style == null) {
      return 0;
    }

    var matcher = PADDING_PATTERN.matcher(style);
    if (matcher.matches()) {
      return Integer.parseInt(matcher.group("indent"));
    }

    return 0;
  }

  private static List<WebElement> getCollapseButton(ChromeDriver driver) {
    return driver.findElements(By.cssSelector("button[aria-label=expand]")).stream()
        .filter(i -> i.findElement(By.className("mat-icon")).getText().contains("chevron_right"))
        .collect(Collectors.toList());
  }
}
