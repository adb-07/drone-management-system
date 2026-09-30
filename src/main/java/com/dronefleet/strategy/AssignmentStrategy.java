package com.dronefleet.strategy;import com.dronefleet.domain.*;import java.util.*;public interface AssignmentStrategy{Optional<Drone> select(List<Drone> drones,AssignmentContext ctx);String name();}
