package com.dronefleet.service;

import com.dronefleet.config.AppConfig;
import com.dronefleet.domain.*;
import com.dronefleet.repository.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class FleetService {
    private final DroneRepository repo;
    private final AuditRepository audit;
    private final AppConfig cfg;
    private final DeliveryRepository deliveries;
    private final ScheduledExecutorService rechargeExecutor = Executors.newScheduledThreadPool(2);

    public FleetService(DroneRepository repo, AuditRepository audit, AppConfig cfg, DeliveryRepository deliveries) {
        this.repo=repo; this.audit=audit; this.cfg=cfg; this.deliveries=deliveries;
    }

    public synchronized void addDrone(Drone d,String user){
        if(repo.findAll().size()>=cfg.maxDrones()) throw new IllegalStateException("Maximum fleet size reached");
        if(repo.find(d.id()).isPresent()) throw new IllegalArgumentException("Drone ID already exists");
        repo.save(d); audit.log(user,"DRONE_CREATED","DRONE",String.valueOf(d.id()),d.type());
    }

    public synchronized void removeDrone(int id,String user){
        Drone d=repo.find(id).orElseThrow(()->new IllegalArgumentException("Drone not found"));
        if(d.status()==DroneStatus.BUSY || d.status()==DroneStatus.CHARGING) throw new IllegalStateException("Active drone cannot be removed");
        if(deliveries.hasDeliveriesForDrone(id)) throw new IllegalStateException("This drone has delivery history and cannot be removed. Put it into maintenance instead.");
        repo.delete(id); audit.log(user,"DRONE_REMOVED","DRONE",String.valueOf(id),d.type());
    }

    /** Starts a simulated recharge. The callback is invoked after the battery reaches 100%. */
    public synchronized int startRecharge(int id,String user, Consumer<Drone> onComplete){
        Drone d=repo.find(id).orElseThrow(()->new IllegalArgumentException("Drone not found"));
        int seconds=cfg.rechargeDurationSeconds();
        d.beginRecharge();
        repo.save(d);
        audit.log(user,"RECHARGE_STARTED","DRONE",String.valueOf(id),"from="+d.battery()+"% duration="+seconds+"s");
        rechargeExecutor.schedule(() -> {
            try {
                synchronized (FleetService.this) {
                    Drone current=repo.find(id).orElse(null);
                    if(current==null || current.status()!=DroneStatus.CHARGING) return;
                    current.finishRecharge();
                    repo.save(current);
                    audit.log(user,"RECHARGE_COMPLETED","DRONE",String.valueOf(id),"Battery=100%");
                    if(onComplete!=null) onComplete.accept(current);
                }
            } catch(Exception ignored) { }
        }, seconds, TimeUnit.SECONDS);
        return seconds;
    }

    public synchronized void maintenance(int id,boolean on,String user){
        Drone d=repo.find(id).orElseThrow(()->new IllegalArgumentException("Drone not found"));
        d.maintenance(on); repo.save(d);
        audit.log(user,on?"MAINTENANCE_STARTED":"MAINTENANCE_ENDED","DRONE",String.valueOf(id),"status="+d.status());
    }

    public List<Drone> all(){return repo.findAll();}

    public synchronized void recoverAfterRestart(java.util.Set<Integer> affectedDroneIds){
        for(Integer id: affectedDroneIds){
            repo.find(id).ifPresent(d -> { if(d.status()==DroneStatus.BUSY){ d.release(); repo.save(d); } else if(d.status()==DroneStatus.CHARGING){ d.interruptRecharge(); repo.save(d); } });
        }
    }

    public void shutdown(){rechargeExecutor.shutdownNow();}
}
