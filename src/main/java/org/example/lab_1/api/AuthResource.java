package org.example.lab_1.api;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
import org.example.lab_1.security.AuthService;
import org.example.lab_1.service.ChangeStream;
@Path("auth") @RequestScoped @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
 @Inject AuthService service;
 @Inject RequestValidation validation;
 @Inject ChangeStream stream;
 @Context HttpServletRequest request;
 @POST @Path("login") public Map<String,String> login(Inputs.Login in) {
  validation.check(in);
  String username=service.login(in.username,in.password);
  HttpSession previous=request.getSession(false);
  if(previous!=null) { stream.disconnect(previous); previous.invalidate(); }
  HttpSession session=request.getSession(true);
  session.setAttribute("username",username); session.setAttribute("csrf",UUID.randomUUID().toString());
  return session();
 }
 @GET @Path("session") public Map<String,String> session() {
  HttpSession session=request.getSession(false);
  return Map.of("username",(String)session.getAttribute("username"),"csrfToken",(String)session.getAttribute("csrf"));
 }
 @POST @Path("logout") public void logout() {
  HttpSession session=request.getSession(false); stream.disconnect(session); session.invalidate();
 }
}
