package de.dfki.cos.basys.platform.osgi.services.ext.internal;

import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import de.dfki.cos.basys.demonstrator.service.worldmodel.WorldModelService;
import de.dfki.cos.basys.demonstrator.service.worldmodel.WorldModelServiceImpl;
import de.dfki.cos.basys.platform.model.domain.linebalancing.StaticWorldModel;
import de.dfki.cos.basys.platform.model.domain.linebalancing.WorldModel;
import de.dfki.cos.basys.platform.model.domain.topology.TopologyElement;
import de.dfki.cos.basys.platform.osgi.services.BasysOsgiComponent;
import de.dfki.cos.basys.platform.osgi.services.ResourceSetProvider;
import de.dfki.cos.basys.platform.runtime.component.manager.ComponentManager;
import de.dfki.cos.basys.platform.runtime.component.manager.ComponentManagerException;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true)
public final class WorldModelServiceComponent extends BasysOsgiComponent implements WorldModelService {

	ComponentManager componentManager;
	WorldModelServiceImpl impl;

	public WorldModelServiceComponent() {
		LOGGER.info("WorldModelServiceComponent created");
	}

	@Override
	@Activate
	public void activate(ComponentContext context, Map<String, Object> properties) {
		super.activate(context, properties);

		impl = new WorldModelServiceImpl(config);
		//impl.setSharedResourceSet(provider.getSharedResourceSet());
		try {
			componentManager.addLocalComponent(impl);
		} catch (ComponentManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	@Modified
	public void modified(ComponentContext context, Map<String, Object> properties) {
		if (super.context == null)
			return;

		super.modified(context, properties);
	}

	@Override
	@Deactivate
	public void deactivate(ComponentContext context, int reason) {
		super.deactivate(context, reason);
	}

	@Reference
	void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}

	void unsetComponentManager(ComponentManager componentManager) {
		this.componentManager = null;
	}

	ResourceSetProvider provider;
	
	@Reference
	void setResourceSetProvider(ResourceSetProvider provider) {
		this.provider = provider;
	}
	
	void unsetResourceSetProvider(ResourceSetProvider provider) {
		this.provider = null;
	}
	
	/*
	 * WorldmodelManager interface
	 */

	@Override
	public String getHull() {
		return impl.getHull();
	}

}
