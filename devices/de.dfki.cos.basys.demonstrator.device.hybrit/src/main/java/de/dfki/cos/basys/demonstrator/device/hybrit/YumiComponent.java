package de.dfki.cos.basys.demonstrator.device.hybrit;

import java.util.UUID;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;

import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.DeviceStatus;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.TecsDeviceComponent;
import de.dfki.iui.basys.model.domain.capability.CapabilityPackage;
import de.dfki.iui.basys.model.domain.resourceinstance.ManufacturingCapabilityVariant;
import de.dfki.iui.basys.model.domain.resourceinstance.ResourceinstancePackage;
import de.dfki.iui.basys.model.runtime.component.CapabilityRequest;
import de.dfki.iui.basys.model.runtime.component.ComponentConfiguration;
import de.dfki.iui.basys.model.runtime.component.ResponseStatus;
import de.dfki.iui.basys.model.runtime.component.Variable;
import de.dfki.iui.basys.model.runtime.component.VariableType;
import de.dfki.iui.basys.model.runtime.component.impl.VariableImpl;
import de.dfki.iui.hrc.hybritcommand.CommandResponse;
import de.dfki.iui.hrc.yumi.PickException;
import de.dfki.iui.hrc.yumi.QAException;
import de.dfki.iui.hrc.yumi.Yumi;
import de.dfki.iui.hrc.yumi.YumiState;

public class YumiComponent extends TecsDeviceComponent {

	protected YumiTECS client;
	private final String businessKey;

	public YumiComponent(ComponentConfiguration config) {
		super(config);
		this.businessKey = UUID.randomUUID().toString();
	}

	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		client = new YumiTECS(protocol, businessKey);
	}

	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		UnitConfiguration config = new UnitConfiguration();

		if (req.getCapabilityVariant().eClass()
				.equals(ResourceinstancePackage.eINSTANCE.getManufacturingCapabilityVariant())) {
			ManufacturingCapabilityVariant variant = (ManufacturingCapabilityVariant) req.getCapabilityVariant();
			if (variant.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getInspect())) {
				if (variant.getAppliedOn().size() == 1) {
					config.setPayload("PERFORM QA");
				}
			}
		}
		return config;
	}

	@Override
	public void onStarting() {
		if (((String) getUnitConfig().getPayload()).equals("PERFORM QA")) {
			try {			
				client.performQA("PERFORM QA");
			} catch (TException e) {
				e.printStackTrace();
				if (e instanceof TTransportException ) {
					LOGGER.warn("Trying to reconnect");
					reconnect();
					try {
						client.performQA("PERFORM QA");
					} catch (TException e1) {
						e1.printStackTrace();
						setErrorCode(1);
						stop();
					}
				}
			}
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
			LOGGER.info("QA result was " + cr.getDescription());
			Variable qaResult = new VariableImpl.Builder()
					.name("qaResult")
					.type(VariableType.BOOLEAN)
					.value(cr.getDescription().equals("io") ? "true" : "false")
					.build();
			
			sendComponentResponse(ResponseStatus.OK, 0, qaResult);
			//sendComponentResponse(ResponseStatus.OK, 0);
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onStopping() {
		sendComponentResponse(ResponseStatus.NOT_OK, getErrorCode());
	}

	
	private class YumiTECS extends Yumi.Client implements DeviceStatus {

		private TProtocol protocol;
		private final String businessKey;

		public YumiTECS(TProtocol prot, String businessKey) {
			super(prot);
			protocol = prot;
			this.businessKey = businessKey;
		}

		@Override
		public CommandResponse getCommandState() throws TException {
			return super.getCommandState(businessKey);
		}

		@Override
		public YumiState getState() throws TException {
			return super.getState();
		}

		public CommandResponse performPick(String objectId, String sourceLocation) throws PickException, TException {
			return super.performPick(objectId, sourceLocation, businessKey);
		}

		public CommandResponse performPut(String objectId, String sourceLocation) throws PickException, TException {
			return super.performPut(objectId, sourceLocation, businessKey);
		}

		public CommandResponse performQA(String objectId) throws QAException, TException {
			return super.performQA(objectId, businessKey);
		}
	}

}
