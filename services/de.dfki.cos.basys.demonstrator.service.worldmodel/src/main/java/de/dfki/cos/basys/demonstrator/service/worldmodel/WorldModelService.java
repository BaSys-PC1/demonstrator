package de.dfki.cos.basys.demonstrator.service.worldmodel;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.dfki.cos.basys.common.wmrestclient.dto.Hull;

@Path("/worldmodel")
public interface WorldModelService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/")
	Hull getHull();
	
}
