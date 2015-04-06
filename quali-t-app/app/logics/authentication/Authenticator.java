package logics.authentication;

import dao.authentication.RoleDao;
import dao.authentication.TokenDao;
import dao.models.UserDao;
import models.authentication.Role;
import models.authentication.Token;
import models.authentication.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import play.Logger;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

/**
 * Created by emre on 31/03/15.
 */
public class Authenticator {
    /**
     * Does simple authentication. Returns the user if authentication was ok. Otherwise null.
     *
     * @param username
     * @param password
     * @param token
     * @return user
     */
    public static Token authenticate(String username, String password, String token) {
        UserDao userDao = new UserDao();
        User u = userDao.findByUsername(username);

        // if user exists, check password
        if (u != null) {
            // check for token
            if (token != null) {
                TokenDao tokenDao = new TokenDao();
                Token tokenOfUser = tokenDao.findByToken(token);
                if (isTokenOfUser(tokenOfUser, u) && isTokenValid(tokenOfUser)) {
                    return tokenOfUser;
                }
            } else {
                // no token
                if (Authenticator.isPasswordCorrect(u, password)) {
                    Token newToken = Authenticator.generateToken(u);
                    u.getToken().add(newToken);
                    userDao.persist(u);
                    return newToken;
                }
            }
        }
        // if no token found or created
        return null;
    }

    /**
     * Checks the validUntil property of the token and compares it with todays date
     *
     * @param tokenOfUser
     * @return
     */
    private static boolean isTokenValid(Token tokenOfUser) {
        Logger.info("in tokenIsValid, argument=" + tokenOfUser);
        DateTime validUntil = tokenOfUser.getValidUntil();
        Logger.info("isvaliduntilbefore? = validUntil.isBefore(new DateTime())");
        if (validUntil.isAfter(new DateTime())) {
            return true;
        }

        return false;
    }

    /**
     * Checks the token if its available and belonging to the given user
     *
     * @param token
     * @param u
     * @return
     */
    private static boolean isTokenOfUser(Token token, User u) {
        return token != null && token.getUser().getId().equals(u.getId());
    }

    /**
     * Checks wether the password of the user is correct or not
     *
     * @param user
     * @param password
     * @return
     */
    public static boolean isPasswordCorrect(@Nullable User user, @NotNull String password) {
        return user != null && user.getHashedPassword().equals(calculatePasswordHash(user.getSalt(), password));
    }

    /**
     * Calculates the password based on the hash
     *
     * @param salt
     * @param password
     * @return
     */
    private static String calculatePasswordHash(@NotNull String salt, @NotNull String password) {
        String hashedPassword = "";

        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("SHA");
            //Add password bytes to digest
            md.update(salt.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest(password.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            hashedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedPassword;
    }

    /**
     * Generates a token for the corresponding user which is valid for 6 months
     *
     * @param user
     * @return
     */
    public static Token generateToken(User user) {
        TokenDao tokenDao = new TokenDao();
        String generatedToken = generateTokenString();

        DateTime date = new DateTime();
        date = date.plusMonths(6);
        Token t = new Token(generatedToken, date, user);

        return t;
    }

    private static String generateTokenString() {
        return RandomStringUtils.randomAlphanumeric(20).toString();
    }

    /**
     * Creates the user in the database with a salt, hashed password and default rules.
     * Returns the message of the backend
     *
     * @param username
     * @param password
     * @return
     */
    public static String registerUser(String username, String password) {
        // Default roles for registered user
        // TODO refactor into logic maybe?
        RoleDao roleDao = new RoleDao();
        List<Role> defaultRoles = roleDao.findDefaultRoles();
        UserDao userDao = new UserDao();

        // check if user already exists
        User user = userDao.findByUsername(username);
        if (user == null) {
            user = new User();
            user.setName(username);
            user.initSalt();
            user.setHashedPassword(calculatePasswordHash(user.getSalt(), password));
            user.getRoles().addAll(roleDao.findDefaultRoles());
            userDao.persist(user);
            // TODO refactor messages to somewhere more static!
            return "User successfully created";
        } else {
            // TODO refactor messages to somewhere more static!
            return "User already exists!";
        }
    }

    /**
     * Invalidates the users session by removing the token. Removes the token only if token belongs to the given user
     *
     * @param username
     * @param token
     * @return
     */
    public static String invalidateUserSession(String username, String token) {
        UserDao userDao = new UserDao();
        User user = userDao.findByUsername(username);

        TokenDao tokenDao = new TokenDao();
        Token userToken = tokenDao.findByToken(token);

        // prevent that a user can invalidate sessions of others
        if (userToken.getUser().equals(user)) {
            user.removeToken(userToken);
            userDao.persist(user);
            tokenDao.remove(userToken);
            // TODO refactor messages to somewhere more static!
            return "User session successfully invalidated.";
        } else {
            // TODO refactor messages to somewhere more static!
            return "Error at invalidating user session.";
        }
    }
}
