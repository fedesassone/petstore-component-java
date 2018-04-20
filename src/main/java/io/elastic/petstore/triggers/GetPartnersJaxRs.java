package io.elastic.petstore.triggers;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import io.elastic.petstore.Constants;

/**
 * Trigger to get partners from db.
 */
public class GetPartnersJaxRs implements Module {
    private static final Logger logger = LoggerFactory.getLogger(GetPartnersJaxRs.class);

    /**
     * Executes the trigger's logic by sending a request to the local connector and emitting a response to the platform.
     *
     * @param parameters execution parameters
     */
    @Override
    public void execute(final ExecutionParameters parameters) {
        final JsonObject configuration = parameters.getConfiguration();

        // access the value of the apiKey field defined in credentials section of component.json
        final JsonString apiKey = configuration.getJsonString("apiKey");
        if (apiKey == null) {
            throw new IllegalStateException("apiKey is required");
        }

      logger.info("About to retrieve partners");

        final JsonArray partners = ClientBuilder.newClient()
                .target(Constants.DB_API_BASE_URL)
                .path(Constants.SELECT_ALL_PARTNERS_PATH)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(Constants.API_KEY_HEADER, apiKey.getString())
                .get(JsonArray.class);

      logger.info("Got {} partners", partners.size());

        // emitting naked arrays is forbidden by the platform
        final JsonObject body = Json.createObjectBuilder()
                .add("partners", partners)
                .build();

        final Message data
                = new Message.Builder().body(body).build();

        logger.info("Emitting data");

        // emitting the message to the platform
        parameters.getEventEmitter().emitData(data);
    }
}
