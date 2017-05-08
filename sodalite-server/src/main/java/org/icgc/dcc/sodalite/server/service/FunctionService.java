package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionService {

  private void info(String fmt, Object... args) {
    log.info(format(fmt, args));
  }

  public int notifyUpload(String id) {
    // TODO Auto-generated method stub
    info("Called notifyUpload with %s", id);
    return 0;

  }

  public int publish(String id) {
    // TODO Auto-generated method stub
    info("Called publish with %s", id);
    return 0;
  }

}
