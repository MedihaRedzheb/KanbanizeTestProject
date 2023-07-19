package org.example.pojo;

import java.util.List;

public class User {

    private String userId;
    private String username;
    private List<ISBN> collectionOfIsbns;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<ISBN> getBooks() {
        return collectionOfIsbns;
    }

    public void setBooks(List<ISBN> books) {
        this.collectionOfIsbns = books;
    }
}
