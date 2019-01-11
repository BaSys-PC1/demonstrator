package de.dfki.cos.basys.demonstrator.device.button;

import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.runtime.component.ComponentContext;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.mqtt.MqttDeviceComponent;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;

public class ButtonDeviceComponent extends MqttDeviceComponent {

	private final Integer qos = 0;
	private final String topic = "switch";
	
	private boolean switchIsActive = false;
	
	private CountDownLatch counter;
	
	public ButtonDeviceComponent(ComponentConfiguration config) {
		super(config);
		resetOnComplete = true;
		resetOnStopped = true;
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {

		//CapabilityVariant<?> variant = (CapabilityVariant<?>)req.getCapabilityVariant();
		// c = variant.getCapability();

		//if (c.eClass().equals(CapabilityPackage.eINSTANCE.getSwitchConfirmationCapability()))
		{
			//return new UnitConfiguration();
		}
		return new UnitConfiguration();
		//throw new ComponentException("unknown capability");
		//return null;
		

	}
	
	@Override
	public void activate(ComponentContext context) throws ComponentException {
		super.activate(context);
		
		if (isConnectedToExternal()) {
			selfCheck();
		}
		
	}

	private void selfCheck() {
		LOGGER.info("start self-check...");

		switchDeactivate();
		sleep(1);
		switchActivate();
		sleep(1);
		switchDeactivate();
		
		LOGGER.info("self-check done");		
	}
	
	@Override
	public void onStarting() {
		
		counter = new CountDownLatch(1);
		
		switchActivate();

	}
	
	@Override
	public void onExecute() {
		
			try {
				counter.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			LOGGER.info("EXECUTE FINISHED");
		
	}

	@Override
	public void onCompleting() {
		switchDeactivate();
		super.onCompleting();
	}
	
	@Override
	public void onStopping() {
		selfCheck();
		super.onStopping();
	}
	
	@Override
	public void onResetting() {
		if (switchIsActive)
			switchDeactivate();
	}
	
	private boolean switchActivate() {

		try {
			mqttClient.subscribe(topic, qos, new IMqttMessageListener() {

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {

					String sMessage = new String(message.getPayload());
					if (sMessage.contains("Switch") && sMessage.contains("Event")) {
						counter.countDown();
					}

				}
			}).waitForCompletion();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
		publish(topic, "01");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		publish(topic, "activate");
		switchIsActive = true;
		return true;
	}
	
	private boolean switchDeactivate() {
	
		publish(topic, "10");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		publish(topic, "deactivate");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		publish(topic, "00");
		
		try {
			mqttClient.unsubscribe(topic).waitForCompletion();
			LOGGER.info("UNSUB");
		} catch (MqttException e) {
			e.printStackTrace();
		}
		switchIsActive = false;
		return true;
	}
	
	private void publish(String topic, String content) {
		final MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        try {
        	mqttClient.publish(topic, message).waitForCompletion();
        	LOGGER.debug("publishing message %s on topic %s%n", message, topic);
        } catch (MqttException e) {
        	e.printStackTrace();
        }
	}
	
}
