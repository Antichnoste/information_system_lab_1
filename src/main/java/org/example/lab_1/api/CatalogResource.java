package org.example.lab_1.api;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.sse.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import org.example.lab_1.service.*;
import org.example.lab_1.model.*;
import static org.example.lab_1.api.Views.*;

@Path("/") @RequestScoped @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class CatalogResource {
 @Inject CatalogService service;
 @Inject RequestValidation validation;
 @Inject ChangeStream stream;
 @GET @Path("humans") public Page<HumanView> humans(@DefaultValue("0") @QueryParam("page") int page,
  @DefaultValue("20") @QueryParam("size") int size,@DefaultValue("id") @QueryParam("sort") String sort,
  @DefaultValue("asc") @QueryParam("direction") String direction,@Context UriInfo uri) {
  var filters=new HashMap<String,String>();
  for(String key:List.of("name","soundtrackName","carName","carColor","mood","weaponType")) filters.put(key,uri.getQueryParameters().getFirst(key));
  return service.humans(page,size,sort,direction,filters);
 }
 @GET @Path("humans/{id}") public HumanView human(@PathParam("id") int id) { return service.humanById(id); }
 @POST @Path("humans") public Response createHuman(Inputs.Human input) { return Response.status(201).entity(service.createHuman(validation.check(input))).build(); }
 @PUT @Path("humans/{id}") public HumanView updateHuman(@PathParam("id") int id,Inputs.Human input) { return service.updateHuman(id,validation.check(input)); }
 @DELETE @Path("humans/{id}") public void deleteHuman(@PathParam("id") int id,@QueryParam("version") Long version) { service.deleteHuman(id,version); }
 @GET @Path("cars") public List<CarView> cars() { return service.cars(); }
 @GET @Path("cars/{id}") public CarView car(@PathParam("id") long id) { return service.carById(id); }
 @POST @Path("cars") public Response createCar(Inputs.CarInput input) { return Response.status(201).entity(service.createCar(validation.check(input))).build(); }
 @PUT @Path("cars/{id}") public CarView updateCar(@PathParam("id") long id,Inputs.CarInput input) { return service.updateCar(id,validation.check(input)); }
 @DELETE @Path("cars/{id}") public void deleteCar(@PathParam("id") long id,@QueryParam("replacementId") Long replacement,@QueryParam("version") Long version) { service.deleteCar(id,replacement,version); }
 @GET @Path("coordinates") public List<CoordinatesView> coordinates() { return service.coordinates(); }
 @GET @Path("coordinates/{id}") public CoordinatesView coordinates(@PathParam("id") long id) { return service.coordinatesById(id); }
 @POST @Path("coordinates") public Response createCoordinates(Inputs.CoordinatesInput input) { return Response.status(201).entity(service.createCoordinates(validation.check(input))).build(); }
 @PUT @Path("coordinates/{id}") public CoordinatesView updateCoordinates(@PathParam("id") long id,Inputs.CoordinatesInput input) { return service.updateCoordinates(id,validation.check(input)); }
 @DELETE @Path("coordinates/{id}") public void deleteCoordinates(@PathParam("id") long id,@QueryParam("replacementId") Long replacement,@QueryParam("version") Long version) { service.deleteCoordinates(id,replacement,version); }
 @DELETE @Path("operations/by-weapon") public Changed weapon(@QueryParam("weaponType") WeaponType weapon) { return service.deleteByWeapon(weapon); }
 @GET @Path("operations/minimum-waiting") public Response minimum() { var result=service.minimumWaiting(); return result==null?Response.noContent().build():Response.ok(result).build(); }
 @GET @Path("operations/soundtrack") public List<HumanView> soundtrack(@QueryParam("substring") String substring) { return service.soundtrack(substring); }
 @POST @Path("operations/sadden") public Changed sadden() { return service.sadden(); }
 @POST @Path("operations/give-cars") public Changed giveCars() { return service.giveCars(); }
 @GET @Path("events") @Produces(MediaType.SERVER_SENT_EVENTS)
 public void events(@Context SseEventSink sink,@Context Sse sse,@Context HttpServletRequest request) { stream.subscribe(sink,sse,request.getSession(false)); }
 @GET @Path("health") public Map<String,String> health() { return Map.of("status","up"); }
}
