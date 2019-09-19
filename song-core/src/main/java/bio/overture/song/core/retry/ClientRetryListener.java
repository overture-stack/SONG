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
package bio.overture.song.core.retry;

import static lombok.AccessLevel.PROTECTED;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.retry.listener.RetryListenerSupport;

/**
 * ClientRetryListener allows to inject client logic which will be executed before any statements of
 * {@link DefaultRetryListener}. If after a call to the ClientRetryListener {@code isRetry()}
 * returns {@code FALSE} the default retry logic will not be executed.
 */
@Data
@FieldDefaults(level = PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class ClientRetryListener extends RetryListenerSupport {

  boolean retry = true;
}
