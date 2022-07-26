package net.catenax.edc.cp.adapter;

import static java.util.Objects.isNull;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import net.catenax.edc.cp.adapter.dto.ProcessData;
import net.catenax.edc.cp.adapter.messaging.Channel;
import net.catenax.edc.cp.adapter.messaging.Message;
import net.catenax.edc.cp.adapter.messaging.MessageService;
import net.catenax.edc.cp.adapter.service.ResultService;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/adapter/asset")
@RequiredArgsConstructor
public class HttpController {
  private final Monitor monitor;
  private final ResultService resultService;
  private final MessageService messageService;

  @GET
  @Path("sync/{assetId}")
  public Response getAssetSynchronous(
      @PathParam("assetId") String assetId, @QueryParam("providerUrl") String providerUrl) {

    if (invalidParams(assetId, providerUrl)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("AssetId or providerUrl is empty!")
          .build();
    }

    String traceId = initiateProcess(assetId, providerUrl);

    try {
      return Response.status(Response.Status.OK).entity(resultService.poll(traceId)).build();
    } catch (InterruptedException e) {
      monitor.severe("InterruptedException", e);
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  private boolean invalidParams(String assetId, String providerUrl) {
    return isNull(assetId) || assetId.isBlank() || isNull(providerUrl) || providerUrl.isBlank();
  }

  private String initiateProcess(String assetId, String providerUrl) {
    ProcessData processData = new ProcessData(assetId, providerUrl);
    Message message = new Message(processData);
    messageService.send(Channel.INITIAL, message);
    return message.getTraceId();
  }
}
