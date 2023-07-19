package org.example.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.example.constans.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AccountTests extends APITestBase {

    private static final String PASSWORD = "Qwerty3!";
    Logger logger = LoggerFactory.getLogger(AccountTests.class);

    @Test
    public void createNewUser() throws JsonProcessingException {
        logger.info("[Test Name] Test creating new user:");
        String userName = RandomStringUtils.randomAlphanumeric(10);
        Response response = createUser(userName, PASSWORD);
        Assert.assertEquals( response.getStatusCode(),201,
                "User is not created!");

        boolean isAuthorized = isUserAuthorized(userName, PASSWORD);
        if (!isAuthorized){
            authorizeUser(userName, PASSWORD);
        }
        Assert.assertTrue( isUserAuthorized(userName, PASSWORD),
                "User not authorized!");
    }
    @Test
    public void createNewUserWithInvalidPassword() throws JsonProcessingException {
        logger.info("[Test Name] Test creating new user with invalid password:");
        String userName = RandomStringUtils.randomAlphanumeric(10);
        Response response = createUser(userName, "wrongPas");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(mapper.readTree(response.getBody().prettyPrint()).get(Constants.MESSAGE).asText()
                , "Passwords must have at least one non alphanumeric character, one digit ('0'-'9'), one uppercase ('A'-'Z'), one lowercase ('a'-'z'), one special character and Password must be eight characters or longer.");
    }

    @Test
    public void createAlreadyExistingUser() throws JsonProcessingException {
        logger.info("[Test Name] Test creating already existing user:");
        String userName = RandomStringUtils.randomAlphanumeric(10);
        createUser(userName, PASSWORD);
        Response response = createUser(userName, PASSWORD);

        assertEquals(response.getStatusCode(), 406, "Unexpected response code!");
        assertEquals(mapper.readTree(response.getBody().prettyPrint()).get(Constants.MESSAGE).asText()
                , "User exists!", "Unexpected error message!");
    }
}
