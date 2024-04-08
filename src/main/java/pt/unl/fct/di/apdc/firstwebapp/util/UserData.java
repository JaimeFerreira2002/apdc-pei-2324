package pt.unl.fct.di.apdc.firstwebapp.util;

public class UserData {

    public String username;
    public String email;

    public String name;

    public String phone;
    public String password;
    public String passwordConfirmation;

    public String role;

    public String state;


    public UserData() {}

    public UserData(String username, String email, String name,String phone, String password, String passwordConfirmation, String role, String state) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
        this.role = role;
        this.state = state;

    }
}