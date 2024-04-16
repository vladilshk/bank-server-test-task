package ru.vovai.bankserver.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncoder {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static void checkPassword(String password, String hashedPassword) {
        BCrypt.checkpw(password, hashedPassword);
    }
}
