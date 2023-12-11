package nuxeo.labs.genai.aws.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.mimetype.service.MimetypeRegistryService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import nuxeo.labs.genai.aws.InvokeBedrock;
import nuxeo.labs.genai.aws.TestUtils;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.labs.genai.aws.nuxeo-labs-genai-aws-core")
/*
 * Notice: This class does not run any test (skipped) if AWS_nnn env. variables are not set. (si @Before)
 */
public class TestBedrockOperations {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;
    
    @Before
    public void setUpAndCheckAWS() {
        
        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
    }

    @Test
    public void shouldSummarizeInENWithTitanAndNoDoc() throws Exception {

        File testFile = FileUtils.getResourceFileFromContext("to-summarize-1.txt");
        Blob b = new FileBlob(testFile);
        String toSummarize = b.getString();
        
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("modelId", InvokeBedrock.MODEL_TITAN_TEXT_EXPRESS_V1);
        params.put("awsRegion", "us-east-1");
        params.put("text", toSummarize);
        params.put("language", "en");
        
        Blob result = (Blob) automationService.run(ctx, BedrockSummarizeOp.ID, params);
        assertNotNull(result);
        
        assertEquals("text/plain", result.getMimeType());
        
        String summary = result.getString();
        assertNotNull(summary);
        
        assertTrue(summary.length() < toSummarize.length());
    }

    @Test
    public void shouldSummarizeInFRWithClaudeAndNoDoc() throws Exception {
        

        File testFile = FileUtils.getResourceFileFromContext("to-summarize-1-FR.txt");
        Blob b = new FileBlob(testFile);
        String toSummarize = b.getString();
        
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("modelId", InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        params.put("awsRegion", "us-east-1");
        params.put("text", toSummarize);
        params.put("language", "fr");
        
        Blob result = (Blob) automationService.run(ctx, BedrockSummarizeOp.ID, params);
        assertNotNull(result);
        
        assertEquals("text/plain", result.getMimeType());
        
        String summary = result.getString();
        assertNotNull(summary);
        
        assertTrue(summary.length() < toSummarize.length());
    }
    
    @Test
    public void shouldSummarizeAWordInFRWithClaudeAndADocIn2Sentences() throws Exception {
        
        DocumentModel doc = session.createDocumentModel("/", "test", "File");
        File testFile = FileUtils.getResourceFileFromContext("JavaScript-Wikipedia-FR.docx");
        Blob blob = new FileBlob(testFile);
        MimetypeRegistryService service = (MimetypeRegistryService) Framework.getService(MimetypeRegistry.class);
        String mimeType = service.getMimetypeFromFilename(blob.getFilename());
        blob.setMimeType(mimeType);
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);
        
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(doc);
        
        Map<String, Object> params = new HashMap<>();
        params.put("modelId", InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        params.put("awsRegion", "us-east-1");
        params.put("xpath", "file:content");
        params.put("language", "fr");
        params.put("numberOfSentences", 2);
        
        Blob result = (Blob) automationService.run(ctx, BedrockSummarizeOp.ID, params);
        assertNotNull(result);
        
        assertEquals("text/plain", result.getMimeType());
        
        String summary = result.getString();
        assertNotNull(summary);
        
        // How to count the number of phrase...
        // So far, the result starts with "Voici un résumé [etc] and end with a column.
        // => just counting the number of "."
        assertEquals(2, TestUtils.countPeriods(summary));
    }
}
