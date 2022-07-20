package net.catenax.edc.cp.adapter;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.catenax.edc.cp.adapter.dto.ProcessData;
import net.catenax.edc.cp.adapter.messaging.Channel;
import net.catenax.edc.cp.adapter.messaging.Message;
import net.catenax.edc.cp.adapter.messaging.MessageService;
import net.catenax.edc.cp.adapter.service.ResultService;

import java.util.Objects;

@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Path("/adapter/asset")
public class HttpController {
    private final ResultService resultService;
    private final MessageService messageService;

    public HttpController(ResultService resultService, MessageService messageService) {
        this.resultService = resultService;
        this.messageService = messageService;
    }

    @GET
    @Path("sync/{assetId}")
    public Response getAssetSynchronous(
            @PathParam("assetId") String assetId,
            @QueryParam("providerUrl") String providerUrl) {

        if (invalidParams(assetId, providerUrl)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("AssetId or providerUrl is empty!")
                    .build();
        }

        String traceId = initiateProcess(assetId, providerUrl);

        try {
            return Response
                    .status(Response.Status.OK)
                    .entity(resultService.poll(traceId))
                    .build();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private boolean invalidParams(String assetId, String providerUrl) {
        return Objects.isNull(assetId) || assetId.isBlank() ||
                Objects.isNull(providerUrl) || providerUrl.isBlank();
    }

    private String initiateProcess(String assetId, String providerUrl) {
        ProcessData processData = new ProcessData(assetId, providerUrl);
        Message message = new Message(processData);
        String traceId = message.getTraceId();
        messageService.send(Channel.INITIAL, message);
        return traceId;
    }

    // TODO add async request handler
//    @GET
//    @Path("/{assetId}")
//    public Response getAsset(@PathParam("assetId") String assetId,
//                             @QueryParam("providerUrl") String providerUrl) {
//        EndpointDataReference result = resultService.poll(assetId, 1, TimeUnit.MILLISECONDS);
//        if (result == null) {
//            String traceId = initiateProcess(assetId, providerUrl);
//            // start search, create result placeholder
//            // return 202
//        }
//
//        if (result != null && result.data() == null) {
//            //return 202
//        }
//
//        return result.data(); // 200
//    }
}
