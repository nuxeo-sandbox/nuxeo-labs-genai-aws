package nuxeo.labs.genai.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.mimetype.service.MimetypeRegistryService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy("nuxeo.labs.genai.aws.nuxeo-labs-genai-aws-core")
/*
 * Notice: This class does not run any test (skipped) if AWS_nnn env. variables are not set. (si @Before)
 */
public class TestInvokeBedrock {

    @Before
    public void setUpAndCheckAWS() {

        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));

    }

    @Test
    public void shouldInvokeBedrockWithTitanSimple() {

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_TITAN_TEXT_EXPRESS_V1);
        String result = ibr.run("What is the distance between the Earth and the Moon, in kilometers?", null, null);
        assertNotNull(result);
        assertTrue(result.indexOf("384,400") > -1);
    }

    @Test
    public void shouldSummarizeWithTitan() throws Exception {

        File testFile = FileUtils.getResourceFileFromContext("to-summarize-1.txt");
        Blob b = new FileBlob(testFile);
        String prompt = "Please summarize this text:\n\n" + b.getString();

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_TITAN_TEXT_EXPRESS_V1);
        String result = ibr.run(prompt, null, null);
        assertNotNull(result);

        assertTrue(b.getString().length() > result.length());
    }

    @Test
    public void shouldSummarizeAnENWordDocWithClaude() throws Exception {

        File testFile = FileUtils.getResourceFileFromContext("JavaScript-Wikipedia-EN.docx");
        Blob blob = new FileBlob(testFile);
        MimetypeRegistryService service = (MimetypeRegistryService) Framework.getService(MimetypeRegistry.class);
        String mimeType = service.getMimetypeFromFilename(blob.getFilename());
        blob.setMimeType(mimeType);
        String prompt = "Please summarize this text:";

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run(prompt, blob, null);
        assertNotNull(result);

        // assertTrue(b.getString().length() > result.length());

    }

    @Test
    public void shouldInvokeBedrockWithClaudeInstantv1Simple() throws Exception {

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        // String result = ibr.run("What is the distance between the Earth and the Moon, in kilometers?", null, null);
        String result = ibr.run("What is the distance between the Earth and the Moon, in kilometers?", null, null);
        assertNotNull(result);
        assertTrue(result.indexOf("384,400") > -1);
    }

    @Test
    public void shouldInvokeBedrockWithClaudeInstantv1SimpleFR() {

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run("Quelle est la distance de la terre à la lune, en kilometres ?", null, null);
        assertNotNull(result);
        assertTrue(result.indexOf("384 400") > -1);
    }

    @Test
    public void shouldSummarizeWithClaudeInstantv1FR() throws Exception {

        File testFile = FileUtils.getResourceFileFromContext("to-summarize-1-FR.txt");
        Blob b = new FileBlob(testFile);
        String prompt = "Merci de résumer ce texte:\n\n" + b.getString();

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run(prompt, null, null);
        assertNotNull(result);

        // File f = new File("/Users/thibaud/Downloads/test-" + Math.random() + ".txt");
        // org.apache.commons.io.FileUtils.writeStringToFile(f, result, Charset.defaultCharset(), false);

        assertTrue(b.getString().length() > result.length());

    }

    @Test
    public void shouldSummarizeFRPdf() throws Exception {

        File testFile = FileUtils.getResourceFileFromContext("JavaScript-Wikipedia-FR.pdf");
        Blob blob = new FileBlob(testFile);
        blob.setMimeType("application/pdf");

        String prompt = "Merci de résumer ce texte:\n";
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run(prompt, blob, null);
        assertNotNull(result);

        // File f = new File("/Users/thibaud/Downloads/test-" + Math.random() + ".txt");
        // org.apache.commons.io.FileUtils.writeStringToFile(f, result, Charset.defaultCharset(), false);
        String originalText = InvokeBedrock.blobToText(blob);
        assertTrue(originalText.length() > result.length());

    }

    @Test
    public void shouldSummarizeFRPdf2Sentences() throws Exception {

        File testFile = FileUtils.getResourceFileFromContext("JavaScript-Wikipedia-FR.pdf");
        Blob blob = new FileBlob(testFile);
        blob.setMimeType("application/pdf");

        String prompt = "Merci de résumer ce texte en 2 phrases:\n";
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run(prompt, blob, null);
        assertNotNull(result);

        // File f = new File("/Users/thibaud/Downloads/test-" + Math.random() + ".txt");
        // org.apache.commons.io.FileUtils.writeStringToFile(f, result, Charset.defaultCharset(), false);
        String originalText = InvokeBedrock.blobToText(blob);
        assertTrue(originalText.length() > result.length());

        // How to count the number of phrase...
        // So far, the result starts with "Voici un résumé [etc] and end with a column.
        // => just counting the number of "."
        // We should have 2 but sometimes, the model returns an intro with a point "this is the summary." :-)
        assertTrue(TestUtils.countPeriods(result) <= 3);

    }

    @Test
    public void shouldSummarizeFRPdf2SentencesInFrankfurt() throws Exception {

        File testFile = FileUtils.getResourceFileFromContext("JavaScript-Wikipedia-FR.pdf");
        Blob blob = new FileBlob(testFile);
        blob.setMimeType("application/pdf");

        String prompt = "Merci de résumer ce texte en 2 phrases:\n";
        InvokeBedrock ibr = new InvokeBedrock(Region.EU_CENTRAL_1, InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run(prompt, blob, null);
        assertNotNull(result);

        File f = new File("/Users/thibaud/Downloads/test-" + Math.random() + ".txt");
        org.apache.commons.io.FileUtils.writeStringToFile(f, result, Charset.defaultCharset(), false);
        String originalText = InvokeBedrock.blobToText(blob);
        assertTrue(originalText.length() > result.length());

        // How to count the number of phrase...
        // So far, the result starts with "Voici un résumé [etc] and end with a column.
        // We should have 2 but sometimes, the model returns an intro with a point "this is the summary." :-)
        assertTrue(TestUtils.countPeriods(result) <= 3);

    }

    @Test
    public void testRunRawSimple() throws Exception {

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        ibr.setRegion(Region.US_EAST_1);

        JSONObject jsonBody = new JSONObject();
        String prompt = "What is the distance between the Earth and the Moon, in kilometers?";
        // Filling fields as expected by Claude
        jsonBody.put("prompt", "Human: " + prompt + "\n\nAssistant: ")
                .put("temperature", 0.7)
                .put("top_p", 0.5)
                .put("max_tokens_to_sample", 500);
        
        InvokeModelResponse response = ibr.run(jsonBody);
        assertNotNull(response);
        
        assertEquals(200, response.sdkHttpResponse().statusCode());
        
        String responseBodyStr = response.body().asUtf8String();
        JSONObject responseBodyJson = new JSONObject(responseBodyStr);
        String responseStr = responseBodyJson.getString("completion");
        assertTrue(responseStr.indexOf("384,400") > -1);
        
    }
}
