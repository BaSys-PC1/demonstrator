package de.dfki.iui.basys.demonstrator.device.laser.projectionentities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.dfki.iui.basys.demonstrator.device.laser.EProjectionColor;
import de.dfki.iui.basys.model.data.CartesianCoordinate;

public class PEMovingArrows extends ProjectionEntity {

	@JsonProperty
	private List<CartesianCoordinate> points = new ArrayList<>();

	@JsonProperty
	private int arrowCount;

	@JsonProperty
	private long delay;

	public PEMovingArrows(List<CartesianCoordinate> points, int arrowCount, long delay, EProjectionColor color) {
		super(0, 0, 0, color);
		type = "movingArrows";
		this.points.addAll(points);
		this.arrowCount = arrowCount;
		this.delay = delay;

	}

}
