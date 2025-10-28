/**
 * VariableInductorElm by Ricky Genz
 *
 * An inductor with an inductance value changeable with circuitry.
 * This component will be the same as the Inductor component,
 * but with an added input so the value of the inductance can be
 * changed by an external source (modulation). Changing the
 * modulation changes the inductance: an inductor with 10 H when
 * modulation is 0 V and then 100 mH when modulation is 10 V.
 */

package com.lushprojects.circuitjs1.client;

public class VariableInductorElm extends InductorElm {

	/**
	 * inductanceLow    Inductance when modulation = voltageLow (e.g., 10 H)
	 * inductanceHigh   Inductance when modulation = voltageHigh (e.g., 100 mH)
	 * voltageLow       Voltage which produces inductanceLow (e.g., 0 V)
	 * voltageHigh      Voltage which produces inductanceHigh (e.g., 10 V)
	 * clampVoltage     Clamp the modulation voltage to [voltageLow, voltageHigh]
	 */
	double inductanceLow = 10; /* 1 H */
	double inductanceHigh = 0.1; /* 100 mH */
	double voltageLow = 0.0; /* 0 V */
	double voltageHigh = 10.0; /* 10 V */
	boolean clampVoltage = true;

	public VariableInductorElm(int xx, int yy) {
		super(xx, yy);
		noDiagonal = true;
	}
	public VariableInductorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		inductanceLow = new Double(st.nextToken()).doubleValue();
		inductanceHigh = new Double(st.nextToken()).doubleValue();
		voltageLow = new Double(st.nextToken()).doubleValue();
		voltageHigh = new Double(st.nextToken()).doubleValue();
		clampVoltage = new Boolean(st.nextToken()).booleanValue();
		noDiagonal = true;
	}

	int getDumpType() { return 301; }
	String dump() {
		return super.dump() + " " + inductanceLow + " " + inductanceHigh + " " + voltageLow + " " + voltageHigh + " " + clampVoltage;
	}

	Point point3, lead3, arrow1, arrow2;
	void setPoints() {
		super.setPoints();

		/* ensure the modulation voltage input is snapped to the grid */
		adjustLeadsToGrid(false, false);
		point3 = interpPoint(lead1, lead2, 0.5, 0);
		lead3 = interpPoint(point1, point2, 0.35, 12);

		/* calc arrow */
		arrow1 = interpPoint(point1, point2, 0.38, 4);
		arrow2 = interpPoint(point1, point2, 0.40, 12);
	}

	void draw(Graphics g) {
		super.draw(g);

		setVoltageColor(g, volts[2]);
		drawThickLine(g, point3, lead3);

		/* draw arrow */
		g.context.setLineWidth(3.0);
		g.context.beginPath();
		g.context.moveTo(lead3.x, lead3.y);
		g.context.lineTo(arrow1.x, arrow1.y);
		g.context.moveTo(lead3.x, lead3.y);
		g.context.lineTo(arrow2.x, arrow2.y);
		g.context.stroke();
	}

	int getPostCount() { return 3; }
	Point getPost(int n) {
		return (n == 0) ? point1 : (n == 1) ? point2 : point3;
	}

	/**
	 * Interpolate the inductance based on a modulation voltage.
	 *
	 * @param modulationVoltage   The current modulation voltage (V)
	 * @return                    Interpolated inductance (H)
	 */
	public double interpInductance(double modulationVoltage) {

		if (clampVoltage) {
			/* clamp the modulation voltage to the valid range [voltageLow, voltageHigh] */
			if (modulationVoltage < voltageLow) {
				modulationVoltage = voltageLow;
			} else if (modulationVoltage > voltageHigh) {
				modulationVoltage = voltageHigh;
			}
		} else {
			modulationVoltage = Math.abs(modulationVoltage);
		}

		/* linear interpolation: L(v) = Llow + ((v - Vlow) * (Lhigh - Llow) / (Vhigh - Vlow)) */
		return inductanceLow + ((modulationVoltage - voltageLow) * (inductanceHigh - inductanceLow) / (voltageHigh - voltageLow));
	}

	void startIteration() {
		/* calc starting energy */
		double e = 0.5 * super.getInductance() * Math.pow(volts[2], 2);

		/* modify inductance */
		super.setInductance(interpInductance(volts[2]));

		/* preserve energy continuity */
		double v = Math.sqrt(2 * e / super.getInductance());
		volts[2] = v;

		super.startIteration();
	}

	void getInfo(String arr[]) {
		super.getInfo(arr);
		arr[0] = "Inductor (variable)";
		arr[5] = "Vmod = " + getUnitText(volts[2], "V");
	}
	public EditInfo getEditInfo(int n) {
		if (n == 3)
			return new EditInfo("Inductance (H) at Low Voltage", inductanceLow, 1e-2, 10);
		if (n == 4)
			return new EditInfo("Inductance (H) at High Voltage", inductanceHigh, 1e-2, 10);
		if (n == 5)
			return new EditInfo("Low Modulation Voltage (V)", voltageLow, -1000, 1000);
		if (n == 6)
			return new EditInfo("High Modulation Voltage (V)", voltageHigh, -1000, 1000);
		if (n == 7)
			return EditInfo.createCheckbox("Clamp Modulation Voltage", clampVoltage);
		return super.getEditInfo(n);
	}
	public void setEditValue(int n, EditInfo ei) {
		if (n == 3)
			inductanceLow = (ei.value > 0) ? (ei.value > 10) ? 10 : ei.value : 1e-2;
		if (n == 4)
			inductanceHigh = (ei.value > 0) ? (ei.value > 10) ? 10 : ei.value : 1e-2;
		if (n == 5)
			voltageLow = (ei.value > 1000) ? 1000 : (ei.value < -1000) ? -1000 : ei.value;
		if (n == 6)
			voltageHigh = (ei.value > 1000) ? 1000 : (ei.value < -1000) ? -1000 : ei.value;
		if (n == 7)
			clampVoltage = ei.checkbox.getState();
		super.setEditValue(n, ei);
	}
	int getShortcut() { return 0; }

	@Override
	public void setInductance(double l) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Inductance may not be manually set for a Variable Inductor as inductance is set dynamically via the modulation voltage input.");
	}
}
