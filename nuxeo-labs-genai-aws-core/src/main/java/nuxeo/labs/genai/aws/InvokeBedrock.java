package nuxeo.labs.genai.aws;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.runtime.api.Framework;

import nuxeo.labs.genai.aws.models.AWSTitan;
import nuxeo.labs.genai.aws.models.AnthropicClaude;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

/**
 * Also see AWS documentation of course. Such as:
 * About prompts : https://docs.aws.amazon.com/bedrock/latest/userguide/prompt-engineering-guidelines.html
 * <br>
 * Warning: The code adds the required tokens. For example, when using Claude, it makes the prompt starts with "Human:"
 * and ends with "Assistant:"
 * 
 * @since 2023
 */
public class InvokeBedrock {

    private static final Logger log = LogManager.getLogger(InvokeBedrock.class);

    public static final Region DEFAULT_REGION = Region.US_EAST_1;

    protected Region region = DEFAULT_REGION;

    // Reminder: as of today (end 2023), Titan is EN only:
    // "Sorry, this model is only accessible for English only applications. Please consider revising your content to be
    // in English."
    public static final String MODEL_TITAN_TEXT_EXPRESS_V1 = "amazon.titan-text-express-v1";

    public static final String MODEL_ANTHROPIC_CLAUDE_INSTANT_V1 = "anthropic.claude-instant-v1";

    public static final String MODEL_ANTHROPIC_CLAUDE_V2 = "anthropic.claude-v2";

    public static final String DEFAULT_MODEL = MODEL_ANTHROPIC_CLAUDE_INSTANT_V1;// MODEL_TITAN_TEXT_EXPRESS_V1;// ;

    protected String modelId = DEFAULT_MODEL;

    protected String prompt;

    protected Blob sourceBlob;

    public static final float DEFAULT_TEMPERATURE = 0.8F;

    // Use a lower value to decrease randomness in the response.
    protected float temperature = DEFAULT_TEMPERATURE;

    // "Use a lower value to ignore less probable options. => 0-1"
    protected Float topP = null;

    // "Specify the maximum number of tokens in the generated response"
    // Titan: 0-8000, default 512
    // Claude: 0-4096, default 200. "We recommend a limit of 4,000 tokens for optimal performance"
    protected Integer responseMaxTokenCount = null;

    protected ArrayList<String> stopSequences = null;

    BedrockRuntimeClient bedrockRuntime = null;

    public InvokeBedrock() {
        getBedrockRuntime();
    }

    public InvokeBedrock(Region region, String modelId) {
        getBedrockRuntime();
        setRegion(region);
        setModelId(modelId);
    }

    public InvokeBedrock(Region region, String modelId, Float temperature, Float topP, Integer responseMaxTokens) {
        getBedrockRuntime();
        setRegion(region);
        setModelId(modelId);
        setTemperature(temperature);
        setTopP(topP);
        setResponseMaxTokenCount(responseMaxTokens);
    }

    protected void getBedrockRuntime() {
        if (bedrockRuntime == null) {
            bedrockRuntime = BedrockRuntimeClient.builder().region(region).build();
        }
    }

    public void setRegion(Region region) {
        if (region != null) {
            this.region = region;
        } else {
            log.warn("region is null => not changing it, using " + this.region);
        }
    }

    // null => model will use a default value
    public void setResponseMaxTokenCount(Integer value) {
        responseMaxTokenCount = value;
    }

    // null => model will use a default value
    public void setTopP(Float value) {
        topP = value;
    }

    public void setModelId(String modelID) {
        if (StringUtils.isNotBlank(modelId)) {
            this.modelId = modelID;
        } else {
            log.warn("modelID is blank => not changing it, using " + this.modelId);
        }
    }

    public void setTemperature(float value) {
        temperature = value;
    }

    public void setStopSequences(ArrayList<String> values) {
        stopSequences = values;
    }

    public void setStopSequences(String... values) {
        if (values.length == 0) {
            stopSequences = null;
        } else {
            stopSequences = new ArrayList<>(Arrays.asList(values));
        }

    }

