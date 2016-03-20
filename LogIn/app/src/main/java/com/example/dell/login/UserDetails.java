package com.example.dell.login;

public class UserDetails {
    private String _id;
    private String _name;
    private String _password;
    public UserDetails(){}
    public UserDetails(String _id, String _name, String _password)
    {
        this._id=_id;
        this._name=_name;
        this._password=_password;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }
}
