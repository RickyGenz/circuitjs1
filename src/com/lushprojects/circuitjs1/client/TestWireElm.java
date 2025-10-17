/*
    TestWireElm by Ricky Genz
*/

package com.lushprojects.circuitjs1.client;

public class TestWireElm extends WireElm {

	public TestWireElm(int xx, int yy) {
	    super(xx, yy);
		noDiagonal = true;
	}
	public TestWireElm(int xa, int ya, int xb, int yb, int f,
			    StringTokenizer st) {
	    super(xa, ya, xb, yb, f, st);
		noDiagonal = true;
	}

	int getDumpType() { return 300; }
	String dump() {
	    return super.dump() + " TEST";
	}

	Point point3, lead3;
	void setPoints() {
		super.setPoints();
		calcLeads(32);
		adjustLeadsToGrid(false, false);
		point3 = interpPoint(lead1, lead2, .5, 0);
		lead3  = interpPoint(lead1, lead2, .25, 6);
	}

	void draw(Graphics g) {
		super.draw(g);

		setVoltageColor(g, volts[2]);
		drawThickLine(g, point3, lead3);
	}

	int getPostCount() { return 3; }
	Point getPost(int n) {
		return (n == 0) ? point1 : (n == 1) ? point2 : point3;
    }

	void getInfo(String arr[]) {
	    super.getInfo(arr);
	    arr[0] = "Test Wire";
		arr[3] = "Mod = " + getUnitText(volts[2], "V");
	}
	int getShortcut() { return 0; }
}