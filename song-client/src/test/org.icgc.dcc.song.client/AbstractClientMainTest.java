package org.icgc.dcc.song.client;

import lombok.Getter;
import org.junit.Rule;
import org.springframework.boot.test.rule.OutputCapture;

import java.util.function.Consumer;

public class AbstractClientMainTest {
  @Rule
  public OutputCapture capture = new OutputCapture();
  public ExitCodeCapture exitCodeCapture = new ExitCodeCapture();

  protected void executeMain(String... args) {
    SpringApp.exit = exitCodeCapture;
    SpringApp.main(args);
  }

  protected String getOutput() {
    return capture.toString();
  }

  protected Integer getExitCode() {
    return exitCodeCapture.getExitCode();
  }

  private static class ExitCodeCapture implements Consumer<Integer> {

    @Getter
    private Integer exitCode;

    @Override
    public void accept(Integer exitCode) {
      this.exitCode = exitCode;
    }

  }


}
