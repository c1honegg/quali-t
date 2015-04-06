package controllers;

import dao.models.UserDao;
import logics.authentication.Authenticator;
import models.authentication.Role;
import models.authentication.Token;
import models.authentication.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Authentication extends Controller {

    @Transactional
    public static Result login() {
        Logger.info("login called");
        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");
        String token = requestData.get("token");

        Token t = Authenticator.authenticate(username, password, token);
        if (t == null) {
            return notFound("User and password is not matching");
        } else {
            return ok(Json.toJson(t));
        }
    }

    @Transactional
    public static Result register() {
        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");

        ArrayList<Role> roles = new ArrayList<>();
        String message = Authenticator.registerUser(username, password);

        // TODO refactor messages to somewhere more static!
        if(message.equals("User successfully created")) {
            return ok(Json.toJson(message));
        } else {
            return notFound(message);
        }
    }

    @Transactional
    public static Result logout() {
        Logger.info("logout called");

        DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String token = requestData.get("token");

        String message = Authenticator.invalidateUserSession(username, token);

        // TODO refactor messages to somewhere more static!
        if(message.equals("User session successfully invalidated.")) {
            return ok(Json.toJson(message));
        } else {
            return notFound(message);
        }
    }
}