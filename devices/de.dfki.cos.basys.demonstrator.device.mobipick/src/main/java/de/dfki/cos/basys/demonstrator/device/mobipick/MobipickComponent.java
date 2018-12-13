package de.dfki.cos.basys.demonstrator.device.mobipick;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.runtime.component.ComponentContext;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.ros.RosDeviceComponent;
import edu.wpi.rail.jrosbridge.ActionClient;
import edu.wpi.rail.jrosbridge.Goal;
import edu.wpi.rail.jrosbridge.callback.ActionCallback;
import edu.wpi.rail.jrosbridge.messages.actionlib.GoalStatus;

public class MobipickComponent extends RosDeviceComponent {

	private ActionClient client;
	private Goal goal;
	
	public MobipickComponent(ComponentConfiguration config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void activate(ComponentContext context) throws ComponentException {	
		super.activate(context);
	}
	
	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		
		client = new ActionClient(ros, "/mobipick/actor/plan_and_execute", "pbr_msgs/PlanAndExecuteAction");
		client.initialize();
	}
	
	@Override
	public void deactivate() throws ComponentException {	
		super.deactivate();
		
		client.dispose();
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		// TODO Auto-generated method stub
		
		UnitConfiguration config = new UnitConfiguration();
		
		CapabilityVariant<?, ?> c = req.getCapabilityVariant();
		
		// TODO insert correct args
		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getTransport())) {
			String goalString = "{\r\n" + 
					"	\"tasks\" : [\r\n" + 
					"		{\r\n" + 
					"			\"id\" : \"task0\",\r\n" + 
					"			\"type\" : \"bring\" ,\r\n" + 
					"			\"args\" : [\"Powerdrill\", \"sempr:LocalCS_78\"]\r\n" + 
					"		}\r\n" + 
					"	],\r\n" + 
					"	\"constraints\" : []\r\n" + 
					"}"; 
		
//			String goalString = "{\r\n" + 
//					"	\"tasks\" : [\r\n" + 
//					"		{\r\n" + 
//					"			\"id\" : \"task0\",\r\n" + 
//					"			\"type\" : \"!move_arm\" ,\r\n" + 
//					"			\"args\" : [\"ur5\", \"observe100cm_right\", \"false\"]\r\n" + 
//					"		}\r\n" + 
//					"	],\r\n" + 
//					"	\"constraints\" : []\r\n" + 
//					"}"; 		
			
//			String goalString = "{\r\n" + 
//					"	\"tasks\" : [\r\n" + 
//					"		{\r\n" + 
//					"			\"id\" : \"task0\",\r\n" + 
//					"			\"type\" : \"!recognize_objects\" ,\r\n" + 
//					"			\"args\" : [\"sempr:SpatialObject_53\"]\r\n" + 
//					"		}\r\n" + 
//					"	],\r\n" + 
//					"	\"constraints\" : []\r\n" + 
//					"}"; 
			
			JsonReader jsonReader = Json.createReader(new StringReader(goalString));
			JsonObject goalObject = jsonReader.readObject();
			jsonReader.close();
			
			config.setPayload(goalObject);
		}

		return config;
	}	

	
	@Override
	public void onStarting() {
		
		// see http://wiki.ros.org/actionlib/DetailedDescription, Chapter 3.5.2
		goal = client.createGoal(new ActionCallback() {
			
			@Override
			public void handleStatus(GoalStatus status) {
				System.out.println("STATUS: " + status.toString());	
				switch (status.getStatus()) {
				case GoalStatus.ABORTED:
					setErrorCode(4);
					break;
				case GoalStatus.ACTIVE:
					break;
				case GoalStatus.LOST:
					setErrorCode(9);
					break;
				case GoalStatus.PENDING:
					break;
				case GoalStatus.PREEMPTED:
					setErrorCode(2);
					break;
				case GoalStatus.PREEMPTING:
					break;
				case GoalStatus.RECALLED:
					setErrorCode(8);
					break;
				case GoalStatus.RECALLING:
					break;
				case GoalStatus.REJECTED:
					setErrorCode(5);
					break;
				case GoalStatus.SUCCEEDED:
					break;
				default:
					break;
				}
				
			}
			
			@Override
			public void handleResult(JsonObject result) {
				System.out.println("RESULT: " + result.toString());
				if (result.toString().contains("false")) {
					setErrorCode(1);
				}
				
				signalExecuteComplete();
			}
			
			@Override
			public void handleFeedback(JsonObject feedback) {
				System.out.println("FEEDBK: " + feedback.toString());				
			}
		});
		
		JsonObject goalObject = (JsonObject) getUnitConfig().getPayload();		
		goal.submit(goalObject);
				
	}
	
	@Override
	public void onExecute() {
		awaitExecuteComplete();
		if (getErrorCode() > 0) {
			stop();
		}
	}

	@Override
	public void onCompleting() {
		sendComponentResponse(ResponseStatus.OK, 0);
	}

	@Override
	public void onStopping() {
		//TODO: handle cancellation from user
		sendComponentResponse(ResponseStatus.NOT_OK, getErrorCode());
	}

	

	
}