    /**
     * Extract the text from a blob (typically, pdf, Word), with punctuation
     * 
     * @param blob
     * @return the text in the blob, without styling or formatting.
     * @since 2023
     */
    public static String blobToText(Blob blob) {

        try {
            ConversionService conversionService = Framework.getService(ConversionService.class);
            BlobHolder blobHolder = conversionService.convert("any2text", new SimpleBlobHolder(blob), null);
            Blob resultBlob = blobHolder.getBlob();
            String string;
            string = resultBlob.getString();
            // strip '\0 chars from text, if any
            if (string.indexOf('\0') >= 0) {
                string = string.replace("\0", " ");
            }
            return string;
        } catch (ConversionException | IOException e) {
            throw new NuxeoException("Error extracting text from blob", e);
        }

    }
    
    protected boolean looksLikeTitan() {
        if(StringUtils.isNotBlank(modelId)) {
            return modelId.startsWith("amazon.titan");
        }
        
        return false;
    }
    
    protected boolean looksLikeClaude() {
        if(StringUtils.isNotBlank(modelId)) {
            return modelId.startsWith("anthropic.claude");
        }
        
        return false;
    }

    /**
     * If blob is not null, its text is extracted and appended to the prompt.
     * <br>
     * If {@code insertInPromptReplaceTag} is not {@code null} (and not empty), the text of the blob will replace it.
     * <br>
     * So, for example, if the prompt is "Answer the question which is between <text> and </text> tags\n\nBlahblah some
     * context\n\n<text>{HERE_TEXT}</text>\nmore context here\n\n
     * <br>
     * => You would then call InvokeBedrock("Answer the question which is between...", blob, "{HERE_TEXT}");
     * 
     * @param prompt
     * @param blob
     * @param insertInPromptReplaceTag
     * @return
     * @since 2023
     */
    public String run(String prompt, Blob blob, String insertInPromptReplaceTag) {

        if (blob != null) {
            String blobText = blobToText(blob);
            if (StringUtils.isNotBlank(insertInPromptReplaceTag)) {
                prompt = prompt.replace(insertInPromptReplaceTag, blobText);
            } else {
                prompt += "\n\n" + blobText;
            }
        }
        this.prompt = prompt;

        JSONObject jsonBody = getRequestBodyForModel();

        SdkBytes body = SdkBytes.fromUtf8String(jsonBody.toString());

        // System.out.println("====================\nINVOKING MODEL " + modelId);
        InvokeModelRequest request = InvokeModelRequest.builder().modelId(modelId).body(body).build();

        InvokeModelResponse response = bedrockRuntime.invokeModel(request);
        
        String result = getStringResultForModel(response);

        return result;
    }

    protected JSONObject getRequestBodyForModel() {

        RequestParameters params = new RequestParameters(prompt, temperature, topP, responseMaxTokenCount,
                stopSequences);

        switch (modelId) {
        case MODEL_TITAN_TEXT_EXPRESS_V1:
            AWSTitan titan = new AWSTitan();
            return titan.getRequestBody(params);

        case MODEL_ANTHROPIC_CLAUDE_INSTANT_V1:
        case MODEL_ANTHROPIC_CLAUDE_V2:
            AnthropicClaude claude = new AnthropicClaude();
            return claude.getRequestBody(params);

        default:
            throw new IllegalArgumentException("Model shoud be Titan or Anthropic-Claude, but is " + modelId);
        }

    }

    protected String getStringResultForModel(InvokeModelResponse response) {
        
        if(looksLikeTitan()) {
            AWSTitan titan = new AWSTitan();
            return titan.getStringResult(response);
        }
        
        if(looksLikeClaude()) {
            AnthropicClaude claude = new AnthropicClaude();
            return claude.getStringResult(response);
        }

        throw new IllegalArgumentException("Model shoud be AWS-Titan or Anthropic-Claude. It is " + modelId);
    }
}
