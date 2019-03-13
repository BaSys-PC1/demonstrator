package de.dfki.cos.basys.demonstrator.device.hybrit;

import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.cos.basys.platform.model.runtime.communication.Channel;
import de.dfki.cos.basys.platform.model.runtime.communication.Notification;
import de.dfki.cos.basys.platform.model.runtime.communication.Request;
import de.dfki.cos.basys.platform.model.runtime.communication.Response;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.Variable;
import de.dfki.cos.basys.platform.runtime.communication.CommFactory;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.DeviceStatus;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.TecsDeviceComponent;
import de.dfki.cos.hrc.hmi19.FrameType;
import de.dfki.cos.hrc.hmi19.RivetStateQA;
import de.dfki.cos.hrc.hmi19.RivetStateQAChangedEvent;
import de.dfki.cos.hrc.hololens.HoloLens;
import de.dfki.iui.hrc.hybritcommand.CommandResponse;
import de.dfki.iui.hrc.hybritcommand.CommandState;
import de.dfki.iui.hrc.hybritcommand.HumanTaskDTO;
import de.dfki.tecs.Event;
import de.dfki.tecs.ps.PSClient;
import de.dfki.tecs.ps.PSFactory;

public class HololensComponent extends TecsDeviceComponent {

	private HoloLensTECS client;
	private String clientID = "dummy";
	private final String businessKey;

	public HololensComponent(ComponentConfiguration config) {
		super(config);
		this.businessKey = "HMI19";
	}

	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		// Establish TECS connection for receiving status events
		String psUri = componentConfig.getProperty("ps-uri").getValue();

