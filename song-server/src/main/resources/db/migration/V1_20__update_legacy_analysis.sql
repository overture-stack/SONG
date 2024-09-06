UPDATE public.analysis_schema
SET  "schema"='{"type":"object","required":["experiment"],"properties":{"experiment":{"type":"object","required":["matchedNormalSampleSubmitterId","variantCallingTool"],"properties":{"variantCallingTool":{"type":"string"},"matchedNormalSampleSubmitterId":{"type":"string"},"fileTypes":{"enum":["VCF","BAM","OTHER_CUSTOM_TYPE"],"type":"string"}}}}}'::jsonb
where name = 'variantCall';


UPDATE public.analysis_schema
SET  "schema"='{"type":"object","required":["experiment"],"properties":{"experiment":{"type":"object","required":["libraryStrategy"],"properties":{"aligned":{"type":["boolean","null"]},"pairedEnd":{"type":["boolean","null"]},"insertSize":{"type":["integer","null"]},"alignmentTool":{"type":["string","null"]},"libraryStrategy":{"enum":["WGS","WXS","RNA-Seq","ChIP-Seq","miRNA-Seq","Bisulfite-Seq","Validation","Amplicon","Other"],"type":"string"},"referenceGenome":{"type":["string","null"]},"fileTypes":{"enum":["FASTQ","BAM","CRAM"],"type":"string"}}}}}'::jsonb
where name = 'sequencingRead';