package org.quarkus.customer.web;



import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestPath;
import org.quarkus.customer.consumer.JsonPlaceHolderClient;
import org.quarkus.customer.entities.Customer;
import org.quarkus.customer.entities.Product;
import org.quarkus.customer.repositories.CustomerRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "user-api")
public class CustomerApi {

     @Inject
     @RestClient
    JsonPlaceHolderClient jsonPlaceHolderClient;

     @Inject
     CustomerRepository repository;

    @POST
    @Path("/save")
    public Uni<Response> save(Customer customer) {
        return Panache.withTransaction(customer::persist)
                .replaceWith(Response.ok(customer).status(Response.Status.CREATED)::build);
    }

    @GET
    @Path("/find-all")
    public Uni<List<Customer>> findAll() {
        return Customer.listAll(Sort.by("names"));
    }

    @GET
    @Path("/find-all-repository")
    public Uni<List<Customer>> findAllUsingRepository() {
        return repository.listAll();
    }

    @GET
    @Path("/find-by/{id}")
    public Uni<Customer> findById(@PathParam("id") Long id) {
        return Customer.findById(id);
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
    public Uni<Customer> findByIdProduct(@PathParam("id") Long id) {
        return Uni.combine().all().unis(getCustomerReactive(id), getAllProducts())
                .with((customer, externalProducts) -> {
                    if (customer.getProductEntities() == null || customer.getProductEntities().isEmpty()) {
                        return customer;
                    }

                    // Indexa productos externos por id para O(1)
                    Map<Long, Product> extById = externalProducts.stream()
                            .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

                    // Enriquecemos los productos del customer
                    customer.getProductEntities().forEach(p -> {
                        Product ext = extById.get(p.getId());
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
    public Uni<Response> update(@RestPath Long id, Customer c) {
        if (c == null || c.getCode() == null) {
            throw new WebApplicationException("Product code was not set on request.", HttpResponseStatus.UNPROCESSABLE_ENTITY.code());
        }
        return Panache
                .withTransaction(() -> Customer.<Customer> findById(id)
                        .onItem().ifNotNull().invoke(entity -> {
                            entity.setNames(c.getNames());
                            entity.setAccountNumber(c.getAccountNumber());
                            entity.setCode(c.getCode());
                        })
                )
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("/delete/{id}")
    public Uni<Response> deleteById(@PathParam("id") Long id) {
        return Panache.withTransaction(() -> Customer.deleteById(id))
                .map(deleted -> deleted
                        ? Response.ok().status(Response.Status.NO_CONTENT).build()
                        : Response.ok().status(NOT_FOUND).build());
    }

    @GET
    @Path("/product/{id}")
    public Uni<Product> getProductById(@PathParam("id") Long id) {
        return jsonPlaceHolderClient.getProductById(id);
    }

    @GET
    @Path("/stream")
    public Uni<List<Product>> stream() {
        return jsonPlaceHolderClient.getAllProducts();
    }

    private Uni<Customer> getCustomerReactive(Long id) {

        Customer customer = (Customer) Customer.findById(id);
        Uni<Customer> item = Uni.createFrom().item(customer);
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


    private Uni<List<Product>> getAllProducts() {
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
