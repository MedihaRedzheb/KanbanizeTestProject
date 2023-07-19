package org.example.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.example.constans.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.example.pojo.Book;
import org.example.pojo.ISBN;

import static org.testng.Assert.assertEquals;

public class BookStoreTests extends APITestBase {
    private static final String PASSWORD = "Qwerty3!";
    Logger logger = LoggerFactory.getLogger(BookStoreTests.class);

    @Test
    public void validateThatBookWithISBNHasNPages() {
        logger.info("[Test Name] Validate that book with ISBN has N pages:");
        String book = getBook("9781491904244");

        JsonPath jpath = new JsonPath(book);

        int pages = jpath.getInt(Constants.PAGES);

        assertEquals(pages, 278);
    }

    @Test
    public void addBookToFavouriteList() throws JsonProcessingException {
        logger.info("[Test Name] Add book to favourite list:");
        String userName = RandomStringUtils.randomAlphanumeric(10);
        String userID = getUserID(userName, PASSWORD);
        String token = authorizeUser(userName, PASSWORD);

        String book = getBook(getRandomBookISBN());

        Book bookToAdd = mapper.treeToValue(mapper.readTree(book), Book.class);
        ArrayNode collectionOfIsbns = mapper.createArrayNode();
        collectionOfIsbns.add(mapper.convertValue(new ISBN(bookToAdd.getIsbn()), JsonNode.class));

        Response response = addBookToFavouriteList(userID, collectionOfIsbns, token);

        assertEquals(response.getStatusCode(), 201);
        assertEquals((mapper.readTree(response.getBody().prettyPrint())).get(Constants.BOOKS).get(0).get(Constants.ISBN).asText()
                , collectionOfIsbns.get(0).get(Constants.ISBN).asText());
    }

    @Test
    public void addNonExistentBookToFavouriteList() throws JsonProcessingException {
        logger.info("[Test Name] Add non-existent book to favourite list:");
        String userName = RandomStringUtils.randomAlphanumeric(10);
        String userID = getUserID(userName, PASSWORD);
        String token = authorizeUser(userName, PASSWORD);

        String book = getBook(getRandomBookISBN());

        Book bookToAdd = mapper.treeToValue(mapper.readTree(book), Book.class);
        bookToAdd.setIsbn("9781491904255");
        ArrayNode collectionOfIsbns = mapper.createArrayNode();
        collectionOfIsbns.add(mapper.convertValue(new ISBN(bookToAdd.getIsbn()), JsonNode.class));

        Response response = addBookToFavouriteList(userID, collectionOfIsbns, token);

        assertEquals(response.getStatusCode(), 400);
        assertEquals((mapper.readTree(response.getBody().prettyPrint()).get(Constants.MESSAGE).asText())
                , "ISBN supplied is not available in Books Collection!");
    }

    @Test
    public void replaceBookInFavouriteList() throws JsonProcessingException {
        logger.info("[Test Name] Replace book in favourite list:");
        String userName = RandomStringUtils.randomAlphanumeric(10);
        String userID = getUserID(userName, PASSWORD);
        String token = authorizeUser(userName, PASSWORD);

        String currentBook = getBook(getRandomBookISBN());

        Book bookToAdd = mapper.treeToValue(mapper.readTree(currentBook), Book.class);
        ArrayNode collectionOfIsbns = mapper.createArrayNode();
        collectionOfIsbns.add(mapper.convertValue(new ISBN(bookToAdd.getIsbn()), JsonNode.class));

        addBookToFavouriteList(userID, collectionOfIsbns, token);

        getUser(userID, token);

        String replaceBook = getBook(getRandomBookISBN());

        if ((mapper.readTree(currentBook).get(Constants.ISBN)).asText().equals(mapper.readTree(replaceBook).get(Constants.ISBN).asText())) {
            replaceBook = getBook(getRandomBookISBN());
        }

        Response response = replaceBook(userID, (mapper.readTree(currentBook).get(Constants.ISBN)).asText(), (mapper.readTree(replaceBook).get(Constants.ISBN)).asText(), token);

        Response user = getUser(userID, token);

        assertEquals(response.getStatusCode(), 200);

        assertEquals(mapper.readTree(user.asString()).get(Constants.BOOKS).get(0).get(Constants.ISBN).asText(), (mapper.readTree(replaceBook).get(Constants.ISBN)).asText());
    }

    @Test
    public void removeBookFromFavouriteList() throws JsonProcessingException {
        logger.info("[Test Name] Removing book from favourite list:");
        String userName = RandomStringUtils.randomAlphanumeric(10);
        String userID = getUserID(userName, PASSWORD);
        String token = authorizeUser(userName, PASSWORD);

        String book = getBook(getRandomBookISBN());

        Book bookToAdd = mapper.treeToValue(mapper.readTree(book), Book.class);
        ArrayNode collectionOfIsbns = mapper.createArrayNode();
        collectionOfIsbns.add(mapper.convertValue(new ISBN(bookToAdd.getIsbn()), JsonNode.class));

        addBookToFavouriteList(userID, collectionOfIsbns, token);

        Response response = removeABookFromFavouriteList(userID, bookToAdd.getIsbn(), token);

        assertEquals(response.getStatusCode(), 204);

    }

    @Test
    public void removeNonExistentBookFromFavouriteList() throws JsonProcessingException {
        logger.info("[Test Name] Remove non-existent book from favourite list:");
        String userName = RandomStringUtils.randomAlphanumeric(10);
        String userID = getUserID(userName, PASSWORD);
        String token = authorizeUser(userName, PASSWORD);

        Response response = removeABookFromFavouriteList(userID, getRandomBookISBN(), token);

        assertEquals(response.getStatusCode(), 400);
        assertEquals((mapper.readTree(response.getBody().prettyPrint()).get(Constants.MESSAGE).asText())
                , "ISBN supplied is not available in User's Collection!");

    }
}
