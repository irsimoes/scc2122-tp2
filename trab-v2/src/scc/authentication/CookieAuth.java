package scc.authentication;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response.Status;

import scc.cache.RedisCache;


public class CookieAuth {

    private RedisCache cache = RedisCache.getInstance();
    private static CookieAuth instance;

    public static synchronized CookieAuth getInstance() {
        if(instance != null)
            return instance;
        instance = new CookieAuth();
        return instance;
    }

    public String checkCookie(Cookie session, String id) throws NotAuthorizedException {
        if (session == null || session.getValue() == null)
            throw new WebApplicationException(Status.UNAUTHORIZED);

        String s = cache.getSession(session.getValue());

        if (s == null || !s.equals(id))
            throw new WebApplicationException(Status.UNAUTHORIZED);

        return s;
    }

    public void putSession(String cookieId, String userId) {
        cache.putSession(cookieId, userId);
    }

    public String getSession(Cookie session) {
        if(session == null) throw new WebApplicationException(Status.UNAUTHORIZED);
        return cache.getSession(session.getValue());
    }

    public void deleteCookie(String cookieId) {
        cache.deleteCookie(cookieId);
    }

}
