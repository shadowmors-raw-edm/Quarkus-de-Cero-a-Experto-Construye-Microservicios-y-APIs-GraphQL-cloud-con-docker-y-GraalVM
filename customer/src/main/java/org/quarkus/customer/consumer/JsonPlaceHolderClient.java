package org.quarkus.customer.consumer;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.quarkus.customer.entities.ProductEntity;

import java.util.List;

@Path("/product")
@RegisterRestClient(configKey = "products-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface JsonPlaceHolderClient {

    @GET
    @Path("/{id}")
    Uni<ProductEntity> getProductById(@PathParam("id") long id);

    @GET
    Uni<List<ProductEntity>> getAllProducts();
}
