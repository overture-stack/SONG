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

package org.icgc.dcc.song.server.kafka.impl;

import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.song.server.kafka.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

@Profile("kafka")
@Slf4j
public class KafkaSender implements Sender {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public void send(String payload) {
    log.debug("sending payload='{}' to topic='{}'", payload, kafkaTemplate.getDefaultTopic());
    kafkaTemplate.send(kafkaTemplate.getDefaultTopic(), payload);
  }

}
