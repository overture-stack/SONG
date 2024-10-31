# ID Management

Song provides two distinct approaches for managing primary keys across entities (Donors, Specimens, Samples, and Files):

### Local Mode
- Internally manages IDs within Song's system
- Uses UUID format
- Ensures thread safety and consistency through internal memory
- All IDs except analysisId are stateless and computed using UUID5 hash
- AnalysisIDs are stateful to guarantee uniqueness

### Federated Mode
- Relies on external service for ID management
- Supports two authentication methods:
  - Dynamic JWT tokens
  - Predefined static keys
- Requires validation against external ID database
- Stores validated IDs in Song database

:::info Important
You must choose either Local or Federated mode for all IDs. Mixing modes is not supported.
:::

## Configuration

### Local Mode Setup

To use Song's internal ID management system:

```env
ID_USELOCAL=true
```

:::info
 The `AnalysisService.create` method handles analysisId registration, so `LocalIdService` doesn't need to save/register analysisIds directly.
:::

### Federated Mode Setup

To use external ID management:

1. Set the base configuration:
```env
ID_USELOCAL=false
```

2. Configure entity-specific URI templates:
```env
# Entity URIs
ID_FEDERATED_URITEMPLATE_DONOR=https://id.server.org/donor/id?projectCode={studyId}&donorSubmittedId={submitterId}&create=true
ID_FEDERATED_URITEMPLATE_SPECIMEN=https://id.server.org/specimen/id?projectCode={studyId}&specimenSubmittedId={submitterId}&create=true
ID_FEDERATED_URITEMPLATE_SAMPLE=https://id.server.org/sample/id?projectCode={studyId}&sampleSubmittedId={submitterId}&create=true
ID_FEDERATED_URITEMPLATE_FILE=https://id.server.org/file/id?bundleId={analysisId}&fname={fileName}

# Analysis-specific URIs
ID_FEDERATED_URITEMPLATE_ANALYSIS_EXISTENCE=https://id.server.org/analysis/id?submittedAnalysisId={analysisId}&create=false
ID_FEDERATED_URITEMPLATE_ANALYSIS_GENERATE=https://id.server.org/analysis/id/generate
ID_FEDERATED_URITEMPLATE_ANALYSIS_SAVE=https://id.server.org/analysis/id?submittedAnalysisId={submitterId}&create=true
```

3. Configure authentication:
```env
# Base auth URL
ID_FEDERATED_AUTH_URL=https://auth.server.org

# For static authentication (FEDERATED_STATIC_AUTH)
ID_FEDERATED_AUTH_BEARER_TOKEN=your_static_token

# For dynamic authentication (FEDERATED_DYNAMIC_AUTH)
ID_FEDERATED_AUTH_BEARER_CREDENTIALS_CLIENTID=authClientID
ID_FEDERATED_AUTH_BEARER_CREDENTIALS_CLIENTSECRET=authClientSecret
```

### Application YAML Configuration

You can configure ID Management in your `application.yaml` file. Here are the available options:

```yaml
id:
  # Enable local ID management
  useLocal: true  # Set to false for federated mode
  
  # Optional: Enable in-memory persistence for testing
  persistInMemory: true  # Only recommended for development/testing
  
  # Federated mode configuration
  federated:
    # Authentication configuration
    auth:
      bearer:
        # Static token authentication
        token: "your-static-token"
        # Dynamic authentication credentials
        credentials:
          url: "https://auth.server.org"
          clientId: "your-client-id"
          clientSecret: "your-client-secret"
    
    # URI templates for federated services
    uriTemplate:
      # Entity ID endpoints
      donor: "https://id.example.org/donor/id?submittedProjectId={studyId}&submittedDonorId={submitterId}&create=true"
      specimen: "https://id.example.org/specimen/id?submittedProjectId={studyId}&submittedSpecimenId={submitterId}&create=true"
      sample: "https://id.example.org/sample/id?submittedProjectId={studyId}&submittedSampleId={submitterId}&create=true"
      file: "https://id.example.org/file/id?bundleId={analysisId}&fname={fileName}"
      
      # Analysis-specific endpoints
      analysisExistence: "https://id.example.org/analysis/id?submittedAnalysisId={analysisId}&create=false"
      analysisGenerate: "https://id.example.org/analysis/id/generate"
      analysisSave: "https://id.example.org/analysis/id?submittedAnalysisId={submitterId}&create=true"
```

### Profile-Specific Configuration

Song supports different configuration profiles. Here's how to configure ID management for specific profiles:

```yaml
---
spring:
  config:
    activate:
      on-profile: dev
id:
  persistInMemory: true  # Enable in-memory persistence for development

---
spring:
  config:
    activate:
      on-profile: prod
id:
  useLocal: false  # Use federated mode in production
  federated:
    auth:
      bearer:
        credentials:
          url: "https://prod-auth.example.org"
          clientId: ${PROD_CLIENT_ID}
          clientSecret: ${PROD_CLIENT_SECRET}
```


## URI Requirements

When using federated mode, the external ID service must:

- Implement GET controllers for all configured URI templates
- Support either static or dynamic authentication
- Return appropriate responses for each entity type

### Required URI Parameters

| Entity Type | Required Variables | Example URI | Response Type |
|-------------|-------------------|-------------|---------------|
| Donor | studyId, submitterId | `/donor/id?projectCode={studyId}&donorSubmittedId={submitterId}` | plaintext |
| Specimen | studyId, submitterId | `/specimen/id?projectCode={studyId}&specimenSubmittedId={submitterId}` | plaintext |
| Sample | studyId, submitterId | `/sample/id?projectCode={studyId}&sampleSubmittedId={submitterId}` | plaintext |
| File | analysisId, fileName | `/file/id?bundleId={analysisId}&fname={fileName}` | plaintext |
| Analysis Existence | analysisId | `/analysis/id?submittedAnalysisId={analysisId}&create=false` | plaintext |
| Analysis Generate | none | `/analysis/id/generate` | plaintext |
| Analysis Save | analysisId | `/analysis/id?submittedAnalysisId={submitterId}&create=true` | none |

### ICGC ARGO Example

The <a href="https://platform.icgc-argo.org/" target="_blank" rel="noopener noreferrer">ICGC ARGO Data Platform</a> is an international initiative with several distributed processing centres. This required the use of a central ID Service. An example of a URI donor request used by this system is as follows:

`https://clinical.platform.icgc-argo.org/clinical/donors/id?programId=PACA-CA&submitterId=PCSI_0591`

In the provided URI, a researcher requests the centralized ID service to retrieve the unique identifier for a **donor** associated with the programId **PACA-CA** and the submitterID **PCSI_0591**.

#### 200 Response:

```shell
DO224719
```

#### 404 Response:

```json
{
  "error": "Error",
  "message": "Donor not found"
}
```
