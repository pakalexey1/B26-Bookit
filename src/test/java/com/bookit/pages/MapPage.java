package com.bookit.pages;

import com.bookit.utilities.Driver;
import io.cucumber.java.en_old.Ac;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class MapPage {
    public MapPage(){
        PageFactory.initElements(Driver.getDriver(),this);
    }

    @FindBy(xpath = "//a[@class='navbar-link' and .='my']")
//    @FindBy(xpath = "//div/a[.=\"self\"]/..//preceding-sibling::a[@class=\"navbar-link\"]")
    public WebElement myLink;

    @FindBy(xpath = "//a[@class='navbar-item' and .='self']")
//    @FindBy(xpath = "//a[.='self']")
    public WebElement selfLink;

    public void gotoSelfPage() {
        Actions actions = new Actions(Driver.getDriver());
        actions.moveToElement(myLink);
        actions.moveToElement(myLink).moveToElement(selfLink).click().perform();
    }
}
