package CommonUtilPackage;

import java.io.Serializable;

/**
 * Created by Asher on 26.05.2016.
 */
public class ContactItem implements Serializable{
    private String name;
    private String phoneNumber;
    private boolean isSelected;

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

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() { return name; }

    public boolean getIsSelected() { return isSelected; }

}
