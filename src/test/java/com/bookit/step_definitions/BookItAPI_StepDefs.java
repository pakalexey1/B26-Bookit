package com.bookit.step_definitions;

import com.bookit.pages.LoginPage;
import com.bookit.pages.MapPage;
import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookItApiUtil;
import com.bookit.utilities.DBUtils;
import com.bookit.utilities.Driver;
import com.bookit.utilities.Environment;

import groovy.util.logging.Log4j;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.text.html.ListView;
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
    WebDriverWait wait = new WebDriverWait(Driver.getDriver(), 10);
    Map<String, String> newRecordMap;
    Map<String, String> dbTeamInfo;
    Map<String, String> actualTeamFromApiMap;

    @Given("User logged in to Bookit api as teacher role")
    public void user_logged_in_to_bookit_api_as_teacher_role() {
        String email = Environment.TEACHER_EMAIL;
        String password = Environment.TEACHER_PASSWORD;
        LOG.info("Authorizing teacher user email = " + email + ", password = " + password);
        LOG.info("Environment base url = " + baseUrl);

        accessToken = BookItApiUtil.getAccessToken(email, password);
        if (accessToken == null || accessToken.isEmpty()) {
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
        System.out.println(response.statusCode());
        assertEquals("Status code verification failed", expectedStatus, response.statusCode());
//        assertThat(response.statusCode(),is(expectedStatus));
//        response.then().statusCode(HttpStatus.SC_OK);
    }

    @Then("content type is {string}")
    public void content_type_is(String expectedContentType) {
        assertEquals(expectedContentType, response.contentType());
        assertThat(response.contentType(), is(ContentType.JSON.toString()));
        response.then().contentType(ContentType.JSON);
    }

    @Then("role is {string}")
    public void role_is(String expectedRole) {
        assertEquals(expectedRole, response.path("role"));

        JsonPath jsonPath = response.jsonPath();
        assertEquals(expectedRole, jsonPath.getString("role"));

        //deserialization: json to map or json to pojo
        Map<String, ?> responseMap = response.as(Map.class);
//        assertEquals(expectedRole,responseMap.get("role"));
    }

    @Given("User logged in to Bookit app as teacher role")
    public void user_logged_in_to_bookit_app_as_teacher_role() {
        //go to the login page
        Driver.getDriver().get(Environment.URL);
        LoginPage loginPage = new LoginPage();
        loginPage.login(Environment.TEACHER_EMAIL, Environment.TEACHER_PASSWORD);
//        wait.until(ExpectedConditions());
//        assertTrue(Driver.getDriver().getCurrentUrl().endsWith("map"));
    }

    @Given("User is on self page")
    public void user_is_on_self_page() {

        MapPage mapPage = new MapPage();
        mapPage.gotoSelfPage();

    }

    @Then("User should see same info on UI and API")
    public void user_should_see_same_info_on_ui_and_api() {
        SelfPage selfPage = new SelfPage();
        String fullName = selfPage.fullName.getText();
        String role = selfPage.role.getText();

        Map<String, String> uiUserInfo = new HashMap<>();
        uiUserInfo.put("role", role);
        String[] name = fullName.split(" ");// [0] = firstName, [1] = lastName
        uiUserInfo.put("firstName", name[0]);
        uiUserInfo.put("lastName", name[1]);

        System.out.println("uiUserInfo " + uiUserInfo);

        Map<String, ?> responseMap = response.as(Map.class);
        responseMap.remove("id");
        assertThat(uiUserInfo, is(responseMap));
    }

    @When("Users sends POST request to {string} with following info:")
    public void users_sends_post_request_to_with_following_info(String endpoint, Map<String, String> dataMap) {

//        Faker faker = new Faker();
//        if (dataMap.containsKey("email")){
//            dataMap.put("email",faker.internet().emailAddress());
//        }

        response = given().accept(ContentType.JSON)
                .and().queryParams(dataMap)
                .and().header("Authorization", accessToken)
                .when().post(baseUrl + endpoint);
        response.prettyPrint();
        //store into the newRecordMap to use it in the next step validation
        this.newRecordMap = dataMap;
    }

    @Then("Database should persist same team info")
    public void database_should_persist_same_team_info() {
        int newTeamID = response.path("entryiId");

        String sql = "SELECT * FROM team WHERE id = " + newTeamID;
        Map<String, Object> dbNewTeamMap = DBUtils.getRowMap(sql);

        System.out.println("sql = " + sql);
        System.out.println("dbNewTeamMap = " + dbNewTeamMap);

        assertThat(dbNewTeamMap.get("id"), equalTo((long) newTeamID));
        assertThat(dbNewTeamMap.get("name"), equalTo(newRecordMap.get("team-name")));
        assertThat(dbNewTeamMap.get("batch_number").toString(), equalTo(newRecordMap.get("batch-number")));
    }

    @Then("User deletes previously created team")
    public void user_deletes_previously_created_team() {
        int teamId = response.path("entryiId");

        given().accept(ContentType.JSON)
                .and().header("Authorization", accessToken)
                .and().queryParam("id", teamId)
                .when().delete(baseUrl + "/api/teams/{id}")
                .then().log().all();
    }

    @Given("User sends GET request to {string} with {string}")
    public void user_sends_get_request_to_with(String endpoint, String teamId) {
        response = given().accept(ContentType.JSON)
                .and().pathParam("id", teamId)
                .and().header("Authorization", accessToken)
                .when().get(baseUrl + endpoint);

        response.prettyPrint();
    }

    @And("Team name should be {string} in response")
    public void teamNameShouldBeInResponse(String expectedTeamName) {
        JsonPath jsonPath = response.jsonPath();
        String actualTeamName = jsonPath.getString("name");
        String actualTeamId = "" + jsonPath.getInt("id");
        actualTeamFromApiMap = new HashMap<>();
        actualTeamFromApiMap.put(actualTeamId,actualTeamName);
        assertEquals(expectedTeamName, actualTeamName);
    }

    @Then("Database query should have same {string} and {string}")
    public void database_query_should_have_same_and(String teamId, String teamName) {
        int newTeamId = Integer.parseInt(teamId);
        String query = "select id, name from TEAM where id = " + newTeamId;
        dbTeamInfo = new HashMap<>();
        dbTeamInfo.put(DBUtils.getCustomCellValue(query,0)+"",DBUtils.getCustomCellValue(query,1)+"");

        assertEquals(dbTeamInfo,actualTeamFromApiMap);
    }


    @And("Database should contain same student info")
    public void databaseShouldContainSameStudentInfo() {
        int newStudentId = response.path("entryiId");
        String sql = "select * from users u join campus c on u.campus_id = c.id join team t on u.team_id = t.id where u.id=" + newStudentId;
        Map<String, Object> dbStudentMap = DBUtils.getRowMap(sql);
        System.out.println("dbSutdentMap: " + dbStudentMap);

//        assertEquals(newRecordMap.get("first-name"),dbStudentMap.get("firstname"));
//        assertEquals(newRecordMap.get("last-name"),dbStudentMap.get("lastname"));
//        assertEquals(newRecordMap.get("email"),dbStudentMap.get("email"));
//        assertEquals(newRecordMap.get("role"),dbStudentMap.get("role"));
//        assertEquals(newRecordMap.get("campus-location"),dbStudentMap.get("location"));
//        assertEquals(newRecordMap.get("batch-number"),dbStudentMap.get("batch-number"));
//        assertEquals(newRecordMap.get("team-name"),dbStudentMap.get("name"));
    }

    @And("User should able to login bookit app")
    public void userShouldAbleToLoginBookitApp() {

        LoginPage loginPage = new LoginPage();
        loginPage.login(newRecordMap.get("email"), newRecordMap.get("password"));
        MapPage mapPage = new MapPage();
        assertTrue(mapPage.myLink.isDisplayed());

    }

    @And("User deletes previously created student")
    public void userDeletesPreviouslyCreatedStudent() {
        int newStudentId = response.path("entryiId");
        given().accept(ContentType.JSON)
                .and().header("Authorization",accessToken)
                .and().pathParam("id",newStudentId)
                .when().delete(baseUrl+"/api/students/{id}")
                .then().statusCode(204)
                .and().log().all();
    }


}
