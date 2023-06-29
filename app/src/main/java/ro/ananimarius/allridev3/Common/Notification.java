package ro.ananimarius.allridev3.Common;

public class Notification implements Cloneable{
    private String customerId;
    private String driverId;
    private String customerFirstName;
    private String customerLastName;
    private double custLatitude;
    private double custLongitude;
    private double destLatitude;
    private double destLongitude;
    private boolean isRead;
    private int timeoutSeconds;
    //customer photo uri
    //customer rating
    private long timeCreated;
    private RideDTO informativeRide;

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public RideDTO getInformativeRide() {
        return informativeRide;
    }

    public void setInformativeRide(RideDTO informativeRide) {
        this.informativeRide = informativeRide;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public double getCustLatitude() {
        return custLatitude;
    }

    public void setCustLatitude(double custLatitude) {
        this.custLatitude = custLatitude;
    }

    public double getCustLongitude() {
        return custLongitude;
    }

    public void setCustLongitude(double custLongitude) {
        this.custLongitude = custLongitude;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public double getDestLatitude() {
        return destLatitude;
    }

    public void setDestLatitude(double destLatitude) {
        this.destLatitude = destLatitude;
    }

    public double getDestLongitude() {
        return destLongitude;
    }

    public void setDestLongitude(double destLongitude) {
        this.destLongitude = destLongitude;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean read) {
        isRead = read;
    }
    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Notification(String customerId, String driverId, String customerFirstName, String customerLastName, double custLatitude, double custLongitude, double destLatitude, double destLongitude) {
        this.customerId = customerId;
        this.driverId = driverId;
        this.customerFirstName = customerFirstName;
        this.customerLastName = customerLastName;
        this.custLatitude = custLatitude;
        this.custLongitude = custLongitude;
        this.destLatitude = destLatitude;
        this.destLongitude = destLongitude;
        this.isRead = false;
        this.timeCreated = System.currentTimeMillis();
    }
    @Override
    public Notification clone() {
        try {
            Notification clone = (Notification) super.clone();
            clone.customerId = this.customerId;
            clone.driverId = this.driverId;
            clone.customerFirstName = this.customerFirstName;
            clone.customerLastName = this.customerLastName;
            clone.custLatitude = this.custLatitude;
            clone.custLongitude = this.custLongitude;
            clone.destLatitude = this.destLatitude;
            clone.destLongitude = this.destLongitude;
            clone.isRead = this.isRead;
            clone.timeoutSeconds = this.timeoutSeconds;
            clone.timeCreated = this.timeCreated;
            clone.informativeRide = (this.informativeRide != null) ? this.informativeRide.clone() : null;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("The Notification object could not be cloned.", e);
        }
    }
}
