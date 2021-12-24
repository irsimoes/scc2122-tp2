package scc.srv;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.mongodb.MongoException;

import java.util.*;

import javax.ws.rs.*;

import scc.authentication.CookieAuth;
import scc.cache.RedisCache;
import scc.data.*;
import scc.layers.*;

@Path(MessageResources.PATH)
public class MessageResources {

    public static final String PATH = "/messages";
    private BlobStorageLayer blob = BlobStorageLayer.getInstance();
    private RedisCache cache = RedisCache.getInstance();
    private DataLayer data = DataLayer.getInstance();
    private CookieAuth auth = CookieAuth.getInstance();

    /**
     * Creates a message given its id.
     * 
     * @param message - message object
     * @return the generated id
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createMessage(@CookieParam("scc:session") Cookie session, Message message) {

        if (message.getChannel() == null || message.getUser() == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        User user = data.get(message.getUser(), User.class, UserDAO.class, false);
        if(user == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        auth.checkCookie(session, user.getId());

        Channel channel = data.get(message.getChannel(), Channel.class, ChannelDAO.class, false);
        if (channel == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        if (!Arrays.asList(channel.getMembers()).contains(user.getId()))
            throw new WebApplicationException(Status.UNAUTHORIZED);

        Message replyTo = data.get(message.getReplyTo(), Message.class, MessageDAO.class, false);
        if (message.getReplyTo() != null && replyTo == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        if(replyTo != null && !((replyTo.getChannel()).equals(message.getChannel())))
            throw new WebApplicationException(Status.BAD_REQUEST);

        if (message.getImageId() != null && !blob.blobExists(message.getImageId()))
            throw new WebApplicationException(Status.BAD_REQUEST);

        String randID = UUID.randomUUID().toString();
        message.setId(randID);

        try {
            MessageDAO messageDAO = new MessageDAO(message);
            messageDAO.set_ts(System.currentTimeMillis());
            data.put(message.getId(), message, messageDAO, Message.class, MessageDAO.class, false);
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode());
        }

        if(channel.isPublicChannel()) //only the public channels can be trending
            cache.incrementLeaderboard(channel.getId());

        return randID;
    }

    /**
     * Delete the message, given its id.
     * 
     * @param id - id of the message to be deleted
     */
    @DELETE
    @Path("/{id}")
    public void deleteMessage(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
        
        Message message = data.get(id, Message.class, MessageDAO.class, false);
        if(message == null)
            throw new WebApplicationException(Status.NOT_FOUND);
                
        auth.checkCookie(session, message.getUser());
        
        try {
            data.delete(id, message.getChannel(), Message.class, MessageDAO.class, false);
            data.put(id, message, new MessageDAO(message), Message.class, MessageDAO.class, true);
        } catch (MongoException e) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
    }

    /**
     * Gets a message, given its id
     * 
     * @param id - id of the message to retrieve
     * @return the message
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Message getMessage(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {

        Message message = data.get(id, Message.class, MessageDAO.class, false);
        if(message == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        Channel channel = data.get(message.getChannel(), Channel.class, ChannelDAO.class, false);
        if(channel == null) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        String userId = auth.getSession(session);

        if (userId == null || !Arrays.asList(channel.getMembers()).contains(userId))
            throw new WebApplicationException(Status.UNAUTHORIZED);

        return message;
    }
}
