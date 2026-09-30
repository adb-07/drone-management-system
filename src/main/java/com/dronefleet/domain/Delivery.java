package com.dronefleet.domain;
import java.time.LocalDateTime;
public final class Delivery {
 private final int id; private final PackageItem item; private Integer droneId; private DeliveryStatus status; private int estimatedMinutes; private double batteryUsed,charge; private String failureReason; private final LocalDateTime createdAt; private LocalDateTime completedAt;
 public Delivery(int id,PackageItem item,LocalDateTime createdAt){this.id=id;this.item=item;this.status=DeliveryStatus.PENDING;this.createdAt=createdAt==null?LocalDateTime.now():createdAt;}
 public int id(){return id;} public PackageItem item(){return item;} public Integer droneId(){return droneId;} public DeliveryStatus status(){return status;} public int estimatedMinutes(){return estimatedMinutes;} public double batteryUsed(){return batteryUsed;} public double charge(){return charge;} public String failureReason(){return failureReason;} public LocalDateTime createdAt(){return createdAt;} public LocalDateTime completedAt(){return completedAt;}
 public void assign(int droneId,int minutes,double battery,double charge){this.droneId=droneId;this.estimatedMinutes=minutes;this.batteryUsed=battery;this.charge=charge;this.status=DeliveryStatus.ASSIGNED;}
 public void start(){if(status!=DeliveryStatus.ASSIGNED)throw new IllegalStateException("Delivery must be assigned first");status=DeliveryStatus.IN_TRANSIT;}
 public void complete(){if(status!=DeliveryStatus.IN_TRANSIT)throw new IllegalStateException("Delivery is not in transit");status=DeliveryStatus.COMPLETED;completedAt=LocalDateTime.now();}
 public void fail(String reason){status=DeliveryStatus.FAILED;failureReason=reason;completedAt=LocalDateTime.now();}
}
