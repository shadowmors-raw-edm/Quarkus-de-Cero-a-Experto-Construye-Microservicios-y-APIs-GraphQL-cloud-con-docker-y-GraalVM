package quarkus_web_03.resource;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import quarkus_web_03.entity.Contact;

import java.util.List;

@Path("/api/contact")
@Produces("application/json")
@Consumes
public class ContactResource {

    @Inject
    EntityManager entityManager;

    @POST
    @Transactional
    public Response createContact(Contact contact) {
        entityManager.persist(contact);
        return Response.status(Response.Status.CREATED).entity(contact).build();
    }

    @GET
    public List<Contact> getAllContacts(){
        List<Contact> fromContact = entityManager.createQuery("from Contact", Contact.class).getResultList();
        return fromContact;
    }

    @GET
    @Path("/{id}")
    public Response getContactById(@PathParam("id") Long id) {
        Contact contact = entityManager.find(Contact.class, id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(contact).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateContact(@PathParam("id") Long id, Contact updatedContact) {
        Contact contact = entityManager.find(Contact.class, id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contact.setName(updatedContact.getName());
        contact.setEmail(updatedContact.getEmail());
        entityManager.merge(contact);
        return Response.ok(contact).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteContact(@PathParam("id") Long id) {
        Contact contact = entityManager.find(Contact.class, id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        entityManager.remove(contact);
        return Response.noContent().build();
    }

    @PUT
    @Path("/email/{email}")
    @Transactional
    public Response updateContactByEmail(@PathParam("email") String email, Contact updatedContact) {
        Contact contact = entityManager.createQuery("SELECT c FROM Contact c WHERE c.email = :email", Contact.class)
                .setParameter("email", email)
                .getSingleResult();
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contact.setName(updatedContact.getName());
        contact.setEmail(updatedContact.getEmail());
        entityManager.merge(contact);
        return Response.ok(contact).build();
    }

    @GET
    @Path("/email/{email}")
    public Response getContactByEmail(@PathParam("email") String email) {
        Contact contact = entityManager.createQuery("SELECT c FROM Contact c WHERE c.email = :email", Contact.class)
                .setParameter("email", email)
                .getSingleResult();
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(contact).build();
    }

    @GET
    @Path("/search")
    public Response searchContacts(@QueryParam("email") String email) {
        List<Contact> contacts = entityManager.createQuery("SELECT c FROM Contact c WHERE c.email LIKE :email", Contact.class)
                .setParameter("email", "%" + email + "%")
                .getResultList();
        return Response.ok(contacts).build();
    }
}
