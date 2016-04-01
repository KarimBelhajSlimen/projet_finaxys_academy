package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import cors.CorsAction;
import dao.UnknownUsername;
import dao.UserAlreadyExistsException;
import dao.UserDAO;
import model.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.Auth;
import utils.HashUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AuthController extends RestController {

    /**
     * Takes credentials and returns JWT when successfull
     */
    public Result login() throws IOException {
        JsonNode json = jsonRequest();
        String username = null;
        String password = null;
        try {
            username = json.findPath("username").asText();
            password = json.findPath("password").asText();
        }catch(NullPointerException e){
            return unauthorized("wrong_format");
        }
        User user = null;
        try {
            user = new UserDAO().getByUsername( username );
        } catch (UnknownUsername unknownUsername) {
            return unauthorized("unknown_username");
        }
        Auth auth = new Auth();
        if( auth.authentify(user,password) == false ) return unauthorized("wrong_credentials");
        else return ok(auth.generateJWT(user));
    }

    /**
     * Creates new user and returns JWT
     */
    public Result signUp() throws NoSuchAlgorithmException, IOException {
        JsonNode json = jsonRequest();
        String username = null;
        String password = null;
        try {
            username = json.findPath("username").asText();
            password = json.findPath("password").asText();
        }catch(NullPointerException e){
            return unauthorized("wrong_format");
        }
        User user = new User();
        user.setEmail(username);
        user.setRoles( Arrays.asList(new String[]{"user"})  );
        user.setPasswordHash( new HashUtil().hash(password) );
        UserDAO userDAO = new UserDAO();
        try {
            userDAO.createUser(user);
        } catch (UserAlreadyExistsException e) {
            return unauthorized("user_already_exists");
        }
        Auth auth = new Auth();
        return ok(auth.generateJWT(user));
    }

}
