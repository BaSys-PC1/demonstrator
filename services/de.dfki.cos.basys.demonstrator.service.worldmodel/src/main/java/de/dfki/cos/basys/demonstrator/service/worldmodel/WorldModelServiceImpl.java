package de.dfki.cos.basys.demonstrator.service.worldmodel;

import java.io.StringReader;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.dfki.cos.basys.common.emf.json.JsonUtils;
import de.dfki.cos.basys.common.wmrestclient.WorldModelRestClient;
import de.dfki.cos.basys.common.wmrestclient.WorldModelRestClientImpl;
import de.dfki.cos.basys.common.wmrestclient.WorldModelRestClientMockup;
import de.dfki.cos.basys.common.wmrestclient.dto.Hull;
import de.dfki.cos.basys.common.wmrestclient.dto.RivetPosition;
import de.dfki.cos.basys.common.wmrestclient.dto.RivetPosition.State;
import de.dfki.cos.basys.common.wmrestclient.dto.Sector.SectorEnum;
import de.dfki.cos.basys.platform.model.runtime.communication.Channel;
import de.dfki.cos.basys.platform.model.runtime.communication.Notification;
import de.dfki.cos.basys.platform.model.runtime.communication.Request;
import de.dfki.cos.basys.platform.model.runtime.communication.Response;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentRequestStatus;
import de.dfki.cos.basys.platform.model.runtime.component.RequestStatus;
import de.dfki.cos.basys.platform.model.runtime.component.impl.ComponentRequestStatusImpl;
import de.dfki.cos.basys.platform.runtime.communication.CommFactory;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.service.ServiceComponent;

public class WorldModelServiceImpl extends ServiceComponent implements WorldModelService {

	WorldModelRestClient client;
	String hullID = null;
	
	public WorldModelServiceImpl(ComponentConfiguration config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void connectToExternal() throws ComponentException {
		if ("-".equals(getConfig().getExternalConnectionString()))
			client = new WorldModelRestClientMockup(getConfig().getExternalConnectionString());
		else 
			client = new WorldModelRestClientImpl(getConfig().getExternalConnectionString());
	}
	
	@Override
	public Hull getHull() {
		List<Hull> hulls = client.getHulls();
		//return hulls.get(0);
		return client.getHull(hulls.get(0).getId());
		
		//return "{ \"todo\" : \"implement\"}";
	}
	
	@Override
	public void handleNotification(Channel channel, Notification not) {
		LOGGER.debug("handleNotification");
		LOGGER.debug(not.getPayload());
		
		JsonReader jsonReader = Json.createReader(new StringReader(not.getPayload()));
		JsonObject jsonIn = jsonReader.readObject();
		
		if ("updateRivetPosition".equals(jsonIn.getString("action"))) {
			int frameIndex = jsonIn.getInt("frameIndex");
			int rivetIndex = jsonIn.getInt("rivetIndex");
			String state = jsonIn.getString("state");
			RivetPosition rivetPosition = new RivetPosition(rivetIndex).setFrameIndex(frameIndex).setState(State.valueOf(State.class, state));
			if (client.updateRivetPosition(rivetPosition)) {
				//TODO: notify on outChannel --> MQTT --> Dashboard
				//TODO: check if sending notification in handleNotification is possible -> otherwise do handleNotification in extra thread in CommProvider
				Notification not2 = CommFactory.getInstance().createNotification(not.getPayload());
				outChannel.sendNotification(not2);
			}
			else {
				LOGGER.warn("Updating rivet position {} FAILED!", rivetPosition.toString());
			}
		}	
	}
	
	@Override
	public Response handleRequest(Channel channel, Request request) {
		LOGGER.debug("handleRequest");
		LOGGER.debug(request.getPayload());
		
		// TODO Auto-generated method stub
		//return super.handleRequest(channel, request);
		JsonReader jsonReader = Json.createReader(new StringReader(request.getPayload()));
		JsonObject jsonIn = jsonReader.readObject();
		
		if ("getRivetPositions".equals(jsonIn.getString("action"))) {
			int count = jsonIn.getInt("count");
			String sector = jsonIn.getString("sector");
			String state = jsonIn.getString("state");			
			
			if(hullID == null)
				hullID = client.getHulls().get(0).getId();	
			
			List<RivetPosition> result = client.getRivetPositions(
					hullID, 
					SectorEnum.valueOf(SectorEnum.class, sector), 
					count, 
					State.valueOf(State.class, state), 
					true);	
		
			try {
				ObjectMapper mapper = new ObjectMapper();
				String jsonResponse = mapper.writeValueAsString(result);
				Response response = cf.createResponse(request.getId(), jsonResponse);
				return response;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Response response = cf.createResponse(request.getId(), e.getMessage());
				return response;
			}

				
		}

		
		try {
			ComponentRequestStatus status = new ComponentRequestStatusImpl.Builder().status(RequestStatus.REJECTED).message("unknown request").build();
			String payload = JsonUtils.toString(status);
			return cf.createResponse(request.getId(), payload);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return cf.createResponse(request.getId(), e.getMessage());
		}		
	}
}
