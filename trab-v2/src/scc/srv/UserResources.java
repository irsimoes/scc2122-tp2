package scc.srv;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.mongodb.MongoException;

import java.util.*;
import javax.ws.rs.*;

import scc.authentication.CookieAuth;
import scc.cache.RedisCache;
import scc.data.*;
import scc.layers.*;

@Path(UserResources.PATH)
public class UserResources {

    public static final String PATH = "/users";
    private DataLayer data = DataLayer.getInstance();
    private BlobStorageLayer blob = BlobStorageLayer.getInstance();
    private RedisCache cache = RedisCache.getInstance();
    private CookieAuth auth = CookieAuth.getInstance();
    private boolean cacheActive = false;

    /**
     * Creates a new user, given its object
     * 
     * @param user - user object
     * @return the generated ID
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User createUser(User user) {

        String id = user.getId();
        if (id == null || (user.getPhotoId() != null && !blob.blobExists(user.getPhotoId()))
                || (user.getChannelIds().length != 0))
            throw new WebApplicationException(Status.BAD_REQUEST);

        if (cacheActive) { // check if it's in cache so we know it was created
            User u = cache.getValue(id, User.class);
            if (u != null) {
                throw new WebApplicationException(Status.CONFLICT);
            }
        }

        try {
            data.put(id, user, new UserDAO(user), User.class, UserDAO.class, false);
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode());
        }

        return user;
    }

    /**
     * Deletes a user, given its ID
     * 
     * @param id - id of the user to be deleted
     */
    @DELETE
    @Path("/{id}")
    public void deleteUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {

        auth.checkCookie(session, id);
        User u = data.get(id, User.class, UserDAO.class, false);
        if(u == null)
            throw new WebApplicationException(Status.NOT_FOUND);
        try {
            data.delete(id, id, User.class, UserDAO.class, false);
            auth.deleteCookie(session.getValue());
            data.put(id, u, new UserDAO(u), User.class, UserDAO.class, true);
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode());
        }
    }

    /**
     * Updates a user, given its object
     * 
     * @param id - id of the user to be updated
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, User user) {
        auth.checkCookie(session, id);

        User u = data.get(id, User.class, UserDAO.class, false);
        if (u == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        if (!user.getId().equals(u.getId()) || !Arrays.equals(user.getChannelIds(), u.getChannelIds())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        try {
            data.delete(id, id, User.class, UserDAO.class, false);
            data.put(id, user, new UserDAO(user), User.class, UserDAO.class, false);

        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode());
        }
    }

    /**
     * Gets a user, given its id
     * 
     * @param id - id of the user to retrieve
     * @return the user
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public <T> T getUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
       
        User user = data.get(id, User.class, UserDAO.class, false);

        if (user == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        if(session == null || !auth.getSession(session).equals(id))
            return (T) new UserProfile(user);
        return (T) user;
    }

    /**
     * Lists all users
     * 
     * @param id - id of the user to retrieve
     * @return the user
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserProfile> listUsers() {

        List<UserDAO> users = data.getAll(UserDAO.class, false);

        List<UserProfile> l = new ArrayList<>();
        Iterator<UserDAO> it = users.iterator();
        while (it.hasNext()) {
            l.add(new UserProfile(it.next()));
        }

        return l;
    }

    /**
     * Lists the channels of a certain user, given its id.
     * 
     * @return a list with all the channels
     */
    @GET
    @Path("/{id}/channels/list")
    @Produces(MediaType.APPLICATION_JSON)
    public <T> String[] listChannelsOfUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {

        T u = getUser(session, id);
        if(u instanceof UserProfile) //if this returns a user profile then a user is trying to get the channels of another user
            throw new WebApplicationException(Status.UNAUTHORIZED);
        else 
            return ((User)u).getChannelIds();
    }

    /**
     * Subscribes given user to a given channel
     * 
     * @param id - id of the user that will subscribe
     * @param id - id of the channel the user will subscribe to
     */
    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(Login login) {
        String id = login.getId();
        String pwd = login.getPwd();
        
        if (id == null || pwd == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        User user = data.get(id, User.class, UserDAO.class, false);

        if (user != null && pwd.equals(user.getPwd())) {
            String uid = UUID.randomUUID().toString();
            NewCookie cookie = new NewCookie("scc:session", uid, "/", null, "sessionid", 3600, false, true);
            auth.putSession(uid, id);
            return Response.ok().cookie(cookie).build();
        } else {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
    }

}
