package de.dfki.cos.basys.demonstrator.device.laser.action;

import java.util.ArrayList;
import java.util.List;

import de.dfki.cos.basys.demonstrator.device.laser.projectionentities.ProjectionEntity;

public class Projection extends LaserAction {

	public List<ProjectionEntity> data = new ArrayList<>();

	public Projection() {

		action = "projection";
	}

	public void addEntity(ProjectionEntity entity) {
		data.add(entity);
	}

}
