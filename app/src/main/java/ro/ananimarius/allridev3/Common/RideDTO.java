package ro.ananimarius.allridev3.Common;

import java.math.BigDecimal;
import java.util.Set;

public class RideDTO {
    private Long id;
    private UserDTO passenger;
    private UserDTO driver;
    private Set<WaypointDTO> route;
    private BigDecimal cost;
    private String currency;

    public RideDTO(Long id, UserDTO passenger, UserDTO driver, Set<WaypointDTO> route, BigDecimal cost, String currency) {
        this.id = id;
        this.passenger = passenger;
        this.driver = driver;
        this.route = route;
        this.cost = cost;
        this.currency = currency;
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
}