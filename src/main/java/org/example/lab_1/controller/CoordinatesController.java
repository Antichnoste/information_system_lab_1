package org.example.lab_1.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.example.lab_1.dto.CoordinatesRequest;
import org.example.lab_1.dto.CoordinatesResponse;
import org.example.lab_1.service.CoordinatesService;

@Path("coordinates")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordinatesController {
    @Inject
    CoordinatesService coordinatesService;

    @GET
    public List<CoordinatesResponse> coordinates() {
        return coordinatesService.list();
    }

    @GET
    @Path("{id}")
    public CoordinatesResponse coordinates(@PathParam("id") long id) {
        return coordinatesService.get(id);
    }

    @POST
    public Response createCoordinates(@NotNull(message = "Тело запроса обязательно") @Valid CoordinatesRequest input) {
        return Response.status(201)
                .entity(coordinatesService.create(input))
                .build();
    }

    @PUT
    @Path("{id}")
    public CoordinatesResponse updateCoordinates(
            @PathParam("id") long id,
            @NotNull(message = "Тело запроса обязательно") @Valid CoordinatesRequest input) {
        return coordinatesService.update(id, input);
    }

    @DELETE
    @Path("{id}")
    public void deleteCoordinates(
            @PathParam("id") long id,
            @QueryParam("replacementId") Long replacement) {
        coordinatesService.delete(id, replacement);
    }
}
