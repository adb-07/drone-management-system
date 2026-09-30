package com.dronefleet.domain;
import java.time.LocalDateTime;
public record PackageItem(int id,double weight,double distance,LocalDateTime createdAt){
 public PackageItem{if(id<=0)throw new IllegalArgumentException("Package ID must be greater than 0");if(weight<=0||weight>50)throw new IllegalArgumentException("Weight must be between 1kg and 50kg");if(distance<=0||distance>70)throw new IllegalArgumentException("Distance must be between 1km and 70km");if(createdAt==null)createdAt=LocalDateTime.now();}
}
