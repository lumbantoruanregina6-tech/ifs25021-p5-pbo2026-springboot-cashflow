package org.delcom._1s25021springbootcashflow.framework.auth;

import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Menyimpan user login di VaadinSession + HTTP session + cookie (cashflow_uid).
 * Cookie ditandatangani HMAC agar id tidak bisa dipalsukan.
 * Flag LOGGED_OUT_KEY mencegah sesi dipulihkan setelah logout.
 */
public final class AuthSession {
    private static final String USER_ID_KEY = "cashflow.userId";
    private static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;
    private static final byte[] SECRET = new byte[32];

    static {
        new SecureRandom().nextBytes(SECRET);
    }

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession vaadin = VaadinSession.getCurrent();
        if (vaadin != null) {
            vaadin.setAttribute(USER_ID_KEY, user.getId());
            vaadin.setAttribute(LOGGED_OUT_KEY, null);
        }
        HttpSession http = httpSession(true);
        if (http != null) {
            http.setAttribute(USER_ID_KEY, user.getId());
            http.removeAttribute(LOGGED_OUT_KEY);
        }
        writeCookie(signed(user.getId()), COOKIE_MAX_AGE);
    }

    public static void logout() {
        VaadinSession vaadin = VaadinSession.getCurrent();
        if (vaadin != null) {
            vaadin.setAttribute(USER_ID_KEY, null);
            vaadin.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        HttpSession http = httpSession(false);
        if (http != null) {
            http.removeAttribute(USER_ID_KEY);
            http.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        writeCookie("", 0);
    }

    /** Mengembalikan user yang sedang login, atau null. Memulihkan sesi setelah refresh. */
    public static User currentUser(IUserRepository users) {
        VaadinSession vaadin = VaadinSession.getCurrent();
        HttpSession http = httpSession(false);

        if (vaadin != null && vaadin.getAttribute(LOGGED_OUT_KEY) != null) return null;
        if (http != null && http.getAttribute(LOGGED_OUT_KEY) != null) return null;

        Integer id = null;
        if (vaadin != null) id = (Integer) vaadin.getAttribute(USER_ID_KEY);
        if (id == null && http != null) id = (Integer) http.getAttribute(USER_ID_KEY);
        if (id == null) id = idFromCookie();
        if (id == null) return null;

        Optional<User> user = users.findById(id);
        if (user.isEmpty()) return null;
        if (vaadin != null) vaadin.setAttribute(USER_ID_KEY, id);
        return user.get();
    }

    // ---------- helper ----------

    private static HttpSession httpSession(boolean create) {
        HttpServletRequest request = servletRequest();
        return request == null ? null : request.getSession(create);
    }

    private static HttpServletRequest servletRequest() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        return request instanceof VaadinServletRequest servlet ? servlet.getHttpServletRequest() : null;
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) return;
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static Integer idFromCookie() {
        HttpServletRequest request = servletRequest();
        if (request == null || request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return null;
    }

    private static String signed(int id) {
        return id + "." + hmac(String.valueOf(id));
    }

    private static Integer verify(String value) {
        if (value == null) return null;
        int dot = value.indexOf('.');
        if (dot < 1) return null;
        String idPart = value.substring(0, dot);
        String sig = value.substring(dot + 1);
        boolean ok = MessageDigest.isEqual(hmac(idPart).getBytes(StandardCharsets.UTF_8),
                sig.getBytes(StandardCharsets.UTF_8));
        if (!ok) return null;
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC gagal", e);
        }
    }
}
