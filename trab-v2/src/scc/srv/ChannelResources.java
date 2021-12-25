package scc.srv;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.mongodb.MongoException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.*;

import scc.authentication.CookieAuth;
import scc.cache.RedisCache;
import scc.data.*;
import scc.layers.*;

@Path(ChannelResources.PATH)
public class ChannelResources {

    public static final String PATH = "channels";
    private RedisCache cache = RedisCache.getInstance();
    private CookieAuth auth = CookieAuth.getInstance();
    private DataLayer data = DataLayer.getInstance();

    /**
     * Creates a channel, given its object.
     * 
     * @param channel - channel object
     * @return the generated id
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Channel createChannel(@CookieParam("scc:session") Cookie session, Channel channel) {

        String owner = channel.getOwner();
        String id = UUID.randomUUID().toString();
        channel.setId(id);

        auth.checkCookie(session, owner);
       
        if (channel.getName() == null || owner == null || channel.getMembers().size() != 1 || 
            !channel.getMembers().get(0).equals(owner) || data.get(channel.getOwner(), User.class, UserDAO.class, false) == null)
            throw new WebApplicationException(Status.BAD_REQUEST);
            
        try {
            data.put(channel.getId(), channel, new ChannelDAO(channel), Channel.class, ChannelDAO.class, false);
            data.patchAdd(owner, User.class, UserDAO.class, "channelIds", id);
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode() != 11000 ? e.getCode() : 409);
        }
        return channel;
    }

    /**
     * Deletes a channel, given its id.
     * 
     * @param id - id of the channel to be deleted
     */
    @DELETE
    @Path("/{id}")
    public void deleteChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
        
        Channel channel = data.get(id, Channel.class, ChannelDAO.class, false);
        if(channel == null)
            throw new WebApplicationException(Status.NOT_FOUND);

        auth.checkCookie(session, channel.getOwner());

