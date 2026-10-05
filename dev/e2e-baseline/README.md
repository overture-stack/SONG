# End-to-end baseline (Phase 0.7)

Output of the Makefile end-to-end flow on the release before the Java/Spring upgrade
(song 5.4.1-SNAPSHOT, Java 11, Spring Boot 2.6), for comparison after each upgrade phase.

Flow, on a stack with an empty song-db:

```bash
docker compose down
make test-submit test-manifest test-score-upload test-publish test-unpublish
```

This runs the real Keycloak (API key introspection), Score server, MinIO and song-server containers.

- `make-output.txt`: the Makefile output with Docker build and pull progress removed. Analysis and
  object ids are random on every run.
- `analysis-after-unpublish.json`: `GET /studies/ABC123/analysis/{analysisId}` after the flow.

Compare the shape of each step's output (JSON fields, messages, manifest columns, upload summary),
not the ids or timestamps.
