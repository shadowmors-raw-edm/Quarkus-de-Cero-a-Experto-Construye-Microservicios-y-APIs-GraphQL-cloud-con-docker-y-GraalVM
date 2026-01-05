package rest_client_02;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@Path("/extensions02")
public class ExtensionInfoApi {

    @ConfigProperty(name = "grettings")
    private String grettings;

    @Inject
    ExtensionInfo extensionInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<PersonRestClient.Extension> getExtensionInfo() {
        return extensionInfo.doSomething();
    }

    @GET
    @Path("/02")
    @Produces(MediaType.APPLICATION_JSON)
    public String getExtensionInfoToString() {
        Set<PersonRestClient.Extension> extensions = extensionInfo.doSomething();

        StringBuilder sb = new StringBuilder();
        for (PersonRestClient.Extension extension : extensions) {
            sb.append("ID: ").append(extension.id).append("\n")
                    .append("Name: ").append(extension.name).append("\n")
                    .append("shortName: ").append(extension.shortName).append("\n")
                    .append("Keywords: ").append(extension.keywords).append("\n");
        }
        return sb.toString();
    }

    @GET
    @Path("/custom/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCustomName(@PathParam("name") String name) {

        return "Hello " + name;
    }

    @GET
    @Path("/custom-properties/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCustom(@PathParam("name") String name) {

        return grettings + name + ", como estas?";
    }

}
