package com.na.common.utils;

import java.util.regex.Pattern;

/**
 * @author pg
 * @date 2025年11月06日
 */
public class RequestParamsValidUtil {
    public static boolean isValidLettersOnly(String input) {
        return Pattern.matches("[a-zA-Z]+", input);
    }

    public static boolean isValidLettersAndUnderscore(String input) {
        String regex = "^[a-zA-Z_]+$";
        return Pattern.matches(regex, input);
    }

    public static boolean isValidAlphaNumericUnderscore(String input) {
        return Pattern.matches("^[a-zA-Z0-9_]+$", input);
    }

    public static boolean isValidLettersAndSpecialChars(String input) {
        String regex = "^[a-zA-Z!@#$%^&*()_+-=]+";
        return Pattern.matches(regex, input);
    }

    public static boolean isValidLettersAndChinese(String input) {
        String regex = "^[a-zA-Z\\u4e00-\\u9fa5]+$";
        return Pattern.matches(regex, input);
    }

    public static boolean isValidLettersAndNumbers(String input) {
        String regex = "^[a-zA-Z0-9]+$";
        return Pattern.matches(regex, input);
    }

    public static boolean isValidNumeric(String input) {
        return Pattern.matches("\\d+", input);
    }

    public static boolean isValidLettersAndNumbersAndSymbols(String input) {
        String regex = "^[a-zA-Z0-9!@#$%^&*()_+-=]+$";
        return Pattern.matches(regex, input);
    }

    public static boolean isAlphaNumericWithSpecialChars(String str) {
        String regex = "^[a-zA-Z0-9!@#$%^&*()-_+=<>?{}\\[\\]:;,\"'|]*$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).matches();
    }

    public static boolean isAlphaNumeric(String input) {
        String regex = "^[a-zA-Z0-9]*$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(input).matches();
    }

    public static boolean isValidChinese(String input) {
        return Pattern.matches("[\\u4e00-\\u9fa5]+", input);
    }

    public static boolean isValidEmail(String email) {
        return Pattern.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", email);
    }

    public static boolean isValidIdCard(String idCard) {
        String regex = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])|(0[13-9]|1[0-2])(0[1-9]|[1-2]\\d|30)|(0[13578]|1[02])(0[1-9]|[1-2]\\d|3[01]))\\d{3}([0-9Xx])$)";
        return Pattern.matches(regex, idCard);
    }

    public static boolean isValidChineseAddress(String address) {
        String regex = "^[\\u4e00-\\u9fa5]+[省市区县]+[\\u4e00-\\u9fa5]+$";
        return Pattern.matches(regex, address);
    }

    public static boolean isValidSpecialCharacters(String input) {
        String regex = "^[!@#$%^&*()]+$";
        return Pattern.matches(regex, input);
    }

    public static boolean isValidMobileNumber(String mobileNumber) {
        String regex = "^1[3456789]\\d{9}$";
        return Pattern.matches(regex, mobileNumber);
    }

    public static boolean isValidLandlineNumber(String landlineNumber) {
        String regex = "^(0\\d{2,3}-?)?\\d{7,8}$";
        return Pattern.matches(regex, landlineNumber);
    }
}
