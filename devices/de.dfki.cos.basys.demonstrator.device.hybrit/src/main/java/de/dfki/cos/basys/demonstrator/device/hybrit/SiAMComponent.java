package de.dfki.cos.basys.demonstrator.device.hybrit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.json.JSONObject;

import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.capability.InteractionCapability;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.GeneralCapabilityVariant;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.ResourceinstancePackage;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.model.runtime.component.Variable;
import de.dfki.cos.basys.platform.model.runtime.component.VariableType;
import de.dfki.cos.basys.platform.model.runtime.component.impl.VariableImpl;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.DeviceStatus;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.TecsDeviceComponent;
import de.dfki.iui.hrc.hybritcommand.CommandResponse;
import de.dfki.iui.hrc.siam.CeBITDialogue;

public class SiAMComponent extends TecsDeviceComponent {

	private SiAMTECS client;
	private final String businessKey;

	public SiAMComponent(ComponentConfiguration config) {
		super(config);
		this.businessKey = UUID.randomUUID().toString();
		resetOnComplete = true;
		resetOnStopped = true;
	}
	
	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		client = new SiAMTECS(protocol, businessKey);
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		UnitConfiguration config = new UnitConfiguration();

		if (req.getCapabilityVariant().eClass().equals(ResourceinstancePackage.eINSTANCE.getGeneralCapabilityVariant())) {
			GeneralCapabilityVariant variant = (GeneralCapabilityVariant) req.getCapabilityVariant();
			if (variant.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getInteractionCapability())) {
				InteractionCapability capability = (InteractionCapability) variant.getCapability();
				config.setPayload(capability.getTopic());
			}
		}

		return config;
	}

	@Override
	public void onStarting() {
		String topic = (String) getUnitConfig().getPayload();
		LOGGER.info("Performing dialogue " + topic);
		try {
			if ("intention".equals(topic)) {
				client.performIntentionDialog();
			} else if ("cola".equals(topic)) {
				client.performColaDialog();
			} else if ("delivery".equals(topic)) {
				client.performDeliveryDialog();
			} else {
				setErrorCode(1);
				stop();
			}
		} catch (TException e) {
			e.printStackTrace();
			setErrorCode(3);
			stop();
		}
	}

	@Override
	public void onExecute() {
		busyWait(client);
	}

	@Override
	public void onCompleting() {
		try {
			CommandResponse cr = client.getCommandState();
			LOGGER.info("Dialogue result was " + cr.getDescription());

			if (cr.getDescription().startsWith("{")) {
				JSONObject ob = new JSONObject(cr.getDescription());
				
				String[] props = new String[] { "colaRequest", "emptyBottle", "milkrunRequest", "qaRequest" };
	
				List<Variable> variables = new ArrayList<>(4);
				for (String prop : props) {
					Variable var = new VariableImpl.Builder()
							.name(prop).type(VariableType.BOOLEAN).value("false")
							.build();
					if (ob.has(prop)) {
						String value = ob.getString(prop);
						var.setValue(value);
					}
					variables.add(var);
				}
	
				handleCapabilityResponse(ResponseStatus.OK, 0, variables);
			} else {
				handleCapabilityResponse(ResponseStatus.OK, 0);
			}

		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private class SiAMTECS extends CeBITDialogue.Client implements DeviceStatus {
		private final String businessKey;

		public SiAMTECS(TProtocol prot, String businessKey) {
			super(prot);
			this.businessKey = businessKey;
		}

		public CommandResponse performColaDialog() throws TException {
			return super.performColaDialog(businessKey);
		}

		public CommandResponse performIntentionDialog() throws TException {
			return super.performIntentionDialog(businessKey);
		}

		public CommandResponse performDeliveryDialog() throws TException {
			return super.performDeliveryDialog(businessKey);
		}

		@Override
		public CommandResponse getCommandState() throws TException {
			return super.getCommandState(businessKey);
		}

	}

}
