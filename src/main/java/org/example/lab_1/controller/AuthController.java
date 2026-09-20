package org.example.lab_1.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
    @Path("register")
    public Response register(@NotNull(message = "Укажите логин и пароль") @Valid AuthRequest input) {
        String username = authService.register(input);
        return Response.status(Response.Status.CREATED)
                        .entity(new AuthResponse(username))
                        .build();
    }

    @POST
    @Path("login")
    public AuthResponse login(@NotNull(message = "Укажите логин и пароль") @Valid AuthRequest input) {
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
