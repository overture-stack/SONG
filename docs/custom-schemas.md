# Custom Analysis Schemas

## Minimal Example

```json
{
 "name": "minimalExample",
 "options":{},
 "schema":{
     "type": "object",
     "required":[
         "experiment"
     ],
     "properties":{
        "experiment":{}
     }
  }
}
```

## Options

All properties in options are optional. If no value is provided, a default configuration will be used for the analysis. If this is an update to an existing analysis type, you can omit any option and the value will be maintained from the previous version.

```json
{
	"fileTypes":["bam", "cram"],
	"externalValidation":[
		{
			"url": "http://localhost:8099/",
			"jsonPath": "experiment.someId"
		}
	]
},
```

### File Types

`options.fileTypes` can be provided an array of strings. These represent the file types (file extensions) that are allowed to be uploaded for this type of analysis.

If an empty array is provided, then any file type will be allowed. If an array of file types is provided, then an analysis will be invalid if it contains files of a type not listed.

```json
{
	"options": {
		"fileTypes": ["bam","cram"]
	}
}
```

### External Validation

External validations configure Song to check a value in the analysis against an external service by sending an HTTP GET request to a configurable URL. The URL needs to return 2XX status message to indicate that if the provided value "is valid", typically meaning that the value for this property is known by that service.

As an example, if the project clinical data is being managed in a separate service, we can add an external validation to the donor id field of our custom scheme. This will send the donor id to the external service which can confirm that we have previously registered that donor.

This might look like the following:

```json
{
	"url": "http://example.com/{study}/donor/{value}",
	"jsonPath": "experiment.donorId"
}
```

The URL provided is a template, with two variables that will be replaced during validation. Song will replace the token `{value}` with the value read from the analysis at the property as defined in the `jsonPath`. Song will also replace the token `{study}` with the study ID for the Analysis.

Continuing the above example, if the following analysis was submitted:

```json
{
  "studyId": "ABC123",
  "analysisType": {
    "name": "minimalExample"
  },
  "files": [
    {
      "dataType": "text",
      "fileName": "file1.txt",
      "fileSize": 123,
      "fileType": "txt",
      "fileAccess": "open",
      "fileMd5sum": "595f44fec1e92a71d3e9e77456ba80d1"
    }
  ],
  "experiment": {
    "donorId": "id01"
  }
}
```

Song would attempt to validate the donorId by sending a URL to `http://example.com/ABC123/donor/id01`.

It is allowable to use either the `{study}` or `{value}` multiple times in the URL, each instance will be replaced by the corresponding value.

> [!Warning]
> The URL may cause errors in song if it contains any tokens matching the `{word}` format other than `{study}` and `{value}`