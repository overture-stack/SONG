/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.sdk;

import bio.overture.song.client.cli.ClientMain;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.junit.BeforeClass;

public abstract class AbstractClientMainTest {

  private static final ByteArrayOutputStream BAOS_OUT = new ByteArrayOutputStream();
  private static final ByteArrayOutputStream BAOS_ERR = new ByteArrayOutputStream();
  private static final PrintStream REAL_OUT = System.out;
  private static final PrintStream REAL_ERR = System.err;

  /**
   * Proxy the System.out stream. A write request called on the custom PrintStream, will also be
   * called on the System.out stream and ByteArrayOutputStream (listener) stream, effectively
   * duplicating the write.
   */
  @BeforeClass
  public static void beforeClass() {
    val out = new PrintStream(new ProxyOutputStream(REAL_OUT, BAOS_OUT));
    val err = new PrintStream(new ProxyOutputStream(REAL_ERR, BAOS_ERR));
    System.setOut(out);
    System.setErr(err);
  }

  public ExitCodeCapture exitCodeCapture = new ExitCodeCapture();

  protected abstract ClientMain getClientMain();

  protected OutputConsole executeMain(String... args) {
    ClientMain.exit = exitCodeCapture;
    getClientMain().run(args);
    return captureConsole();
  }

  protected static OutputConsole captureConsole() {
    val o = OutputConsole.builder().out(BAOS_OUT.toString()).err(BAOS_ERR.toString()).build();
    BAOS_OUT.reset();
    BAOS_ERR.reset();
    return o;
  }

  protected Integer getExitCode() {
    return exitCodeCapture.getExitCode();
  }

  @Value
  @Builder
  public static class OutputConsole {
    private final String out;
    private final String err;
  }

  private static class ExitCodeCapture implements Consumer<Integer> {

    @Getter private Integer exitCode;

    @Override
    public void accept(Integer exitCode) {
      this.exitCode = exitCode;
    }
  }

  @RequiredArgsConstructor
  private static class ProxyOutputStream extends OutputStream {

    @NonNull private final OutputStream stream1;
    @NonNull private final OutputStream stream2;

    @Override
    public void write(int b) throws IOException {
      stream1.write(b);
      stream2.write(b);
    }

    @Override
    public void flush() throws IOException {
      stream1.flush();
      stream2.flush();
    }

    @Override
    public void close() throws IOException {
      stream1.close();
      stream2.close();
    }
  }
}
