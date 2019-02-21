package cookbook.chapter2;

public class DummySub extends Dummy {
	private String name;
	public DummySub(int pValue, String pName) {
		super(pValue);
		name = pName;
	}
	public String getName() {
		return name;
	}
	public void setName(String pName) {
		name = pName;
	}
}
