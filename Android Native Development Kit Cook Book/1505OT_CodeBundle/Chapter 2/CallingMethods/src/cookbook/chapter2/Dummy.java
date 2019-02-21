package cookbook.chapter2;

public class Dummy {
	public int value;
	private static int value2 = 100;
	public Dummy(int pValue) {
		value = pValue;
	}
	
	public static int getValue2() {
		return value2;
	}
	
	public static void setValue2(int pValue) {
		value2 = pValue;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int pValue) {
		value = pValue;
	}
}
