package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import cors.CorsAction;
import dao.UnknownUsername;
import dao.UserAlreadyExistsException;
import dao.UserDAO;
import model.Education;
import model.Experience;
import model.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.Auth;
import security.Secure;
import utils.HashUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
        List<String> roles=new ArrayList<String>();
        roles.add("user");
        if( auth.authentify(user,password) == false ) return unauthorized("wrong_credentials");
        else if( auth.verifyRoles(user,roles) == false ) return unauthorized("unauthorized");
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
        user.setAddress("");
        user.setDescription("");
        user.setDob("");
        user.setEducation(new ArrayList<Education>());
        user.setExperiences(new ArrayList<Experience>());
        user.setFirstname("");
        user.setGithub("");
        user.setLastname("");
        user.setLinkedin("");
        user.setNumber("");
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
