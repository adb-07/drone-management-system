package com.dronefleet.domain;

public final class Drone {
    private final int id; private final String type; private final double capacity;
    private int battery; private DroneStatus status; private int completedDeliveries; private double totalDistance,totalRevenue;
    public Drone(int id,String type,double capacity,int battery,DroneStatus status,int completed,double distance,double revenue){
        if(id<=0) throw new IllegalArgumentException("Drone ID must be greater than 0");
        if(type==null||type.isBlank()) throw new IllegalArgumentException("Drone type is required");
        if(capacity<=0||capacity>50) throw new IllegalArgumentException("Capacity must be between 1kg and 50kg");
        if(battery<0||battery>100) throw new IllegalArgumentException("Battery must be between 0% and 100%");
        this.id=id;this.type=type;this.capacity=capacity;this.battery=battery;this.status=status;this.completedDeliveries=completed;this.totalDistance=distance;this.totalRevenue=revenue;
    }
    public Drone(int id,String type,double capacity,int battery){this(id,type,capacity,battery,battery<20?DroneStatus.LOW_BATTERY:DroneStatus.AVAILABLE,0,0,0);}
    public int id(){return id;} public String type(){return type;} public double capacity(){return capacity;} public int battery(){return battery;} public DroneStatus status(){return status;}
    public int completedDeliveries(){return completedDeliveries;} public double totalDistance(){return totalDistance;} public double totalRevenue(){return totalRevenue;}
    public boolean hasBatteryFor(double use){return use>=0&&battery>=use;}
    public void markBusy(){if(status!=DroneStatus.AVAILABLE) throw new IllegalStateException("Drone is not available"); status=DroneStatus.BUSY;}
    public void release(){status=battery<20?DroneStatus.LOW_BATTERY:DroneStatus.AVAILABLE;}
    public void maintenance(boolean on){if(on&&status==DroneStatus.BUSY) throw new IllegalStateException("Busy drone cannot enter maintenance"); status=on?DroneStatus.MAINTENANCE:(battery<20?DroneStatus.LOW_BATTERY:DroneStatus.AVAILABLE);}
    public void beginRecharge(){
        if(battery>=100) throw new IllegalStateException("Drone battery is already at 100%.");
        if(status==DroneStatus.BUSY) throw new IllegalStateException("Busy drone cannot be recharged.");
        if(status==DroneStatus.MAINTENANCE) throw new IllegalStateException("Drone is in maintenance.");
        if(status==DroneStatus.CHARGING) throw new IllegalStateException("Drone is already charging.");
        status=DroneStatus.CHARGING;
    }
    public void finishRecharge(){
        if(status!=DroneStatus.CHARGING) throw new IllegalStateException("Drone is not charging.");
        battery=100;
        status=DroneStatus.AVAILABLE;
    }
    public void interruptRecharge(){
        if(status==DroneStatus.CHARGING) status=battery<20?DroneStatus.LOW_BATTERY:DroneStatus.AVAILABLE;
    }
    public void consumeBattery(double use){if(use<0)throw new IllegalArgumentException("Battery use cannot be negative");battery=Math.max(0,(int)Math.ceil(battery-use));}
    public void recordDelivery(double distance,double revenue){completedDeliveries++;totalDistance+=distance;totalRevenue+=revenue;}
}
