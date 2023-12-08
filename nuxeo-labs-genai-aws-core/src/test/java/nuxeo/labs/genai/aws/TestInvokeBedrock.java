package nuxeo.labs.genai.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy("nuxeo.labs.genai.aws.nuxeo-labs-genai-aws-core")
public class TestInvokeBedrock {
    
    public static int countPeriods(String text) {
        int count = 0;
        int index = text.indexOf('.');
        while (index != -1) {
            count++;
            index = text.indexOf('.', index + 1);
        }
        return count;
    }

    @Test
    public void shouldInvokeBedrockWithTitanSimple() {
        
        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_TITAN_TEXT_EXPRESS_V1);
        String result = ibr.run("What is the distance between the Earth and the Moon, in kilometers?", null, null);
        assertNotNull(result);
        assertTrue(result.indexOf("384,400") > -1);
    }
    
    @Test
    public void shouldSummarizeWithTitan() throws Exception {

        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
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
    public void shouldInvokeBedrockWithClaudeInstantv1Simple() throws Exception {
        
        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        //String result = ibr.run("What is the distance between the Earth and the Moon, in kilometers?", null, null);
        String result = ibr.run("What is the distance between the Earth and the Moon, in kilometers?", null, null);
        assertNotNull(result);
        assertTrue(result.indexOf("384,400") > -1);
    }

    @Test
    public void shouldInvokeBedrockWithClaudeInstantv1SimpleFR() {
        
        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run("Quelle est la distance de la terre à la lune, en kilometres ?", null, null);
        assertNotNull(result);
        assertTrue(result.indexOf("384 400") > -1);
    }
    
    @Test
    public void shouldSummarizeWithClaudeInstantv1FR() throws Exception {

        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        File testFile = FileUtils.getResourceFileFromContext("to-summarize-1-FR.txt");
        Blob b = new FileBlob(testFile);
        String prompt = "Merci de résumer ce texte:\n\n" + b.getString();

        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run(prompt, null, null);
        assertNotNull(result);
        
        //File f = new File("/Users/thibaud/Downloads/test-" + Math.random() + ".txt");
        //org.apache.commons.io.FileUtils.writeStringToFile(f, result, Charset.defaultCharset(), false);
        
        assertTrue(b.getString().length() > result.length());
        
    }
    
    @Test
    public void shouldSummarizeFRPdf() throws Exception {

        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        File testFile = FileUtils.getResourceFileFromContext("JavaScript-Wikipedia-FR.pdf");
        Blob blob = new FileBlob(testFile);
        blob.setMimeType("application/pdf");
        
        String prompt = "Merci de résumer ce texte:\n";
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run(prompt, blob, null);
        assertNotNull(result);
        
        //File f = new File("/Users/thibaud/Downloads/test-" + Math.random() + ".txt");
        //org.apache.commons.io.FileUtils.writeStringToFile(f, result, Charset.defaultCharset(), false);
        String originalText = InvokeBedrock.blobToText(blob);
        assertTrue(originalText.length() > result.length());
        
    }

    
    @Test
    public void shouldSummarizeFRPdf2Sentences() throws Exception {

        Assume.assumeTrue("Access to AWS not set. Not doing the test",
                           StringUtils.isNotBlank(System.getenv("AWS_ACCESS_KEY_ID")));
        
        File testFile = FileUtils.getResourceFileFromContext("JavaScript-Wikipedia-FR.pdf");
        Blob blob = new FileBlob(testFile);
        blob.setMimeType("application/pdf");
        
        String prompt = "Merci de résumer ce texte en 2 phrases:\n";
        InvokeBedrock ibr = new InvokeBedrock();
        ibr.setModelId(InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1);
        String result = ibr.run(prompt, blob, null);
        assertNotNull(result);
        
        //File f = new File("/Users/thibaud/Downloads/test-" + Math.random() + ".txt");
        //org.apache.commons.io.FileUtils.writeStringToFile(f, result, Charset.defaultCharset(), false);
        String originalText = InvokeBedrock.blobToText(blob);
        assertTrue(originalText.length() > result.length());
        
        // How to count the number of phrase...
        // So far, the result starts with "Voici un résumé [etc] and end with a column.
        // => just counting the number of "."
        assertEquals(2, countPeriods(result));
        
    }
}
