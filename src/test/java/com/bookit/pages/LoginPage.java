package com.bookit.pages;


import com.bookit.utilities.Driver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {
    public LoginPage(){
        PageFactory.initElements(Driver.getDriver(),this);

    }

    @FindBy(name = "email")
    public WebElement email;

    @FindBy(name = "password")
    public WebElement password;

    @FindBy(xpath = "//button[.='sign in']")
    public WebElement signInBtn;

    public void login (String userEmail, String userPassword){
        email.sendKeys(userEmail);
        password.sendKeys(userPassword);
        signInBtn.click();
    }


}
