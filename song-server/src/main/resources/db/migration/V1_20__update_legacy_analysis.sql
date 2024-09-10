UPDATE public.analysis_schema
SET "schema"='{"type":"object","required":["experiment"],"properties":{"experiment":{"type":"object","required":["matchedNormalSampleSubmitterId","variantCallingTool"],"properties":{"fileTypes":{"oneOf":[{"$ref":"classpath:/schemas/analysis/analysisBase.json#/definitions/file/fileType"},{"type":"string"}]},"variantCallingTool":{"type":"string"},"matchedNormalSampleSubmitterId":{"type":"string"}}}}}'::jsonb
WHERE "name"='variantCall';


UPDATE public.analysis_schema
SET "schema"='{"type":"object","required":["experiment"],"properties":{"experiment":{"type":"object","required":["libraryStrategy"],"properties":{"aligned":{"type":["boolean","null"]},"fileTypes":{"oneOf":[{"$ref":"classpath:/schemas/analysis/analysisBase.json#/definitions/file/fileType"},{"type":"string"}]},"pairedEnd":{"type":["boolean","null"]},"insertSize":{"type":["integer","null"]},"alignmentTool":{"type":["string","null"]},"libraryStrategy":{"enum":["WGS","WXS","RNA-Seq","ChIP-Seq","miRNA-Seq","Bisulfite-Seq","Validation","Amplicon","Other"],"type":"string"},"referenceGenome":{"type":["string","null"]}}}}}'::jsonb
WHERE "name"='sequencingRead';