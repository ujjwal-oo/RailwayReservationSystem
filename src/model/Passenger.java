package model;

/**
 * Simple data holder for a passenger. Passed as an OBJECT (not a raw String)
 * to Observer.update() as per instructor feedback on earlier practicals.
 */
public class Passenger {
    private String name;
    private int age;
    private String gender;
    private String mobileNumber;
    private String email;

    public Passenger(String name, int age, String gender, String mobileNumber, String email) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.mobileNumber = mobileNumber;
        this.email = email;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getMobileNumber() { return mobileNumber; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return name + " (" + age + "/" + gender + ")";
    }
}
