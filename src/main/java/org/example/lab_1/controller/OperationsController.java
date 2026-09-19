package org.example.lab_1.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.example.lab_1.model.WeaponType;
import org.example.lab_1.dto.HumanResponse;
import org.example.lab_1.dto.OperationResponse;
import org.example.lab_1.service.OperationsService;

@Path("operations")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OperationsController {
    @Inject
    OperationsService operationsService;

    @DELETE
    @Path("by-weapon")
    public OperationResponse weapon(@QueryParam("weaponType") WeaponType weapon) {
        return operationsService.deleteByWeapon(weapon);
    }

    @GET
    @Path("minimum-waiting")
    public Response minimum() {
        var result = operationsService.minimumWaiting();
        return result == null
                ? Response.noContent().build()
                : Response.ok(result).build();
    }

    @GET
    @Path("soundtrack")
    public List<HumanResponse> soundtrack(@QueryParam("substring") String substring) {
        return operationsService.soundtrack(substring);
    }

    @POST
    @Path("sadden")
    public OperationResponse sadden() {
        return operationsService.sadden();
    }

    @POST
    @Path("give-cars")
    public OperationResponse giveCars() {
        return operationsService.giveCars();
    }
}
