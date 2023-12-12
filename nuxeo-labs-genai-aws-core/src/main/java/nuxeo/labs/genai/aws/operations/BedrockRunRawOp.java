package nuxeo.labs.genai.aws.operations;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

import nuxeo.labs.genai.aws.InvokeBedrock;
import nuxeo.labs.genai.aws.RequestParameters;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

/**
 *
 */
@Operation(id = BedrockRunRawOp.ID, category = Constants.CAT_SERVICES, label = "Bedrock: Run Raw", description = ""
        + "Run a model, return the java InvokeModelResponse"
        + " modelId is required. Default is anthropic.claude-instant-v1. IMPORTANT: We support only Anthropic Claude and AWS Titan for now."
        + " awsRegion is required. Default is us-)east-1."
        + " requestBody is a JSON string containing all and everything expected by the model. Caller is in charge of filling it with the values"
        + " (with correct field names and types) as expected by the model (see the model documentation on AWS). For example, if a blob must be"
        + " added to a prompt, caller must do it itself (typically, converting it to text using the any2pdf converter)."
        + " It returns the InvokeModelResponse as received, caller is in charge of using the misc. InvokeModelResponse to get the details."
        + " This operation allows for calling a model not yet supported by the plugin in terms of eas-of-use"
        + " (formating the request, checking the result, etc.)"
        + " because it returns the raw InvokeModelResponse, this operation should not be called from the frontend which can't handle Java objects.")
public class BedrockRunRawOp {

    public static final String ID = "Bedrock.RunRaw";

    @Context
    protected CoreSession session;

    @Param(name = "modelId", required = true, values = { InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1 })
    protected String modelId = InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1;

    @Param(name = "awsRegion", required = true, values = { "us-east-1" })
    protected String awsRegion = "us-east-1";

    @Param(name = "requestBody", required = true)
    protected String requestBody;

    @OperationMethod
    public InvokeModelResponse run() {
        
        JSONObject requestBodyJson = new JSONObject(requestBody);

        InvokeBedrock ibr = new InvokeBedrock(Region.of(awsRegion), modelId);
        InvokeModelResponse response = ibr.run(requestBodyJson);
        
        return response;
    }
}
