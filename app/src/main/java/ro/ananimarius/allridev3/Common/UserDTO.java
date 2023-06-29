package ro.ananimarius.allridev3.Common;

import android.graphics.Point;

public class UserDTO implements Cloneable{
    private long id;
    private String firstName;
    private String lastName;
    private byte[] avatar;
    private String phone;
    private String email;
    private String password;
    private String facebookId;
    private String googleId;
    private boolean driver;
    private String car;
    private Point location;
    private boolean hailing;
    private Long assignedUser;
    private float currentRating;
    private double latitude;
    private double longitude;
    private float direction;

    public UserDTO() {
    }

    public UserDTO(long id, String firstName, String lastName, byte[] avatar, String phone, String email, String password, String facebookId, String googleId, boolean driver, String car, Point location, boolean hailing, Long assignedUser, float currentRating, double latitude, double longitude, float direction) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.facebookId = facebookId;
        this.googleId = googleId;
        this.driver = driver;
        this.car = car;
        this.location = location;
        this.hailing = hailing;
        this.assignedUser = assignedUser;
        this.currentRating = currentRating;
        this.latitude = latitude;
        this.longitude = longitude;
        this.direction = direction;
    }

    // Getters and Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public boolean isDriver() {
        return driver;
    }

    public void setDriver(boolean driver) {
        this.driver = driver;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public boolean isHailing() {
        return hailing;
    }

    public void setHailing(boolean hailing) {
        this.hailing = hailing;
    }

    public Long getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(Long assignedUser) {
        this.assignedUser = assignedUser;
    }

    public float getCurrentRating() {
        return currentRating;
    }

    public void setCurrentRating(float currentRating) {
        this.currentRating = currentRating;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    @Override
    public UserDTO clone() {
        try {
            UserDTO clone = (UserDTO) super.clone();
            clone.id = this.id;
            clone.firstName = this.firstName;
            clone.lastName = this.lastName;
            // clone avatar byte array
            if (this.avatar != null) {
                clone.avatar = this.avatar.clone();
            }
            clone.phone = this.phone;
            clone.email = this.email;
            clone.password = this.password;
            clone.facebookId = this.facebookId;
            clone.googleId = this.googleId;
            clone.driver = this.driver;
            clone.car = this.car;
            if (this.location != null) {
                clone.location = new Point(this.location);
            }
            clone.hailing = this.hailing;
            clone.assignedUser = this.assignedUser;
            clone.currentRating = this.currentRating;
            clone.latitude = this.latitude;
            clone.longitude = this.longitude;
            clone.direction = this.direction;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("The UserDTO object could not be cloned.", e);
        }
    }
}
