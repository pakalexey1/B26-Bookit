package com.bookit.step_definitions;

import com.bookit.utilities.BookItApiUtil;
import com.bookit.utilities.Environment;
import groovy.util.logging.Log4j;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static io.restassured.RestAssured.*;
import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BookItAPI_StepDefs {

    private static final Logger LOG = LogManager.getLogger();
    String baseUrl = Environment.BASE_URL;
    String accessToken;

    Response response;

    @Given("User logged in to Bookit api as teacher role")
    public void user_logged_in_to_bookit_api_as_teacher_role() {
        String email = Environment.TEACHER_EMAIL;
        String password = Environment.TEACHER_PASSWORD;
        LOG.info("Authorizing teacher user email = " + email + ", password = " + password);
        LOG.info("Environment base url = " + baseUrl);

        accessToken = BookItApiUtil.getAccessToken(email,password);
        if (accessToken == null || accessToken.isEmpty()){
            LOG.error("Couldn't authorize user in authorization server");
            fail("Couldn't authorize user in authorization server");
        }
    }

    @Given("User sends GET request to {string}")
    public void user_sends_get_request_to(String endPoint) {
        response = given().accept(ContentType.JSON)
                .and().header("Authorization", accessToken)
                .when().get(baseUrl + endPoint);
        response.then().log().all();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatus) {
        assertEquals("Status code verification failed",expectedStatus,response.statusCode());
        assertThat(response.statusCode(),is(expectedStatus));
        response.then().statusCode(HttpStatus.SC_OK);
    }

    @Then("content type is {string}")
    public void content_type_is(String expectedContentType) {
        assertEquals(expectedContentType,response.contentType());
        assertThat(response.contentType(), is(ContentType.JSON.toString()));
        response.then().contentType(ContentType.JSON);
    }

    @Then("role is {string}")
    public void role_is(String expectedRole) {
        assertEquals(expectedRole, response.path("role"));

        JsonPath jsonPath = response.jsonPath();
        assertEquals(expectedRole,jsonPath.getString("role"));

        //deserialization: json to map or json to pojo
//        Map<String,?> responseMap = response.as(Map.class);
//        assertEquals(expectedRole,responseMap.get("role"));
    }
}
