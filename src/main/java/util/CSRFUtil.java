package util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class CSRFUtil {
    public static String getToken() throws NoSuchAlgorithmException {
        // generate random data
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] data = new byte[16];
        secureRandom.nextBytes(data);

        // convert to Base64 string
        return Base64.getEncoder().encodeToString(data);
    }

    public static Boolean doAction(HttpServletRequest request, HttpServletResponse response) {
        // get the CSRF cookie
        String csrfCookie = null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("csrfToken")) {
                csrfCookie = cookie.getValue();
            }
        }

        // get the CSRF form field
        String csrfField = request.getParameter("csrf");

        // validate CSRF
        if (csrfCookie == null || csrfField == null || !csrfCookie.equals(csrfField)) {
            return false;
        }
        return true;
        // ...
    }
}
