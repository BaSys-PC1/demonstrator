package de.dfki.cos.basys.demonstrator.device.button;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.dfki.cos.basys.common.emf.json.JsonUtils;
import de.dfki.cos.basys.demonstrator.device.button.ButtonDeviceComponent;
import de.dfki.cos.basys.platform.model.domain.capability.CapabilityFactory;
import de.dfki.cos.basys.platform.model.domain.capability.SwitchConfirmationCapability;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.GeneralCapabilityVariant;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.ResourceinstanceFactory;
import de.dfki.cos.basys.platform.model.runtime.communication.Request;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentCategory;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentFactory;
import de.dfki.cos.basys.platform.model.runtime.component.State;
import de.dfki.cos.basys.platform.model.runtime.component.impl.ComponentConfigurationImpl;
import de.dfki.cos.basys.platform.runtime.communication.CommFactory;
import de.dfki.cos.basys.platform.runtime.communication.provider.MqttCommunicationProvider;
import de.dfki.cos.basys.platform.runtime.component.ComponentContext;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;

public class SwitchDeviceComponentTest {

	ComponentConfiguration componentConfig;
	ComponentContext emptyContext = new ComponentContext.Builder().build();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		String basysBroker = MqttCommunicationProvider.defaultConnectionString;
		String localBroker = "tcp://127.0.0.1:1883";
		componentConfig = new ComponentConfigurationImpl.Builder()
				.componentId("switch-component")
				.componentName("switch-component")
				.inChannelName("switch-component#in")
				.outChannelName("switch-component#out")
				.componentCategory(ComponentCategory.DEVICE_COMPONENT)
				.externalConnectionString(basysBroker).build();
	}

	@After
	public void tearDown() throws Exception {
	}

	private void sleep(long seconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(seconds*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testConnection() throws ComponentException {
		
		ButtonDeviceComponent component = new ButtonDeviceComponent(componentConfig);
		assertTrue(!component.isConnectedToExternal());

		component.activate(emptyContext);
		assertTrue(component.isConnectedToExternal());
		
		component.deactivate();		
		assertTrue(!component.isConnectedToExternal());
	}
	
	@Test
	@Ignore
	public void testActivateStop() throws ComponentException {
		ButtonDeviceComponent component = new ButtonDeviceComponent(componentConfig);
		assertTrue(!component.isConnectedToExternal());

		component.activate(emptyContext);
		assertTrue(component.isConnectedToExternal());
		
		
		component.reset();
		sleep(1);
		
		SwitchConfirmationCapability capability = CapabilityFactory.eINSTANCE.createSwitchConfirmationCapability();
		capability.setState(1);
		
		GeneralCapabilityVariant variant = ResourceinstanceFactory.eINSTANCE.createGeneralCapabilityVariant();		
		variant.setCapability(capability);
		
		CapabilityRequest req = ComponentFactory.eINSTANCE.createCapabilityRequest();
		req.setCapabilityVariant(variant);
		req.setComponentId(componentConfig.getComponentId());
		
		try {
			String payload = JsonUtils.toString(req);
			Request message = CommFactory.getInstance().createRequest(payload);
			component.handleRequest(null, message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		sleep(3);
				
		assertEquals(State.EXECUTE, component.getState());
		
		component.stop();
		
		sleep(3);
		
		assertEquals(State.IDLE, component.getState());
		
		component.deactivate();		
		assertTrue(!component.isConnectedToExternal());
	}
	
	@Test
	@Ignore
	public void testActivateComplete() throws ComponentException {
		ButtonDeviceComponent component = new ButtonDeviceComponent(componentConfig);
		assertTrue(!component.isConnectedToExternal());

		component.activate(emptyContext);
		assertTrue(component.isConnectedToExternal());
		
		
		component.reset();
		sleep(1);
		
		SwitchConfirmationCapability capability = CapabilityFactory.eINSTANCE.createSwitchConfirmationCapability();
		capability.setState(1);
		
		GeneralCapabilityVariant variant = ResourceinstanceFactory.eINSTANCE.createGeneralCapabilityVariant();		
		variant.setCapability(capability);
		
		CapabilityRequest req = ComponentFactory.eINSTANCE.createCapabilityRequest();
		req.setCapabilityVariant(variant);
		req.setComponentId(componentConfig.getComponentId());
		
		try {
			String payload = JsonUtils.toString(req);
			Request message = CommFactory.getInstance().createRequest(payload);
			component.handleRequest(null, message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		sleep(3);
				
		assertEquals(State.EXECUTE, component.getState());
		
		sleep(10);
		
		assertEquals(State.IDLE, component.getState());
		
		component.deactivate();		
		assertTrue(!component.isConnectedToExternal());
	}
	
}
