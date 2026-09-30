package com.dronefleet.repository;
import com.dronefleet.domain.*;import java.sql.*;import java.time.LocalDateTime;import java.util.*;
public final class DeliveryRepository {private final com.dronefleet.config.Database db;public DeliveryRepository(com.dronefleet.config.Database db){this.db=db;}
 public int nextId(){try(Connection c=db.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COALESCE(MAX(id),0)+1 FROM deliveries")){return r.next()?r.getInt(1):1;}catch(SQLException e){throw new RuntimeException(e);}}
 public boolean packageExists(int id){
  try(Connection c=db.connect();PreparedStatement p=c.prepareStatement("SELECT 1 FROM packages WHERE id=? LIMIT 1")){
   p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next();}
  }catch(SQLException e){throw new RuntimeException(e);}
 }
 public boolean hasDeliveriesForDrone(int droneId){
  try(Connection c=db.connect();PreparedStatement p=c.prepareStatement("SELECT 1 FROM deliveries WHERE drone_id=? LIMIT 1")){
   p.setInt(1,droneId);try(ResultSet r=p.executeQuery()){return r.next();}
  }catch(SQLException e){throw new RuntimeException(e);}
 }
 public java.util.Set<Integer> recoverInterruptedDeliveries(){
  java.util.Set<Integer> ids=new java.util.HashSet<>();
  String q="SELECT DISTINCT drone_id FROM deliveries WHERE status IN ('ASSIGNED','IN_TRANSIT') AND drone_id IS NOT NULL";
  try(Connection c=db.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){
   while(r.next())ids.add(r.getInt(1));
   try(PreparedStatement p=c.prepareStatement("UPDATE deliveries SET status='FAILED',failure_reason=?,completed_at=? WHERE status IN ('ASSIGNED','IN_TRANSIT')")){
    p.setString(1,"Application restarted before simulated delivery completed");
    p.setString(2,java.time.LocalDateTime.now().toString());
    p.executeUpdate();
   }
  }catch(SQLException e){throw new RuntimeException(e);}
  return ids;
 }
 public void savePackage(PackageItem x){try(Connection c=db.connect();PreparedStatement p=c.prepareStatement("INSERT INTO packages(id,weight,distance,created_at) VALUES(?,?,?,?) ON CONFLICT(id) DO UPDATE SET weight=excluded.weight,distance=excluded.distance")){p.setInt(1,x.id());p.setDouble(2,x.weight());p.setDouble(3,x.distance());p.setString(4,x.createdAt().toString());p.executeUpdate();}catch(SQLException e){throw new RuntimeException(e);}}
 public void save(Delivery d){String q="INSERT INTO deliveries(id,package_id,drone_id,status,estimated_minutes,battery_used,charge,failure_reason,created_at,completed_at) VALUES(?,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET drone_id=excluded.drone_id,status=excluded.status,estimated_minutes=excluded.estimated_minutes,battery_used=excluded.battery_used,charge=excluded.charge,failure_reason=excluded.failure_reason,completed_at=excluded.completed_at";try(Connection c=db.connect();PreparedStatement p=c.prepareStatement(q)){int i=1;p.setInt(i++,d.id());p.setInt(i++,d.item().id());if(d.droneId()==null)p.setNull(i++,Types.INTEGER);else p.setInt(i++,d.droneId());p.setString(i++,d.status().name());p.setInt(i++,d.estimatedMinutes());p.setDouble(i++,d.batteryUsed());p.setDouble(i++,d.charge());p.setString(i++,d.failureReason());p.setString(i++,d.createdAt().toString());p.setString(i,d.completedAt()==null?null:d.completedAt().toString());p.executeUpdate();}catch(SQLException e){throw new RuntimeException(e);}}
 public List<Delivery> findAll(){List<Delivery> out=new ArrayList<>();String q="SELECT d.*,p.weight,p.distance,p.created_at p_created FROM deliveries d JOIN packages p ON p.id=d.package_id ORDER BY d.id DESC";try(Connection c=db.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){while(r.next()){PackageItem p=new PackageItem(r.getInt("package_id"),r.getDouble("weight"),r.getDouble("distance"),LocalDateTime.parse(r.getString("p_created")));Delivery d=new Delivery(r.getInt("id"),p,LocalDateTime.parse(r.getString("created_at")));Integer drone=(Integer)r.getObject("drone_id");if(drone!=null)d.assign(drone,r.getInt("estimated_minutes"),r.getDouble("battery_used"),r.getDouble("charge"));if(d.status()==DeliveryStatus.ASSIGNED)d.start();String st=r.getString("status");if("COMPLETED".equals(st)){d.complete();}else if("FAILED".equals(st)){d.fail(r.getString("failure_reason"));}out.add(d);}}catch(SQLException e){throw new RuntimeException(e);}return out;}
}
