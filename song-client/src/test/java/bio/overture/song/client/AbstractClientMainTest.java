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

package bio.overture.song.client;

import java.util.function.Consumer;
import lombok.Getter;
import org.junit.Rule;
import org.springframework.boot.test.rule.OutputCapture;

public class AbstractClientMainTest {

  @Rule public OutputCapture capture = new OutputCapture();
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

    @Getter private Integer exitCode;

    @Override
    public void accept(Integer exitCode) {
      this.exitCode = exitCode;
    }
  }
}
