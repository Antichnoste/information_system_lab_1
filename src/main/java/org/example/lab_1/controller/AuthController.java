package org.example.lab_1.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.example.lab_1.dto.AuthRequest;
import org.example.lab_1.dto.AuthResponse;
import org.example.lab_1.service.AuthService;

@Path("auth")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {
    @Context
    HttpServletRequest request;
    @Inject
    AuthService authService;

    @POST
    @Path("login")
    public AuthResponse login(AuthRequest input) {
        String username = authService.authenticate(input);
        request.getSession(true).setAttribute("username", username);
        return new AuthResponse(username);
    }

    @GET
    @Path("session")
    public AuthResponse session() {
        HttpSession session = request.getSession(false);
        return new AuthResponse((String) session.getAttribute("username"));
    }

    @POST
    @Path("logout")
    public void logout() {
        request.getSession(false).invalidate();
    }
}
