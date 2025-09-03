# Creating a pipeline
Define a pipeline using JSON-based definitions in your organization.

## 1. Define pipeline assembly Json
Specify the processing elements that are going to be part of the pipeline, the parameters have to align with existing processing element templates stored in organizations.

Then connect the processing elements through channels by listing the publisher with its subscribers. Make sure the output of the publisher matches the input of the subscribers. The syntax of a pipeline representation is defined in the JSON schemas in src/main/resources/jsonschemas.

An example of a simple pipeline:
```json
{
  "processing elements":[
    { "organizationID": "orgA", "hostURL": "http://localhost:8082", "templateID": "SimpleSource", "inputs": [], "output": "Event", "instanceNumber": 1, "configuration": {} },
    { "organizationID": "orgB", "hostURL": "http://localhost:8083", "templateID": "SimpleOperator",  "inputs": ["Event"],  "output": "Event",  "instanceNumber": 1, "configuration": {} },
    { "organizationID": "orgA", "hostURL": "http://localhost:8082", "templateID": "SimpleSink",  "inputs": ["Event"],  "output": null,  "instanceNumber": 1, "configuration": {} }
  ],

  "channels": [
    {
      "publisher": { "organizationID": "orgA", "hostURL": "http://localhost:8082", "templateID": "SimpleSource", "inputs": [], "output": "Event", "instanceNumber": 1, "configuration": {} },
      "subscribers": [
        {
          "processing element": { "organizationID": "orgB", "hostURL": "http://localhost:8083", "templateID": "SimpleOperator", "inputs": ["Event"], "output": "Event", "instanceNumber": 1, "configuration": {} },
          "portNumber": 1
        }
      ]
    },
    {
      "publisher": { "organizationID": "orgB", "hostURL": "http://localhost:8083", "templateID": "SimpleOperator", "inputs": ["Event"], "output": "Event", "instanceNumber": 1, "configuration": {} },
      "subscribers": [
        {
          "processing element": { "organizationID": "orgA", "hostURL": "http://localhost:8082", "templateID": "SimpleSink", "inputs": ["Event"], "output": null, "instanceNumber": 1, "configuration": {} },
          "portNumber": 1
        }
      ]
    }
  ]
}
```
## 2. Create config schema
Each processing element defined in the pipeline assembly JSON must have a corresponding config schema. This schema defines the expected structure of the configuration object used for that particular template and whether additional properties are required and/or allowed.
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://www.dapm.org/config/orga_example_config_schema.json",
  "title": "orgA Example Config",
  "type": "object",
  "properties": {
    "some required string": { "type":  "string", "minLength": 2, "maxLength": 2},
    "optional property": { "type": "integer"}
  },
  "required": ["some required string"],
  "additionalProperties": false
}
```
**Save the configuration schema file using the following naming convention:**
`<organizationID>_<templateID>_config_schema.json`
