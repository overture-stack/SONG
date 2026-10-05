package bio.overture.song.server.utils.contract;

import bio.overture.song.server.kafka.Sender;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;

/** A {@link Sender} that keeps every message in memory instead of publishing it. */
public class RecordingSender implements Sender {

  private final List<SentMessage> sentMessages = new ArrayList<>();

  @Override
  public synchronized void send(String payload, String key) {
    sentMessages.add(new SentMessage(payload, key));
  }

  /** Returns the messages recorded since the last call, oldest first, and forgets them. */
  public synchronized List<SentMessage> takeSentMessages() {
    final List<SentMessage> taken = List.copyOf(sentMessages);
    sentMessages.clear();
    return taken;
  }

  @Value
  public static class SentMessage {
    String payload;
    String key;
  }
}
