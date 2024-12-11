package com.example.abcd.firebaseLogin;

public class HelperClassPOJO {
    String email,password,user;

    public HelperClassPOJO() {
    }

    public HelperClassPOJO(String email, String password, String user) {
        this.email = email;
        this.password = password;
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
