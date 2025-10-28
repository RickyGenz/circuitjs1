/**
 * VariableCapacitorElm by Ricky Genz
 *
 * A capacitor with a capacitance value changeable with circuitry.
 * This component will be the same as the Capacitor component,
 * but with an added input so the value of the capacitance can be
 * changed by an external source (modulation). Changing the
 * modulation changes the capacitance: a capacitor with 100 uF when
 * modulation is 0 V and then 1 uF when modulation is 10 V.
 */

package com.lushprojects.circuitjs1.client;

public class VariableCapacitorElm extends CapacitorElm {

	/**
	 * capacitanceLow    Capacitance when modulation = voltageLow (e.g., 100 µF)
	 * capacitanceHigh   Capacitance when modulation = voltageHigh (e.g., 1 µF)
	 * voltageLow        Voltage which produces capacitanceLow (e.g., 0 V)
	 * voltageHigh       Voltage which produces capacitanceHigh (e.g., 10 V)
	 * clampVoltage      Clamp the modulation voltage to [voltageLow, voltageHigh]
	 */
	double capacitanceLow = 100e-6; /* 100 µF */
	double capacitanceHigh = 1e-6; /* 1 µF */
	double voltageLow = 0.0; /* 0 V */
	double voltageHigh = 10.0; /* 10 V */
	boolean clampVoltage = true;

	public VariableCapacitorElm(int xx, int yy) {
		super(xx, yy);
		noDiagonal = true;
	}
	public VariableCapacitorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		capacitanceLow = new Double(st.nextToken()).doubleValue();
		capacitanceHigh = new Double(st.nextToken()).doubleValue();
		voltageLow = new Double(st.nextToken()).doubleValue();
		voltageHigh = new Double(st.nextToken()).doubleValue();
		clampVoltage = new Boolean(st.nextToken()).booleanValue();
		noDiagonal = true;
	}

	int getDumpType() { return 300; }
	String dump() {
		return super.dump() + " " + capacitanceLow + " " + capacitanceHigh + " " + voltageLow + " " + voltageHigh + " " + clampVoltage;
	}

	Point point3, lead3, arrow1, arrow2;
	void setPoints() {
		super.setPoints();

		/* ensure the modulation voltage input is snapped to the grid */
		adjustLeadsToGrid(false, false);
		point3 = interpPoint(lead1, lead2, 0.5, 0);
		lead3 = interpPoint(point1, point2, 0.35, 12);

		/* restore the original CapacitorElm leads */
		double f = (dn/2-4)/dn;
		lead1 = interpPoint(point1, point2, f);
		lead2 = interpPoint(point1, point2, 1-f);

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
	 * Interpolate the capacitance based on a modulation voltage.
	 *
	 * @param modulationVoltage   The current modulation voltage (V)
	 * @return                    Interpolated capacitance (F)
	 */
	public double interpCapacitance(double modulationVoltage) {

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

		/* linear interpolation: C(v) = Clow + ((v - Vlow) * (Chigh - Clow) / (Vhigh - Vlow)) */
		return capacitanceLow + ((modulationVoltage - voltageLow) * (capacitanceHigh - capacitanceLow) / (voltageHigh - voltageLow));
	}

	void startIteration() {
		/* calc starting energy */
		double e = 0.5 * super.getCapacitance() * Math.pow(volts[2], 2);

		/* modify capacitance */
		super.setCapacitance(interpCapacitance(volts[2]));

		/* preserve energy continuity */
		double v = Math.sqrt(2 * e / super.getCapacitance());
		volts[2] = v;

		super.startIteration();
	}

	void getInfo(String arr[]) {
		super.getInfo(arr);
		arr[0] = "Capacitor (variable)";
		arr[5] = "Vmod = " + getUnitText(volts[2], "V");
	}
	public EditInfo getEditInfo(int n) {
		if (n == 4)
			return new EditInfo("Capacitance (F) at Low Voltage", capacitanceLow, 1e-12, 1);
		if (n == 5)
			return new EditInfo("Capacitance (F) at High Voltage", capacitanceHigh, 1e-12, 1);
		if (n == 6)
			return new EditInfo("Low Modulation Voltage (V)", voltageLow, -1000, 1000);
		if (n == 7)
			return new EditInfo("High Modulation Voltage (V)", voltageHigh, -1000, 1000);
		if (n == 8)
			return EditInfo.createCheckbox("Clamp Modulation Voltage", clampVoltage);
		return super.getEditInfo(n);
	}
	public void setEditValue(int n, EditInfo ei) {
		if (n == 4)
			capacitanceLow = (ei.value > 0) ? (ei.value > 1) ? 1 : ei.value : 1e-12;
		if (n == 5)
			capacitanceHigh = (ei.value > 0) ? (ei.value > 1) ? 1 : ei.value : 1e-12;
		if (n == 6)
			voltageLow = (ei.value > 1000) ? 1000 : (ei.value < -1000) ? -1000 : ei.value;
		if (n == 7)
			voltageHigh = (ei.value > 1000) ? 1000 : (ei.value < -1000) ? -1000 : ei.value;
		if (n == 8)
			clampVoltage = ei.checkbox.getState();
		super.setEditValue(n, ei);
	}
	int getShortcut() { return 0; }

	@Override
	public void setCapacitance(double c) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Capacitance may not be manually set for a Variable Capacitor as capacitance is set dynamically via the modulation voltage input.");
	}
}
