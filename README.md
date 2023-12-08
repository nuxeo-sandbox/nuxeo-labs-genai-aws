# nuxeo-labs-genai-aws

> [!IMPORTANT]
> This is **W**ork **I**n **P**rogress, using GitHub as backup

This plugin allows for calling Generative AI on AWS, using Bedrock.

First Use Case is about summarizing a text.

For now, we use Titan, as first test.

If you find this plugin and want to test it:

* Setup the correct environment so as to be able to call AWS
* Enable/Activate the following models on AWS: `amazon.titan-text-express-v1` and `anthropic.claude-v2`
* Run the unit tests. Notice we use only Titan, so far.

> [!IMPORTANT]
> REMEMBER: This is **W**ork **I**n **P**rogress, using GitHub as backup

ToDo:

* Priority:
  * Handle text-based blobs (pdf, Word, mainly)
  * Add automation for easy call from Nuxeo in the UI
  * Add better result, with error when it occur (model not found, model does not support other languages, ...)
* Less urgent:
  * Make a configurable service (list models to use, bu then describe the input/output JSON format expected...)
  * Make more parameters available to tune the call to the service
  * Make the cal async
  * Maybe make a service where other providers can be plugged?
  * ...


## Support

> [!IMPORTANT]
> These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


## About Nuxeo

[Nuxeo](www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.
