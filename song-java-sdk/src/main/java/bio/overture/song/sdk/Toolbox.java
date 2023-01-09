/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

import static lombok.AccessLevel.PRIVATE;

import bio.overture.song.sdk.config.RestClientConfig;
import bio.overture.song.sdk.config.RetryConfig;
import bio.overture.song.sdk.config.SdkConfig;
import bio.overture.song.sdk.config.impl.DefaultRetryConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public class Toolbox {

  @NonNull private final SongApi songApi;
  @NonNull private final ManifestClient manifestClient;

  public static Toolbox createToolbox(@NonNull SdkConfig config) {
    return createToolbox(config.getClient(), config.getRetry());
  }

  public static Toolbox createToolbox(
      @NonNull RestClientConfig restClientConfig, @NonNull RetryConfig retryConfig) {
    val factory =
        Factory.builder().retryConfig(retryConfig).restClientConfig(restClientConfig).build();
    val songApi = factory.buildSongApi();
    val manifestClient = new ManifestClient(songApi);
    return new Toolbox(songApi, manifestClient);
  }

  public static Toolbox createToolbox(@NonNull RestClientConfig restClientConfig) {
    return createToolbox(restClientConfig, new DefaultRetryConfig());
  }
}
