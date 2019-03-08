package de.dfki.cos.basys.demonstrator.service.worldmodel;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/worldmodel")
public interface WorldModelService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/")
	String getHull();
	
}
