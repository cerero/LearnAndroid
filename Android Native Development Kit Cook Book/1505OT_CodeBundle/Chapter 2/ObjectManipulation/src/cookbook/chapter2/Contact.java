package cookbook.chapter2;

public class Contact {
	private String name;
	private int age;
	
	public Contact(String pName, int pAge) {
		name = pName;
		age = pAge;
	}
	
	public String getContactStr() {
		return name + ":" + age;
	}
}
