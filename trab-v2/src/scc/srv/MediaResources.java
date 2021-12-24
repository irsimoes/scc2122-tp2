package scc.srv;

import javax.ws.rs.core.MediaType;
import java.util.List;

import javax.ws.rs.*;
import scc.layers.*;
import scc.utils.*;

@Path(MediaResources.PATH)
public class MediaResources {

    public static final String PATH = "/media";

    BlobStorageLayer blob = BlobStorageLayer.getInstance();

   /**
     * Uploads content
     * @param contents - content to be uploaded
     * @return the generated id
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(byte[] contents) {
        String id = Hash.of(contents);
        blob.upload(contents, id);
        return id;
    }

    /**
     * Downloads the content
     * @param id - id of the content to be downloaded
     * @return the content
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(@PathParam("id") String id) {
        return blob.download(id);
    }

    /**
     * Lists the ids of images stored.
     * @return the list
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list() {
        return blob.list();
    }
}