        try {
            data.delete(id, id, Channel.class, ChannelDAO.class, false);
            data.put(id, channel, new ChannelDAO(channel), Channel.class, ChannelDAO.class, true);
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode() != 11000 ? e.getCode() : 409);
        }
    }

    /**
     * Updates a channel, given its object.
     * 
     * @param id - id of the channel to be updated
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Channel channel) {

        Channel preChannel = data.get(id, Channel.class, ChannelDAO.class, false);

        if (preChannel == null || channel.getId() == null || channel.getName() == null || channel.getOwner() == null
                || !channel.getId().equals(preChannel.getId()) || !channel.getOwner().equals(preChannel.getOwner())
                || !channel.getMembers().equals(preChannel.getMembers())) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        auth.checkCookie(session, preChannel.getOwner());

        try {
            data.delete(id, id, Channel.class, ChannelDAO.class, false);
            data.put(id, channel, new ChannelDAO(channel), Channel.class, ChannelDAO.class, false);
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode() != 11000 ? e.getCode() : 409);
        }
    }

    /**
     * Gets a channel, given its id
     * 
     * @param id - id of the channel to retrieve
     * @return the channel
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Channel getChannel(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {

        Channel channel = data.get(id, Channel.class, ChannelDAO.class, false);
        if(channel == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if(!channel.isPublicChannel()) {
            String userId = auth.getSession(session);

            if (userId == null || !channel.getMembers().contains(userId))
                throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        return channel;
    }

    /**
     * Gets a channel's messages, given its id
     * 
     * @param id - id of the channel
     * @return the list of messages of the channel
     */
    @GET
    @Path("/{id}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Message> getChannelMessages(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, @QueryParam("st") Integer st, @QueryParam("len") Integer len) {

        if(st == null || len == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        
        Channel channel = data.get(id, Channel.class, ChannelDAO.class, false);
        if(channel == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        String userId = auth.getSession(session);

        if (userId == null || !channel.getMembers().contains(userId))
            throw new WebApplicationException(Status.UNAUTHORIZED);
    
        List<MessageDAO> messages = data.getMessagesFromChannel(id, st, len);

        List<Message> l = new ArrayList<>();
        Iterator<MessageDAO> it = messages.iterator();
        while (it.hasNext()) {
            l.add(new Message(it.next()));
        }

        return l;
    }

    /**
     * @return the list of the trending channels
     */
    @GET
    @Path("/trending")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] trendingChannels() {
        return cache.getTrendingChannels();
    }

    /**
     * Subscribes given user to a given channel
     * 
     * @param id - id of the user that will subscribe
     * @param id - id of the channel the user will subscribe to
     */
    @PUT
    @Path("{channelId}/subscribe/{userId}")
    public void subChannel(@CookieParam("scc:session") Cookie session, @PathParam("userId") String userId, @PathParam("channelId") String channelId) {
        
        auth.checkCookie(session, userId);

        Channel channel = data.get(channelId, Channel.class, ChannelDAO.class, false);
        User user = data.get(userId, User.class, UserDAO.class, false);

        if (channel == null || user == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        if(!channel.isPublicChannel()) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        memberAddition(channel, user);
    }

    /**
     * Unsubscribes given user to a given channel
     * 
     * @param id - id of the user that will subscribe
     * @param id - id of the channel the user will subscribe to
     */
    @PUT
    @Path("{channelId}/unsubscribe/{userId}")
    public void unsubChannel(@CookieParam("scc:session") Cookie session, @PathParam("userId") String userId, @PathParam("channelId") String channelId) {

        auth.checkCookie(session, userId);

        Channel channel = data.get(channelId, Channel.class, ChannelDAO.class, false);
        User user = data.get(userId, User.class, UserDAO.class, false);

        if (channel == null || user == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        memberRemoval(channel, user);
    }

    @PUT
    @Path("{channelId}/add/{userId}")
    public void addMember(@CookieParam("scc:session") Cookie session, @PathParam("channelId") String channelId, @PathParam("userId") String userId) {

        Channel channel = data.get(channelId, Channel.class, ChannelDAO.class, false);
        if(channel == null) 
            throw new WebApplicationException(Status.BAD_REQUEST);
        
        auth.checkCookie(session, channel.getOwner());

        User user = data.get(userId, User.class, UserDAO.class, false);
        if (user == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        memberAddition(channel, user);
    }

    @PUT
    @Path("{channelId}/remove/{userId}")
    public void removeMember(@CookieParam("scc:session") Cookie session, @PathParam("channelId") String channelId, @PathParam("userId") String userId){
        
        Channel channel = data.get(channelId, Channel.class, ChannelDAO.class, false);
        if(channel == null) 
            throw new WebApplicationException(Status.BAD_REQUEST);
        
        auth.checkCookie(session, channel.getOwner());

        User user = data.get(userId, User.class, UserDAO.class, false);
        if (user == null)
            throw new WebApplicationException(Status.BAD_REQUEST);
        
        memberRemoval(channel, user);
    }

    private void memberAddition(Channel channel, User user) {
        String channelId = channel.getId();
        String userId = user.getId();

        if (channel.getMembers().contains(userId)) 
            return; // return so the user thinks that he was added even though he was already there

        try{
            data.patchAdd(channelId, Channel.class, ChannelDAO.class, "members", userId);
            data.patchAdd(userId, User.class, UserDAO.class, "channelIds", channelId);
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode());
        }
        
    }

    private void memberRemoval(Channel channel, User user) {
        String userId = user.getId();
        String channelId = channel.getId();
        
        if (!channel.getMembers().contains(userId) || channel.getOwner().equals(userId)) {
            return;
        }

        try {
            data.patchRemove(channelId, Channel.class, ChannelDAO.class, "members", userId);
            data.patchRemove(userId, User.class, UserDAO.class, "channelIds", channelId);
        } catch (MongoException e) {
            throw new WebApplicationException(e.getCode());
        }
        
    }

}
