package CommonUtilPackage;

/**
 * Created by Asher on 26.05.2016.
 */
public class ContactItem{
    private String name;
    private String phoneNumber;

    public ContactItem(String name, String phoneNumber){
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return name;
    }
}
