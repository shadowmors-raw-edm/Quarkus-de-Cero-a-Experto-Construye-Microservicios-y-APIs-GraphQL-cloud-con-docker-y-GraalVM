package rest_client_02;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Set;

@ApplicationScoped
public class ExtensionInfo {

    @Inject
     @RestClient
    PersonRestClient personRestClient;

    public Set<PersonRestClient.Extension> doSomething() {
        Set<PersonRestClient.Extension> restClientExtension = personRestClient
                .getExtensionsByID("io.quarkus:quarkus-hibernate-validator");

        restClientExtension.forEach(extension -> {

            System.out.println("Extension ID: " + extension.id);
            System.out.println("Extension name: " + extension.name);
        });
        return restClientExtension;
    }
}
