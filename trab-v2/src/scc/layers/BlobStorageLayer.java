package scc.layers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BlobStorageLayer {
	private static BlobStorageLayer instance;
	
	private String path;

	public static synchronized BlobStorageLayer getInstance() {
		if( instance != null)
			return instance;

 		String volumePath = "/mnt/vol";
		instance = new BlobStorageLayer(volumePath);

		return instance;
	}
	
	public BlobStorageLayer(String path ) {
		this.path = path;
	}

	public void upload(byte[] media, String id) {

		try {
			String blobPath = getPath(id);

			File blob = new File(blobPath);
			blob.createNewFile();

			FileOutputStream outputStream = new FileOutputStream(blob);
			outputStream.write(media);
			outputStream.close();

		} catch (IOException e) {
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	public byte[] download(String id) {
		File blob = new File(getPath(id));

		if(!blob.isFile()) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		byte[] media = null;
		try {
			media = new byte[(int) blob.length()];
			DataInputStream dis = new DataInputStream(new FileInputStream(blob));
			dis.readFully(media);
			dis.close();
		} catch (IOException e) {
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		

        return media;
    }

	public List<String> list() {
		return Stream.of(new File(path).listFiles()).map(File::getName).collect(Collectors.toList());
	}

	public boolean blobExists(String id) {
		File blob = new File(getPath(id));
		return blob.isFile();
	}

	private String getPath(String id) {
		return String.format("%s/%s", path, id);
	}

}
