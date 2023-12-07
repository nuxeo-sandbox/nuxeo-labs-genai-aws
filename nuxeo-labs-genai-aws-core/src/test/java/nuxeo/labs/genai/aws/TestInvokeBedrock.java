package nuxeo.labs.genai.aws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy("nuxeo.labs.genai.aws.nuxeo-labs-genai-aws-core")
public class TestInvokeBedrock {

    @Test
    public void shouldInvokeBedrockWithTitanSimple() {
        
        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_TITAN_TEXT_EXPRESS_V1);
        String result = ibr.run("What is the distance between the Earth and the Moon, in kilometers?", null);
        assertNotNull(result);
        assertTrue(result.indexOf("384,400") > -1);
    }
    
    @Test
    public void shouldSummarizeWithTitan() throws Exception {

        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        File testFile = FileUtils.getResourceFileFromContext("to-summarize-1.txt");
        Blob b = new FileBlob(testFile);
        String prompt = "Please summarize this text:\n" + b.getString();

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_TITAN_TEXT_EXPRESS_V1);
        String result = ibr.run(prompt, null);
        assertNotNull(result);
        
        assertTrue(b.getString().length() > result.length());
        
        
    }
    
    @Ignore
    @Test
    // "Sorry, this model is only accessible for English only applications. Please consider revising your content to be in English."
    public void shouldSummarizeWithTitanInFR() throws Exception {

        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        File testFile = FileUtils.getResourceFileFromContext("to-summarize-1-FR.txt");
        Blob b = new FileBlob(testFile);
        String prompt = "Please summarize this text:\n" + b.getString();

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_TITAN_TEXT_EXPRESS_V1);
        String result = ibr.run(prompt, null);
        assertNotNull(result);
        
        assertTrue(b.getString().length() > result.length());
        
        
    }

    @Ignore
    @Test
    // "Sorry, this model is only accessible for English only applications. Please consider revising your content to be in English."
    public void shouldInvokeBedrockWithTitanSimpleInFR() {

        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_TITAN_TEXT_EXPRESS_V1);
        String result = ibr.run("Quelle est la distance de la terre à la lune, en kilometres ?", null);
        assertNotNull(result);
        assertTrue(result.indexOf("384,400") > -1);
    }
}
