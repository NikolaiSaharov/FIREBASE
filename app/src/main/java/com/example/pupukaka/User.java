package com.example.pupukaka;


public class User {
    private String Id;
    private String email;
    private String role;

    public User(){}

    public User(String id,String email,String role){
        this.Id=id;
        this.email=email;
        this.role=role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setId(String id) {
        Id = id;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String Role){
        this.role = role;

    }
}
