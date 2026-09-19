package org.example.lab_1.controller;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.example.lab_1.dto.CarRequest;
import org.example.lab_1.dto.CarResponse;
import org.example.lab_1.service.CarService;

@Path("cars")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarController {
    @Inject
    CarService carService;

    @GET
    public List<CarResponse> cars() {
        return carService.list();
    }

    @GET
    @Path("{id}")
    public CarResponse car(@PathParam("id") long id) {
        return carService.get(id);
    }

    @POST
    public Response createCar(@NotNull(message = "Тело запроса обязательно") @Valid CarRequest input) {
        return Response.status(201)
                .entity(carService.create(input))
                .build();
    }

    @PUT
    @Path("{id}")
    public CarResponse updateCar(@PathParam("id") long id, @NotNull(message = "Тело запроса обязательно") @Valid CarRequest input) {
        return carService.update(id, input);
    }

    @DELETE
    @Path("{id}")
    public void deleteCar(
            @PathParam("id") long id,
            @QueryParam("replacementId") Long replacement) {
        carService.delete(id, replacement);
    }
}
