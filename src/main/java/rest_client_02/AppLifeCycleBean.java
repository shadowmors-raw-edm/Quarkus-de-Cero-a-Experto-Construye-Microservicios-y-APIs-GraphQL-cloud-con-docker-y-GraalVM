package rest_client_02;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.logging.Logger;

@ApplicationScoped
public class AppLifeCycleBean {

    private  final Logger log = Logger.getLogger(AppLifeCycleBean.class.getName());

    void onStart(@Observes StartupEvent ev) {
        log.info("onStart application started");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("onStop application started");
    }
}
