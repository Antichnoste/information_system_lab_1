package org.example.lab_1.service;
import jakarta.annotation.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.*;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.sse.*;
import java.util.concurrent.*;

@ApplicationScoped
public class ChangeStream {
 @Resource ManagedScheduledExecutorService executor;
 private record Client(SseEventSink sink,Sse sse,HttpSession session) {}
 private final ConcurrentMap<SseEventSink,Client> clients=new ConcurrentHashMap<>();
 private ScheduledFuture<?> heartbeat;
 @PostConstruct void start() { heartbeat=executor.scheduleAtFixedRate(()->send("heartbeat"),15,15,TimeUnit.SECONDS); }
 @PreDestroy void stop() { if(heartbeat!=null) heartbeat.cancel(false); clients.keySet().forEach(SseEventSink::close); clients.clear(); }
 public void subscribe(SseEventSink sink,Sse sse,HttpSession session) {
  Client client=new Client(sink,sse,session); clients.put(sink,client); send(client,"ready");
 }
 public void onChange(@Observes(during=TransactionPhase.AFTER_SUCCESS) DomainChange change) { send("changed"); }
 private void send(String event) { clients.values().forEach(c->send(c,event)); }
 private void send(Client c,String event) {
  try {
   c.session.getCreationTime();
   if(c.sink.isClosed()) { clients.remove(c.sink); return; }
   c.sink.send(c.sse.newEventBuilder().name(event).data(String.class,"refresh").reconnectDelay(2000).build())
    .whenComplete((ok,error)->{ if(error!=null) { clients.remove(c.sink); c.sink.close(); } });
  } catch(Exception e) { clients.remove(c.sink); c.sink.close(); }
 }
 public void disconnect(HttpSession session) {
  clients.values().stream().filter(c->c.session==session).forEach(c->{ clients.remove(c.sink); c.sink.close(); });
 }
}
