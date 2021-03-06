package de.dfki.cos.basys.demonstrator.device.laser;

public enum EProjectionColor {

	RED(0x0FFF0000), GREEN(0x0F00FF00), YELLOW(0x0FFFFF00);

	private final int mColor;

	private EProjectionColor(int color) {

		mColor = color;

	}

	public static EProjectionColor getColorByIndex(int index) {
		switch (index) {
		case 1:
			return RED;
		case 2:
			return YELLOW;
		default:
			return GREEN;
		}
	}

	public int getColor() {
		return mColor;
	}

}
