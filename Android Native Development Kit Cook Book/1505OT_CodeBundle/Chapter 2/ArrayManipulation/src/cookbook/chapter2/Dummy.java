package cookbook.chapter2;

public class Dummy {
	private int value;
	
	public Dummy(int pValue) {
		value = pValue;
	}
	
	public String getDummyValue(){
		return String.valueOf(value);
	}
}
