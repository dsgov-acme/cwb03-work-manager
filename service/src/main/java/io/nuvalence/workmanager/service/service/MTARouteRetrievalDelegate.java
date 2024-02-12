package io.nuvalence.workmanager.service.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Delegate class for retrieving routes.
 */
@Component("MTARouteRetrievalDelegate")
public class MTARouteRetrievalDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // TODO: implement
    }
}
