# Kafka Setup

Song can be configured to integrate with Kafka for event streaming. This page explains how to enable and configure the Kafka integration.

:::info Song → Kafka → Maestro Flow

Song publishes message to song-analysis Kafka topic
Maestro listens to this topic and processes incoming messages
Maestro transforms and indexes data into Elasticsearch

This automated flow ensures Song publications are indexed.
:::

## Configuration

### Enable Kafka Profile

To enable Kafka integration, you need to activate the `kafka` profile in your Song deployment. This can be done by setting the following environment variable:

```bash
SPRING_CONFIG_ACTIVATE_ON_PROFILE=kafka
```

### Basic Configuration

The following configuration is used for Kafka integration in [Songs application.yaml](https://github.com/overture-stack/SONG/blob/develop/song-server/src/main/resources/application.yml):

```yaml title="./song-server/src/main/resources/application.yml"
spring:
  kafka:
    bootstrap-servers: localhost:9092
    template:
      default-topic: song-analysis
```

### Environment Variables

You can configure Kafka using the following environment variables:

- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Comma-separated list of host:port pairs for Kafka brokers (localhost:9092)
- `SPRING_KAFKA_TEMPLATE_DEFAULT_TOPIC`: Default topic where Song will publish analysis events (song-analysis)

## Example Docker Environment

Here's an example of how to set these variables in a Docker environment file:

```bash
# Kafka Configuration
SPRING_CONFIG_ACTIVATE_ON_PROFILE=kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
SPRING_KAFKA_TEMPLATE_DEFAULT_TOPIC=song-analysis
```

:::info Need Help?
If you encounter any issues or have questions about our API, please don't hesitate to reach out through our relevant [**community support channels**](https://docs.overture.bio/community/support).
:::