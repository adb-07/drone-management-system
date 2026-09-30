package com.dronefleet;
import com.dronefleet.domain.*;import com.dronefleet.strategy.*;import org.junit.jupiter.api.Test;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class DomainRulesTest{
 @Test void rejectsInvalidDrone(){assertThrows(IllegalArgumentException.class,()->new Drone(0,"Standard",10,100));assertThrows(IllegalArgumentException.class,()->new Drone(1,"Standard",51,100));}
 @Test void batteryAndStateLifecycle(){Drone d=new Drone(1,"Standard",10,100);d.markBusy();d.consumeBattery(85.1);assertEquals(15,d.battery());d.release();assertEquals(DroneStatus.LOW_BATTERY,d.status());d.beginRecharge();assertEquals(DroneStatus.CHARGING,d.status());d.finishRecharge();assertEquals(DroneStatus.AVAILABLE,d.status());assertEquals(100,d.battery());assertThrows(IllegalStateException.class,d::beginRecharge);}
 @Test void strategySelectsCapacityFit(){var a=new Drone(1,"Standard",20,100);var b=new Drone(2,"Standard",10,100);var p=new PackageItem(1,8,10,null);var chosen=new BestFitAssignmentStrategy().select(List.of(a,b),new AssignmentContext(p,"Any"));assertTrue(chosen.isPresent());assertEquals(2,chosen.get().id());}
 @Test void deliveryStateTransitions(){var p=new PackageItem(1,5,5,null);var d=new Delivery(1,p,null);assertEquals(DeliveryStatus.PENDING,d.status());d.assign(1,10,5,100);d.start();d.complete();assertEquals(DeliveryStatus.COMPLETED,d.status());assertNotNull(d.completedAt());}
}