		// Establish connection to rpc server running on the HoloLens
		client = new HoloLensTECS(protocol, psUri, businessKey);
		clientID = client.psClient.getClientId();
	}

	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		LOGGER.debug("################### translateCapabilityRequest({})", req.toString());
		
		UnitConfiguration config = new UnitConfiguration();
		
		CapabilityVariant<?, ?> c = req.getCapabilityVariant();
		
		if(c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getChecking()))
		{
			// Get conrete instances of rivets to check
			List<Map<String,Object>> rivetPositions = getRivetPositions(req, "EMPTY"); // SQUEEZED
			
			// We assume that all rivet positions are with the same (logical) Stringer unit
			String frameIndex = rivetPositions.get(0).get("frameIndex").toString();
			// We assume that only horizontal stringers are processed
			String frameType = FrameType.H8x2.name();
			JsonArrayBuilder builder = Json.createArrayBuilder();
			for(int i=0; i<rivetPositions.size(); i++) {
				builder.add(rivetPositions.get(i).get("rivetIndex").toString());
			}
			JsonArray indices2Check = builder.build();		
			
			// Build description (json) of the checking task for HumanTaskDto	
			String desc = Json.createObjectBuilder()
					.add("description", "Check highlited rivots for quality and mark as iO or niO.")
					.add("frametype", frameType)
					.add("frameindex", frameIndex)	
					.add("rivetindeces", Json.createArrayBuilder(indices2Check))		
					.build().toString();
			
			LOGGER.debug("##################################" + desc);
						
			HumanTaskDTO task = new HumanTaskDTO();
			task.businessKey = this.businessKey;
			task.clientId = clientID;
			task.operationId = "QABroetjeDemoHMI19";
			task.description = desc;
			
			config.setPayload(task);
		}
		
		return config;
	}
	
	@Override
	public void busyWait(DeviceStatus device) {
		
		boolean executing = true;
		CommandState state = CommandState.EXECUTING;
		
		while(executing) {
            try {
            	state = device.getCommandState().state;
            } catch (TException ex) {
                LOGGER.warn("HoloLens agent not responding. Retrying ...");
                waitForMS(1500);
                reconnect();
                continue;
            }

			LOGGER.debug("CommandState is " + state);
            
			switch(state) {
			case EXECUTING:
				// Wait
				break;
			case ABORTED: 
				LOGGER.error("Task execution failed! Stopping ....");
				executing= false;
                setErrorCode(1);
				stop();
                break;
			case FINISHED: 
				executing= false;
				break;
			case PAUSED: 
				//?
				break;
			default:
				LOGGER.warn("Received unexpected CommandState {} during task execution!", state);
				break;
			}
            waitForMS(1500);
            
        } 		
		
	}
	
	private List<Map<String,Object>> getRivetPositions(CapabilityRequest req, String state) {
		LOGGER.debug("################### getRivetPositions({},{})", req.toString(), state); 

		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		
		int count = 0;
		String sector = null;
		
		Variable[] vars = req.getInputParameters().toArray(new Variable[0]);
		
		for (Variable v : vars) {
			if ("count".equalsIgnoreCase(v.getName())) {
				count = Integer.parseInt(v.getValue());
			}
			else if ("sector".equalsIgnoreCase(v.getName())) {
				sector = v.getValue();
			}
		}
		
		JsonObject jsonRequest = Json.createObjectBuilder()
				.add("action", "getRivetPositions")
				.add("count", count)
				.add("sector", sector)
				.add("state", state)
				.build();
	
		Request wmReq = CommFactory.getInstance().createRequest(jsonRequest.toString());						
		Channel wmInChannel = CommFactory.getInstance().openChannel(context.getSharedChannelPool(), "world-model#in", false, null);
		Response wmResp = wmInChannel.sendRequest(wmReq);
		
		JsonReader jsonReader = Json.createReader(new StringReader(wmResp.getPayload()));
		JsonArray jsonArray = jsonReader.readArray();
		List<JsonObject> rivetPositions = jsonArray.getValuesAs(JsonObject.class);
					
		for (JsonObject rivetPosition : rivetPositions) {
			Map<String, Object> p = new HashMap<>();
			//p.put("id", rivetPosition.getString("id"));
			p.put("frameIndex", rivetPosition.getInt("frameIndex"));
			p.put("rivetIndex", rivetPosition.getInt("index"));
			p.put("state", rivetPosition.getString("state"));
			result.add(p);
		}		
				
		return result;
	}
	
	@Override
	public void onResetting() {
		super.onResetting();
		client.reset();
	}
	
	private void waitForMS(long millis) {
		try {
			Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

	@Override
	public void onStarting() {
		Object payload = getUnitConfig().getPayload();
		if (payload instanceof HumanTaskDTO) {
			HumanTaskDTO task = (HumanTaskDTO) payload;
			boolean informed = false;
			CommandState state = CommandState.ABORTED;
			while(!informed)
			{
				try {					
					state = client.requestTaskExecution(task);
					if(state == CommandState.ACCEPTED) {
	                    informed = true;
					}
					else {
						LOGGER.warn("HoloLens agent currently busy. Retrying ...");
						waitForMS(1500);
					}
				} catch (TException e) {
					LOGGER.warn("HoloLens agent not responding. Retrying ..");
					waitForMS(1500);
					reconnect();
				}
			}
		} 
		else {
			setErrorCode(99);
			stop();
		}
	}

	@Override
	public void onExecute() {
		this.busyWait(client);
	}

	public class HoloLensTECS extends HoloLens.Client implements DeviceStatus {
		private PSClient psClient;
		private Thread psThread;
		private CommandState cmdState;
		private String businessKey;

		public HoloLensTECS(TProtocol prot, String psUri, String businessKey) {
			super(prot);
			this.businessKey = businessKey;

			init(psUri);
		}

		// UGLY! Find common sense in libHRC!
		private String translateRivetState(RivetStateQA state) {
			String result = null;
			if(state != null) {
				switch(state) {
				case IO:
					result = "CHECKED_IO";
					break;				
				case NIO:
					result = "CHECKED_NIO";
					break;
				case UNCHECKED:
					result = "UNCHECKED";
					break;
				default:
					// Ignore	
				}
			}
			return result;
		}
		
		private void init(String psUri) {
			cmdState = CommandState.READY;

			// Configure PS client
			psClient = PSFactory.createPSClient(URI.create(psUri));
			psClient.subscribe("RivetStateQAChangedEvent");
			psClient.connect();

			// Start listening to events on separate thread
			psThread = new Thread(() -> {
				while (true) {
					while (psClient.canRecv()) {
						Event event = psClient.recv();
						LOGGER.debug("Received event of type {} on channel {} from {}.", event.getEtype(), event.getChannel(), event.getSource());
						if (event.is("RivetStateQAChangedEvent")) {
							RivetStateQAChangedEvent rsce = new RivetStateQAChangedEvent();
							event.parseData(rsce);

							// **********************************************
							// TODO report changed rivet state to world model
							// **********************************************
							JsonObject jsonNotification = Json.createObjectBuilder()
									.add("action", "updateRivetPosition")
									.add("frameIndex", Integer.parseInt(rsce.frameIndex))
									.add("rivetIndex", Integer.parseInt(rsce.rivetIndex))
									.add("state", translateRivetState(rsce.state))
									.build();
				
							Notification wmNoti = CommFactory.getInstance().createNotification(jsonNotification.toString());						
							Channel wmInChannel = CommFactory.getInstance().openChannel(context.getSharedChannelPool(), "world-model#in", false, null);
							wmInChannel.sendNotification(wmNoti);

						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {					
						e.printStackTrace();
					}
				}
			});

			psThread.start();
		}
		
		public void reset() {
			cmdState = CommandState.READY;
		}
		

		@Override
		public CommandState showNotification(String notification, String businessKey) throws TException {
			CommandState state = super.showNotification(notification, businessKey);
			cmdState = state;
			return cmdState;
		}

		@Override
		public CommandState requestTaskExecution(HumanTaskDTO task) throws TException {
			CommandState state = super.requestTaskExecution(task);
			cmdState = state;
			return cmdState;
		}

		@Override
		public CommandResponse getCommandState() throws TException {
			CommandResponse cmdRsp = super.getCommandState(businessKey);
			cmdState = cmdRsp.state;
			return cmdRsp;
		}

	}

}
