package ro.ananimarius.allridev3.Common;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class RideDTO implements Cloneable{
    private Long id;
    private UserDTO passenger;
    private UserDTO driver;
    private Set<WaypointDTO> route;
    private BigDecimal cost;
    private String currency;
    private boolean nearDestination=false;
    private boolean nearCustomer=false;
    private boolean customerEndsRide=false;
    private boolean customerCancelsRide=false;
    private boolean driverEndsRide=false;
    private boolean driverCancelsRide=false;
    private BigDecimal currentRidePrice;
    private Double currentRideTotalDistance;
    private BigDecimal currentRideTotalTime;

    public Double getCurrentRideTotalDistance() {
        return currentRideTotalDistance;
    }

    public void setCurrentRideTotalDistance(Double currentRideTotalDistance) {
        this.currentRideTotalDistance = currentRideTotalDistance;
    }

    public BigDecimal getCurrentRideTotalTime() {
        return currentRideTotalTime;
    }

    public void setCurrentRideTotalTime(BigDecimal currentRideTotalTime) {
        this.currentRideTotalTime = currentRideTotalTime;
    }

    public BigDecimal getCurrentRidePrice() {
        return currentRidePrice;
    }

    public void setCurrentRidePrice(BigDecimal currentRidePrice) {
        this.currentRidePrice = currentRidePrice;
    }

    public RideDTO(Long id, UserDTO passenger, UserDTO driver, Set<WaypointDTO> route, BigDecimal cost, String currency, boolean nearDestination, boolean nearCustomer, boolean customerEndsRide, boolean customerCancelsRide, boolean driverEndsRide, boolean driverCancelsRide) {
        this.id = id;
        this.passenger = passenger;
        this.driver = driver;
        this.route = route;
        this.cost = cost;
        this.currency = currency;
        this.nearDestination = nearDestination;
        this.nearCustomer = nearCustomer;
        this.customerEndsRide = customerEndsRide;
        this.customerCancelsRide = customerCancelsRide;
        this.driverEndsRide = driverEndsRide;
        this.driverCancelsRide = driverCancelsRide;
    }

    public RideDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getPassenger() {
        return passenger;
    }

    public void setPassenger(UserDTO passenger) {
        this.passenger = passenger;
    }

    public UserDTO getDriver() {
        return driver;
    }

    public void setDriver(UserDTO driver) {
        this.driver = driver;
    }

    public Set<WaypointDTO> getRoute() {
        return route;
    }

    public void setRoute(Set<WaypointDTO> route) {
        this.route = route;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isCustomerEndsRide() {
        return customerEndsRide;
    }

    public void setCustomerEndsRide(boolean customerEndsRide) {
        this.customerEndsRide = customerEndsRide;
    }

    public boolean isCustomerCancelsRide() {
        return customerCancelsRide;
    }

    public void setCustomerCancelsRide(boolean customerCancelsRide) {
        this.customerCancelsRide = customerCancelsRide;
    }

    public boolean isDriverEndsRide() {
        return driverEndsRide;
    }

    public void setDriverEndsRide(boolean driverEndsRide) {
        this.driverEndsRide = driverEndsRide;
    }

    public boolean isDriverCancelsRide() {
        return driverCancelsRide;
    }

    public void setDriverCancelsRide(boolean driverCancelsRide) {
        this.driverCancelsRide = driverCancelsRide;
    }

    public boolean isNearDestination() {
        return nearDestination;
    }

    public void setNearDestination(boolean nearDestination) {
        this.nearDestination = nearDestination;
    }

    public boolean isNearCustomer() {
        return nearCustomer;
    }

    public void setNearCustomer(boolean nearCustomer) {
        this.nearCustomer = nearCustomer;
    }
    @Override
    public RideDTO clone() {
        try {
            RideDTO clone = (RideDTO) super.clone();
            clone.id = this.id;
            clone.passenger = this.getPassenger().clone();
            clone.driver = this.getDriver().clone();
            clone.cost = this.cost;
            clone.currency = this.currency;
            clone.nearDestination = this.nearDestination;
            clone.nearCustomer = this.nearCustomer;
            clone.customerEndsRide = this.customerEndsRide;
            clone.customerCancelsRide = this.customerCancelsRide;
            clone.driverEndsRide = this.driverEndsRide;
            clone.driverCancelsRide = this.driverCancelsRide;
            clone.currentRidePrice = this.currentRidePrice;
            clone.currentRideTotalDistance = this.currentRideTotalDistance;
            clone.currentRideTotalTime = this.currentRideTotalTime;

            // Cloning Set<WaypointDTO>
            Set<WaypointDTO> clonedRoute = new HashSet<>();
            for(WaypointDTO waypoint : this.route){
                clonedRoute.add(waypoint.clone());
            }
            clone.route = clonedRoute;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("The RideDTO object could not be cloned.", e);
        }
    }
}