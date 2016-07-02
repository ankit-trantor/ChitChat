package com.google.firebase.codelab.friendlychat;

/**
 * Created by kapil pc on 7/2/2016.
 */
public class Users {

    private String name;
    private String userId;
    private String photoUrl;

    public Users() {
    }

    public Users(String name, String userId, String photoUrl) {

        this.name = name;
        this.userId = userId;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }


}
