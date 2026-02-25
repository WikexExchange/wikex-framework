package com.wikex.wikex.util;

import com.wikex.wikex.util.PasswordHasherUtil.PasswordVerificationResult;

public final class PasswordCheckUtil {

    private PasswordCheckUtil() {
    }

    public static boolean isCorrectPassword(String inputPassword, String password, String salt) {
        if (inputPassword == null || password == null) {
            return false;
        }
        try {
            // check new password using MD5 with salt
            boolean newPasswordMatch = MD5.md5(inputPassword + salt)
                    .toLowerCase()
                    .equals(password);
            // check old hashed password using PasswordHasherUtil .net code
            boolean passwordHashedMatch = PasswordHasherUtil.verifyHashedPassword(password,
                    inputPassword) == PasswordVerificationResult.Success;

            return newPasswordMatch || passwordHashedMatch;

        } catch (Exception e) {
            return false;
        }
    }
}