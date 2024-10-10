# nuxeo-labs-genai-aws

> [!CAUTION]
> This plugin is discontinued, use [nuxeo-aws-bedrock-connector](https://github.com/nuxeo-sandbox/nuxeo-aws-bedrock-connector) instead

<hr>

This plugin allows for calling Generative AI on AWS, using [Amazon Bedrock](https://docs.aws.amazon.com/bedrock/latest/userguide/what-is-bedrock.html), using Amazon Java SDK.

First Use Cases (Jan. 2024) are about summarizing a text and asking any question. For these purposes, the plugin provides some Automation Operations (see below).

## AWS Setup • Warnings • Known Limitations

### AWS Authentication
The plugin expects the usual AWS environment variables to be set (it does not rely on nuxeo.conf parameters). Notice that if your Nuxeo instance runs on AWS, these are already set for you:

* `AWS_ACCESS_KEY_ID`
* `AWS_SECRET_ACCESS_KEY`
* `AWS_SESSION_TOKEN`
* and `AWS_SESSION_TOKEN`

### Amazon Bedrock Availability

As of December 2023, Amazon Bedrock is supported only in the following regions (see [documentation](https://docs.aws.amazon.com/general/latest/gr/bedrock.html#bedrock_region)):

* us-east-1 (N. Virginia)
* us-west-2 (Oregon)
* ap-southeast-1 (Singapore)
* ap-northeast-1 (Tokyo)
* eu-central-1 (Frankfurt)

ℹ️ The operations accept a `awsRegion` parameter. When a new region is available for Amazon Bedrock, there is no need to release a new version of this plugin, you can just change parameter(s) in your configuration.

It is, of course, your responsibility to [use the AWS Console](https://us-east-1.console.aws.amazon.com/bedrock/home?region=us-east-1#/modelaccess) to activate access to the model(s) you plan to use.


### Supported Models
The plugin has been tested (see unit tests) successfully with:
* `amazon.titan-text-express-v1`,
* `amazon.titan-text-express-v1`
* `anthropic.claude-instant-v1`
* `anthropic.claude-v2`.

Each set of models (text-Titan, text-Claude) has its own input parameters and response format, this is the main reason why we support only these, but feel free to add new ones :-)


> [!IMPORTANT]
> Not all models are supported in all regions: Check models availability with Amazon Bedrock [documentation](https://docs.aws.amazon.com/bedrock/latest/userguide/models-supported.html)


## Operations

### `Bedrock.Summarize` (category Services)
Summarize a text or a blob (text, PDF, Word, any content that can be converted to plain text). Return a `Blob`, `text/plain`. This blob can be previewed in the UI, and its text can be get using the `getString()` method of the blob.

#### input
Can be `void`/`null` or a `Document`.
* If `void`/`null`, the `text` parameter is required and is the content to summarize
* If `input` is a `Document`, then `text` is ignored and `xpath` is required. The blob found at `xpath` is the content to summarize


#### Parameters:
* `modelId`, required. Default is `"anthropic.claude-instant-v1"`. **IMPORTANT** See _Supported Models_ above
* `awsRegion`, required. Default is `"us-east-1"`
* `text`: Required if `input` is `void`/`null`. The text to summarize.
* `xpath`: Required if `input` is a `Document`. The XPath of the blob to summarize
* `language`, required, because it changes the prompt. We support only English ('en') and French ('fr'). For any other language, use `Bedrock.Run`.
* `numberOfSentences`, optional. If passed, changes the prompt and asks for a summary in `numberOfSentences sentences.
* `modelParams`, optional. If passed, it is a JSON string containing an object with values for tuning the model: `temperature` (0-1), `topP (0-1), `responseMaxTokenCount` (integer), `stopSequences` (list of strings). ℹ️ This is an advanced usage. Si AWS Bedrock documentation.

Notice this is the same a using the `Bedrock.Run` operation, we just provide the prompt, like "Summarize the following text:", or "Merci de resumer ce texte en 3 phrases :"


### `Bedrock.Run` (category Services)

The operation runs a model, returns the response as Blob (text/plain). This blob can be previewed in the UI, and its text can be get using the `getString()` method of the blob.

#### input
Optional.
* If `void`/`null`, the operation only sends the `prompt` parameter.
* If `input` is a `Document`, _and_ it has a blob at the `xpath` parameter, it is then _appended_ (after converstion to plain text) to the prompt (with 2 lines before and after).

#### Parameters
* `prompt`, required. The prompt to send to the model ("What is the distance between the Earth and the Moon?". IN different languages if the model you are calling supports other languages)
* `modelId`, required. Default is `"anthropic.claude-instant-v1"`. **IMPORTANT** See _Supported Models_ above
* `awsRegion`, required. Default is `"us-east-1"`
* `xpath`, optional. If `input` is a `Document` and `xpath` is passed and contains a blob, it is converted to text and added to the prompt.
* `modelParams`, optional. If passed, it is a JSON string containing an object with values for tuning the model. For example, `temperature` (0-1), `topP` (0-1), `responseMaxTokenCount` (integer), `stopSequences` (list of strings). ℹ️ This is an advanced usage. Si AWS Bedrock documentation.

### `Bedrock.RunRaw` (category Services)

The operation runs a model with is expected parameters, returns the raw Java `InvokeModelResponse` object.

This operation allows for calling a model not yet supported by the plugin in terms of ease-of-use, pre-formatting, etc.

#### input
`void`, no input is expected

#### Parameters
* `modelId`, required.
* `awsRegion`, required.
* `requestBody`, required.
  * JSON string containing all and everything expected by the model.
  * Caller is in charge of filling it with the values expected by the model (see [Inference Parameters](https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters.html)), with the exact field names, the required fields, etc.
  *  For example, if a blob must be added to a prompt, caller must do it itself (typically, converting it to text using the any2pdf converter)

The operation returns the `InvokeModelResponse` Java object as received, caller is in charge of using the misc. `InvokeModelResponse` methods to get the details. See [its JavaDoc](https://javadoc.io/static/software.amazon.awssdk/bedrockruntime/2.21.12/index.html) (and usual warning: check the version)

ℹ️ Because it returns the raw `InvokeModelResponse`, this operation should not be called from the frontend which can't handle Java objects.


## Build

```
git clone https://github.com/nuxeo-sandbox/nuxeo-labs-genai-aws.git
cd nuxeo-labs-genai-aws
mvn clean install
```

The marketplace package is at `nuxeo-labs-genai-aws/target/nuxeo-labs-genai-aws-package-{VERSION}.zip`


## Support

> [!IMPORTANT]
> These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


## About Nuxeo

[Nuxeo](www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.
