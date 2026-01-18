package org.quarkus.customer.web;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.quarkus.customer.consumer.JsonPlaceHolderClient;
import org.quarkus.customer.entities.CustomerEntity;
import org.quarkus.customer.entities.ProductEntity;
import org.quarkus.customer.repositories.CustomerRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "user-api")
public class CustomerApi {

    @Inject
    Vertx vertx;

    @Inject
    CustomerRepository repository;

     @Inject
     @RestClient
    JsonPlaceHolderClient jsonPlaceHolderClient;

    @POST
    @Path("/save")
    public CustomerEntity save(CustomerEntity customerEntity) {
        repository.createCustomer(customerEntity);
        return customerEntity;
    }

    @GET
    @Path("/find-all")
    public List<CustomerEntity> findAll() {
        return repository.listCustomers();
    }

    @GET
    @Path("/find-by/{id}")
    public CustomerEntity findById(@PathParam("id") Long id) {
        return repository.findCustomerById(id);
    }

    /*@GET
    @Path("/find-by/{id}/product")
    @Blocking
    public CustomerEntity findByIdProduct(@PathParam("id") Long id) {
        return Uni.combine().all().unis(getCustomerReactive(id), getAllProducts())
                .combinedWith((v1,v2) -> {
                    v1.getProductEntities().forEach(p->{
                        v2.forEach(p2->{
                            if(p.getId().equals(p2.getId())) {
                                p.setName(p2.getName());
                                p.setDescription(p2.getDescription());
                            }
                        });
                    });
                    return v1;
                });
    }*/


    @GET
    @Path("/find-by/{id}/product")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<CustomerEntity> findByIdProduct(@PathParam("id") Long id) {
        return Uni.combine().all().unis(getCustomerReactive(id), getAllProducts())
                .with((customer, externalProducts) -> {
                    if (customer.getProductEntities() == null || customer.getProductEntities().isEmpty()) {
                        return customer;
                    }

                    // Indexa productos externos por id para O(1)
                    Map<Long, ProductEntity> extById = externalProducts.stream()
                            .collect(Collectors.toMap(ProductEntity::getId, Function.identity(), (a, b) -> a));

                    // Enriquecemos los productos del customer
                    customer.getProductEntities().forEach(p -> {
                        ProductEntity ext = extById.get(p.getId());
                        if (ext != null) {
                            p.setName(ext.getName());
                            p.setDescription(ext.getDescription());
                        }
                    });

                    return customer;
                });
    }



    @PUT
    @Path("/update")
    public CustomerEntity update(CustomerEntity customerEntity) {
        repository.update(customerEntity);
        return customerEntity;
    }

    @DELETE
    @Path("/delete/{id}")
    public void deleteById(@PathParam("id") Long id) {
        repository.delete(id);
    }

    @GET
    @Path("/product/{id}")
    public Uni<ProductEntity> getProductById(@PathParam("id") Long id) {
        return jsonPlaceHolderClient.getProductById(id);
    }

    @GET
    @Path("/stream")
    public Uni<List<ProductEntity>> stream() {
        return jsonPlaceHolderClient.getAllProducts();
    }

    private Uni<CustomerEntity> getCustomerReactive(Long id) {

        CustomerEntity customerEntity = repository.findCustomerById(id);
        Uni<CustomerEntity> item = Uni.createFrom().item(customerEntity);
        return item;
    }

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(CustomerApi.class);

    /*private Uni<List<ProductEntity>> getAllProducts(){
        jsonPlaceHolderClient.getAllProducts()
                .onFailure().invoke(res -> LOG.error("Error recuperando productos ", res))
                .onItem().transform(res -> {
                    List<ProductEntity> lista = new ArrayList<>();
                    JsonArray objects = res.bodyAsJsonArray();
                    objects.forEach(p -> {
                        LOG.info("See Objects: " + objects);
                        ObjectMapper objectMapper = new ObjectMapper();
                        // Pass JSON string and the POJO class
                        ProductEntity product = null;
                        try {
                            product = objectMapper.readValue(p.toString(), ProductEntity.class);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        lista.add(product);
                    });
                    return lista;
                });
        return null;
    }*/


    private Uni<List<ProductEntity>> getAllProducts() {
        return jsonPlaceHolderClient.getAllProducts()
                .ifNoItem().after(Duration.ofSeconds(5)).fail()     // timeout lógico
                .onFailure().invoke(t -> LOG.error("Error recuperando productos", t))
                .onFailure().transform(t -> new WebApplicationException(
                        "No se pudo obtener productos: " + t.getMessage(),
                        t,
                        Response.Status.BAD_GATEWAY))
                .onItem().ifNull().continueWith(List::of);          // normaliza nulos a lista vacía
    }

}
