package com.bookit.pages;

import com.bookit.utilities.Driver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.Map;

public class SelfPage {
    public SelfPage(){
        PageFactory.initElements(Driver.getDriver(),this);
    }
    @FindBy (xpath = "//p[.='name']//preceding-sibling::p")
    public WebElement fullName;

    @FindBy (xpath = "//p[.='role']//preceding-sibling::p")
    public WebElement role;

}
