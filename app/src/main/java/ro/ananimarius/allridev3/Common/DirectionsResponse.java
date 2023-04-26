package ro.ananimarius.allridev3.Common;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class DirectionsResponse {
    private String status;
    private List<Route> routes;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public static class Route {
        private List<Leg> legs;

        public List<Leg> getLegs() {
            return legs;
        }

        public void setLegs(List<Leg> legs) {
            this.legs = legs;
        }

        public static class Leg {
            private Distance distance;
            private Duration duration;
            private LatLng startLocation;
            private LatLng endLocation;
            private List<Step> steps;

            public Distance getDistance() {
                return distance;
            }

            public void setDistance(Distance distance) {
                this.distance = distance;
            }

            public Duration getDuration() {
                return duration;
            }

            public void setDuration(Duration duration) {
                this.duration = duration;
            }

            public LatLng getStartLocation() {
                return startLocation;
            }

            public void setStartLocation(LatLng startLocation) {
                this.startLocation = startLocation;
            }

            public LatLng getEndLocation() {
                return endLocation;
            }

            public void setEndLocation(LatLng endLocation) {
                this.endLocation = endLocation;
            }

            public List<Step> getSteps() {
                return steps;
            }

            public void setSteps(List<Step> steps) {
                this.steps = steps;
            }

            public static class Distance {
                private String text;
                private int value;

                public String getText() {
                    return text;
                }

                public void setText(String text) {
                    this.text = text;
                }

                public int getValue() {
                    return value;
                }

                public void setValue(int value) {
                    this.value = value;
                }
            }

            public static class Duration {
                private String text;
                private int value;

                public String getText() {
                    return text;
                }

                public void setText(String text) {
                    this.text = text;
                }

                public int getValue() {
                    return value;
                }

                public void setValue(int value) {
                    this.value = value;
                }
            }

            public static class Step {
                private Distance distance;
                private Duration duration;
                private LatLng startLocation;
                private LatLng endLocation;
                private String htmlInstructions;
                private Polyline polyline;

                public Distance getDistance() {
                    return distance;
                }

                public void setDistance(Distance distance) {
                    this.distance = distance;
                }

                public Duration getDuration() {
                    return duration;
                }

                public void setDuration(Duration duration) {
                    this.duration = duration;
                }

                public LatLng getStartLocation() {
                    return startLocation;
                }

                public void setStartLocation(LatLng startLocation) {
                    this.startLocation = startLocation;
                }

                public LatLng getEndLocation() {
                    return endLocation;
                }

                public void setEndLocation(LatLng endLocation) {
                    this.endLocation = endLocation;
                }

                public String getHtmlInstructions() {
                    return htmlInstructions;
                }

                public void setHtmlInstructions(String htmlInstructions) {
                    this.htmlInstructions = htmlInstructions;
                }

                public Polyline getPolyline() {
                    return polyline;
                }

                public void setPolyline(Polyline polyline) {
                    this.polyline = polyline;
                }

                public static class Polyline {
                    private String points;

                    public String getPoints() {
                        return points;
                    }

                    public void setPoints(String points) {
                        this.points = points;
                    }
                }
            }
        }
    }
}
