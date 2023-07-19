package org.example.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class APITestBase {
    protected static ObjectMapper mapper = new ObjectMapper();
    private static Logger logger = LoggerFactory.getLogger(APITestBase.class);

    public static String BASE_URL = "https://demoqa.com/";

    public static final String CREATE_USER_ENDPOINT_PATH = "Account/v1/User";
    public static final String GENERATE_TOKEN_ENDPOINT_PATH = "Account/v1/GenerateToken";
    public static final String IS_USER_AUTHORIZED_ENDPOINT_PATH = "Account/v1/Authorized";
    public static final String GET_USER_ENDPOINT_PATH = "Account/v1/User/{UUID}";
    public static final String GET_BOOK_ENDPOINT_PATH = "/BookStore/v1/Book";
    public static final String GET_ALL_BOOK_ENDPOINT_PATH = "/BookStore/v1/Books";
    public static final String ADD_BOOK_TO_FAVOURITE_LIST_ENDPOINT_PATH = "/BookStore/v1/Books";
    public static final String REPLACE_BOOK_TO_FAVOURITE_LIST_ENDPOINT_PATH = "/BookStore/v1/Books/{ISBN}";
    public static final String REMOVE_BOOK_FROM_FAVOURITE_LIST_ENDPOINT_PATH = "/BookStore/v1/Book";

    public static String authorizeUser(String userName, String password) throws JsonProcessingException {
        logger.info("Authorizing user ...");
        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("userName", userName);
        requestBody.put("password", password);

        Response response = RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(GENERATE_TOKEN_ENDPOINT_PATH)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract().response();
        ;

        String responseBody = response.getBody().prettyPrint();
        JsonNode responseBodyJson = new ObjectMapper().readTree(responseBody);
        return responseBodyJson.get("token").asText();
    }

    public static String getUserID(String userName, String password) throws JsonProcessingException {
        logger.info("Getting user ID ...");
        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("userName", userName);
        requestBody.put("password", password);

        Response response = RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(CREATE_USER_ENDPOINT_PATH)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract().response();

        String responseBody = response.getBody().prettyPrint();
        JsonNode responseBodyJson = new ObjectMapper().readTree(responseBody);
        return responseBodyJson.get("userID").asText();
    }

    public static Response createUser(String userName, String password) {
        logger.info("Creating user ...");
        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("userName", userName);
        requestBody.put("password", password);

        Response response = RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(CREATE_USER_ENDPOINT_PATH)
                .body(requestBody)
                .when()
                .post();

        logger.info("Response code: " + response.getStatusCode());
        return response;
    }

    public static Response getUser(String userId, String token) {
        logger.info("Getting user ...");
        Response response = RestAssured
                .given()
                .auth()
                .oauth2(token)
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(GET_USER_ENDPOINT_PATH)
                .pathParam("UUID", userId)
                .when()
                .get();

        logger.info("Response code: " + response.getStatusCode());
        return response;
    }

    public static String getBook(String isbn) {
        logger.info("Getting book ...");
        Response response = RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(GET_BOOK_ENDPOINT_PATH)
                .queryParam("ISBN", isbn)
                .when()
                .get();

        logger.info("Response code: " + response.getStatusCode());
        return response.getBody().prettyPrint();
    }

    public static String getRandomBookISBN() throws JsonProcessingException {
        List<String> allBooksISBN = new ArrayList<>();

        Response response = RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(GET_ALL_BOOK_ENDPOINT_PATH)
                .when()
                .get();

        int countOfBooks = mapper.readTree(response.getBody().prettyPrint()).get("books").size();

        int i = 0;
        while (i < countOfBooks) {
            String currentISBN = mapper.readTree(response.getBody().prettyPrint()).get("books").get(i).get("isbn").asText();
            allBooksISBN.add(currentISBN);
            i++;
        }
        Random random = new Random();

        return allBooksISBN.get(random.nextInt(allBooksISBN.size()));
    }

    public static boolean isUserAuthorized(String userName, String password) {
        logger.info("Checking if user is authorized ...");
        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("userName", userName);
        requestBody.put("password", password);

        String response = RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(IS_USER_AUTHORIZED_ENDPOINT_PATH)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract().asString();

        return Boolean.valueOf(response);
    }

    public static Response addBookToFavouriteList(String userId, ArrayNode collectionOfIsbns, String token) {
        logger.info("Adding book to favourite list ...");
        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("userId", userId);
        requestBody.put("collectionOfIsbns", collectionOfIsbns);

        Response response = RestAssured
                .given()
                .auth()
                .oauth2(token)
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(ADD_BOOK_TO_FAVOURITE_LIST_ENDPOINT_PATH)
                .body(requestBody)
                .when()
                .post();

        logger.info("Response code: " + response.getStatusCode());
        return response;
    }

    public static Response removeABookFromFavouriteList(String userId, String isbn, String token) {
        logger.info("Removing book from favourite list...");
        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("userId", userId);
        requestBody.put("isbn", isbn);

        Response response = RestAssured
                .given()
                .auth()
                .oauth2(token)
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(REMOVE_BOOK_FROM_FAVOURITE_LIST_ENDPOINT_PATH)
                .body(requestBody)
                .when()
                .delete();

        logger.info("Response code: " + response.getStatusCode());
        return response;
    }

    public static Response replaceBook(String userId, String currentBookISBN, String replaceBookISBN, String token) {
        logger.info("Replacing book in user's list ...");
        ObjectNode requestBody = new ObjectMapper().createObjectNode();
        requestBody.put("userId", userId);
        requestBody.put("isbn", replaceBookISBN);

        Response response = RestAssured
                .given()
                .auth()
                .oauth2(token)
                .accept("application/json")
                .contentType("application/json")
                .baseUri(BASE_URL)
                .basePath(REPLACE_BOOK_TO_FAVOURITE_LIST_ENDPOINT_PATH)
                .pathParam("ISBN", currentBookISBN)
                .body(requestBody)
                .when()
                .put();

        logger.info("Response code: " + response.getStatusCode());
        return response;
    }
}

